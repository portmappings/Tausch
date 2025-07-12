package me.portmapping.trading.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.ui.user.TradeMenu;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.item.InventoryUtil;
import me.portmapping.trading.utils.item.ItemUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@RequiredArgsConstructor
public class TradeSession {

    private final UUID sender;
    private final UUID target;

    private boolean senderConfirmed = false;
    private boolean targetConfirmed = false;

    private final List<ItemStack> senderItems = new ArrayList<>();
    private final List<ItemStack> targetItems = new ArrayList<>();

    private int countdown = 4;

    /**
     * Epoch milliseconds of when the trade was successfully completed.
     * A value of {@code 0L} means the trade has not been completed yet or was cancelled.
     */
    private long completedAt = 0L;

    public static final int MAX_ITEMS_PER_PLAYER = 16;

    public UUID getOther(UUID playerId) {
        return playerId.equals(sender) ? target : (playerId.equals(target) ? sender : null);
    }

    public UUID getOther(Player player) {
        return getOther(player.getUniqueId());
    }

    public List<ItemStack> getPlayerItems(UUID playerId) {
        if (playerId.equals(sender)) return senderItems;
        if (playerId.equals(target)) return targetItems;
        return Collections.emptyList();
    }

    public List<ItemStack> getOtherPlayerItems(UUID playerId) {
        if (playerId.equals(sender)) return targetItems;
        if (playerId.equals(target)) return senderItems;
        return Collections.emptyList();
    }

    public boolean addItem(UUID playerId, ItemStack item) {
        List<ItemStack> items = getPlayerItems(playerId);
        if (items.size() >= MAX_ITEMS_PER_PLAYER) return false;

        items.add(item);
        countdown = 4;
        senderConfirmed = false;
        targetConfirmed = false;
        return true;
    }

    public void removeItem(UUID playerId, int index) {
        List<ItemStack> items = getPlayerItems(playerId);
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            countdown = 4;
            senderConfirmed = false;
            targetConfirmed = false;
        }
    }

    public void toggleConfirmation(UUID playerId) {
        if (countdown > 1) return;

        if (playerId.equals(sender)) {
            senderConfirmed = !senderConfirmed;
        } else if (playerId.equals(target)) {
            targetConfirmed = !targetConfirmed;
        }
    }

    public boolean bothConfirmed() {
        return senderConfirmed && targetConfirmed;
    }

    public boolean hasConfirmed(UUID playerId) {
        if (playerId.equals(sender)) return senderConfirmed;
        if (playerId.equals(target)) return targetConfirmed;
        return false;
    }

    public boolean canConfirm() {
        return countdown <= 1;
    }

    public boolean completeTrade() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (senderPlayer == null || targetPlayer == null) {
            cancelTrade();
            return false;
        }

        if (!InventoryUtil.hasInventorySpace(senderPlayer, targetItems) ||
                !InventoryUtil.hasInventorySpace(targetPlayer, senderItems)) {

            String spaceMessage = CC.t("&cTrade failed: someone needs more empty slots!");
            senderPlayer.sendMessage(spaceMessage);
            targetPlayer.sendMessage(spaceMessage);
            reopenMenus();
            return false;
        }

        Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
            senderPlayer.getInventory().addItem(targetItems.toArray(new ItemStack[0]));
            targetPlayer.getInventory().addItem(senderItems.toArray(new ItemStack[0]));

            String successMessage = "&aTrade complete!";
            CC.sendMessage(senderPlayer, successMessage);
            CC.sendMessage(targetPlayer, successMessage);

            completedAt = System.currentTimeMillis();

            Document tradeDoc = toBson();
            Bukkit.getScheduler().runTaskAsynchronously(
                    Tausch.getInstance(),
                    () -> Tausch.getInstance()
                            .getMongoHandler()
                            .getTradeHistory()
                            .insertOne(tradeDoc)
            );

            // Clean up
            cancel();
            Tausch.getInstance().getTradeManager().getActiveTrades().remove(sender);
            Tausch.getInstance().getTradeManager().getActiveTrades().remove(target);
            senderPlayer.closeInventory();
            targetPlayer.closeInventory();
        });

        return true;
    }

    public void cancelTrade() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);

        String cancelMessage = CC.t("&cTrade cancelled.");
        if (senderPlayer != null) senderPlayer.sendMessage(cancelMessage);
        if (targetPlayer != null) targetPlayer.sendMessage(cancelMessage);

        cancel();
        Tausch.getInstance().getTradeManager().getActiveTrades().remove(sender);
        Tausch.getInstance().getTradeManager().getActiveTrades().remove(target);
    }

    public void reopenMenus() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (senderPlayer != null) {
            new TradeMenu(this).openMenu(senderPlayer);
        }
        if (targetPlayer != null) {
            new TradeMenu(this).openMenu(targetPlayer);
        }
    }

    public void cancel() {
        senderItems.clear();
        targetItems.clear();
        senderConfirmed = false;
        targetConfirmed = false;
        countdown = 4;
    }

    public Document toBson() {
        return new Document()
                .append("sender", sender.toString())
                .append("target", target.toString())
                .append("senderConfirmed", senderConfirmed)
                .append("targetConfirmed", targetConfirmed)
                .append("senderItems", senderItems.stream()
                        .map(ItemUtil::itemStackToBase64)
                        .collect(Collectors.toList()))
                .append("targetItems", targetItems.stream()
                        .map(ItemUtil::itemStackToBase64)
                        .collect(Collectors.toList()))
                .append("completedAt", completedAt);
    }

    public static TradeSession fromBson(Document doc) {
        TradeSession session = new TradeSession(
                UUID.fromString(doc.getString("sender")),
                UUID.fromString(doc.getString("target")));

        session.senderConfirmed = doc.getBoolean("senderConfirmed", false);
        session.targetConfirmed = doc.getBoolean("targetConfirmed", false);
        session.completedAt     = doc.getLong("completedAt") != null ? doc.getLong("completedAt") : 0L;

        List<String> senderBase64 = doc.getList("senderItems", String.class, List.of());
        session.senderItems.addAll(
                senderBase64.stream()
                        .map(ItemUtil::itemStackFromBase64)
                        .toList());

        List<String> targetBase64 = doc.getList("targetItems", String.class, List.of());
        session.targetItems.addAll(
                targetBase64.stream()
                        .map(ItemUtil::itemStackFromBase64)
                        .toList());

        return session;
    }
}
