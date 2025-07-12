package me.portmapping.trading.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.ui.user.TradeMenu;
import me.portmapping.trading.utils.chat.CC;
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

    private long lastChangeTime = 0;
    private int countdownTaskId = -1;

    public static final int MAX_ITEMS_PER_PLAYER = 16;
    public static final long CHANGE_COOLDOWN_MS = 4000;

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
        markChanged();
        return true;
    }

    public void removeItem(UUID playerId, int index) {
        List<ItemStack> items = getPlayerItems(playerId);
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            markChanged();
        }
    }

    public void toggleConfirmation(UUID playerId) {
        if (!canConfirm()) return;

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
        return System.currentTimeMillis() - lastChangeTime >= CHANGE_COOLDOWN_MS;
    }

    public int getRemainingSeconds() {
        long remaining = Math.max(0, CHANGE_COOLDOWN_MS - (System.currentTimeMillis() - lastChangeTime));
        return (int) Math.ceil(remaining / 1000.0);
    }

    public boolean completeTrade() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (senderPlayer == null || targetPlayer == null) {
            handleOfflinePlayer(senderPlayer, targetPlayer);
            return false;
        }

        if (!hasInventorySpace(senderPlayer, targetItems) || !hasInventorySpace(targetPlayer, senderItems)) {
            handleInsufficientSpace(senderPlayer, targetPlayer);
            return false;
        }

        executeTradeExchange(senderPlayer, targetPlayer);
        return true;
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
        lastChangeTime = 0;
        cancelCountdownTask();
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
                        .collect(Collectors.toList()));
    }

    public static TradeSession fromBson(Document doc) {
        TradeSession session = new TradeSession(
                UUID.fromString(doc.getString("sender")),
                UUID.fromString(doc.getString("target")));

        session.senderConfirmed = doc.getBoolean("senderConfirmed", false);
        session.targetConfirmed = doc.getBoolean("targetConfirmed", false);

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

    private void markChanged() {
        lastChangeTime = System.currentTimeMillis();
        senderConfirmed = false;
        targetConfirmed = false;
        startCountdownTask();
    }

    private void startCountdownTask() {
        cancelCountdownTask();

        if (lastChangeTime > 0) {
            countdownTaskId = Bukkit.getScheduler().runTaskTimer(Tausch.getInstance(), () -> {
                if (canConfirm()) {
                    reopenMenus();
                    cancelCountdownTask();
                    return;
                }
                reopenMenus();
            }, 0L, 20L).getTaskId();
        }
    }

    private void cancelCountdownTask() {
        if (countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
            countdownTaskId = -1;
        }
    }

    private boolean hasInventorySpace(Player player, List<ItemStack> items) {
        if (items.isEmpty()) return true;
        return player.getInventory().firstEmpty() != -1 ||
                player.getInventory().addItem(items.toArray(new ItemStack[0])).isEmpty();
    }

    private void handleOfflinePlayer(Player senderPlayer, Player targetPlayer) {
        String offlineMessage = CC.t("&cTrade cancelled, player went offline.");
        if (senderPlayer != null) senderPlayer.sendMessage(offlineMessage);
        if (targetPlayer != null) targetPlayer.sendMessage(offlineMessage);
        cancel();
    }

    private void handleInsufficientSpace(Player senderPlayer, Player targetPlayer) {
        String spaceMessage = CC.t("&cTrade failed: someone needs more empty slots!");
        senderPlayer.sendMessage(spaceMessage);
        targetPlayer.sendMessage(spaceMessage);
        reopenMenus();
    }

    private void executeTradeExchange(Player senderPlayer, Player targetPlayer) {
        Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
            senderPlayer.getInventory().addItem(targetItems.toArray(new ItemStack[0]));
            targetPlayer.getInventory().addItem(senderItems.toArray(new ItemStack[0]));

            String successMessage = "&aTrade complete!";
            CC.sendMessage(senderPlayer, successMessage);
            CC.sendMessage(targetPlayer, successMessage);

            saveTradeToDatabase();
            closeInventoriesAndCleanup(senderPlayer, targetPlayer);
        });
    }

    private void saveTradeToDatabase() {
        Document tradeDoc = toBson();
        Bukkit.getScheduler().runTaskAsynchronously(
                Tausch.getInstance(),
                () -> Tausch.getInstance()
                        .getMongoHandler()
                        .getTradeHistory()
                        .insertOne(tradeDoc)
        );
    }

    private void closeInventoriesAndCleanup(Player senderPlayer, Player targetPlayer) {
        cancel();
        senderPlayer.closeInventory();
        targetPlayer.closeInventory();
    }
}