package me.portmapping.trading.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.ui.user.TradeMenu;
import me.portmapping.trading.utils.chat.Language;
import me.portmapping.trading.utils.item.InventoryUtil;
import me.portmapping.trading.utils.item.ItemUtil;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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



    public boolean isCompleted() {
        return completedAt > 0L;
    }

    public void completeTrade() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (senderPlayer == null || targetPlayer == null) {
            cancelTrade();
            return;
        }

        if (!InventoryUtil.hasInventorySpace(senderPlayer, targetItems) ||
                !InventoryUtil.hasInventorySpace(targetPlayer, senderItems)) {
            String spaceMessage = Language.TRADE_FAILED_NO_SPACE;
            senderPlayer.sendMessage(spaceMessage);
            targetPlayer.sendMessage(spaceMessage);
            reopenMenus();
            return;
        }

        Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
            senderPlayer.getInventory().addItem(targetItems.toArray(new ItemStack[0]));
            targetPlayer.getInventory().addItem(senderItems.toArray(new ItemStack[0]));
            senderPlayer.playSound(senderPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            targetPlayer.playSound(targetPlayer.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            sendTradeCompletionMessage(senderPlayer, targetItems, senderItems);
            sendTradeCompletionMessage(targetPlayer, senderItems, targetItems);

            completedAt = System.currentTimeMillis();

            Document tradeDoc = toBson();
            Bukkit.getScheduler().runTaskAsynchronously(
                    Tausch.getInstance(),
                    () -> Tausch.getInstance()
                            .getMongoHandler()
                            .getTradeHistory()
                            .insertOne(tradeDoc)
            );

            Tausch.getInstance().getTradeManager().getActiveTrades().remove(sender);
            Tausch.getInstance().getTradeManager().getActiveTrades().remove(target);
            senderPlayer.closeInventory();
            targetPlayer.closeInventory();
        });

    }

    private void sendTradeCompletionMessage(Player player, List<ItemStack> receivedItems, List<ItemStack> givenItems) {
        player.sendMessage(Language.TRADE_COMPLETED);

        if (!givenItems.isEmpty()) {
            for (ItemStack item : givenItems) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    String name = getItemDisplayName(item);
                    String message = Language.ITEM_GIVEN_FORMAT
                            .replace("%amount%", String.valueOf(item.getAmount()))
                            .replace("%item%", name);
                    player.sendMessage(message);
                }
            }
        }

        if (!receivedItems.isEmpty()) {
            for (ItemStack item : receivedItems) {
                if (item != null && item.getType() != org.bukkit.Material.AIR) {
                    String name = getItemDisplayName(item);
                    String message = Language.ITEM_RECEIVED_FORMAT
                            .replace("%amount%", String.valueOf(item.getAmount()))
                            .replace("%item%", name);
                    player.sendMessage(message);
                }
            }
        }
    }

    private String getItemDisplayName(ItemStack item) {
        if (item.hasItemMeta() && Objects.requireNonNull(item.getItemMeta()).hasDisplayName()) {
            return item.getItemMeta().getDisplayName();
        }
        String materialName = item.getType().name().toLowerCase().replace('_', ' ');
        return Arrays.stream(materialName.split(" "))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1))
                .collect(Collectors.joining(" "));
    }

    public void cancelTrade() {
        if (isCompleted()) return;
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);
        String cancelMessage = Language.TRADE_CANCELLED;
        if (senderPlayer != null) senderPlayer.sendMessage(cancelMessage);
        if (targetPlayer != null) targetPlayer.sendMessage(cancelMessage);
        clearTradeData();
        Tausch.getInstance().getTradeManager().getActiveTrades().remove(sender);
        Tausch.getInstance().getTradeManager().getActiveTrades().remove(target);
    }

    public void reopenMenus() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);
        if (senderPlayer != null) new TradeMenu(this).openMenu(senderPlayer);
        if (targetPlayer != null) new TradeMenu(this).openMenu(targetPlayer);
    }

    private void clearTradeData() {
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
        session.completedAt = doc.getLong("completedAt") != null ? doc.getLong("completedAt") : 0L;
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
