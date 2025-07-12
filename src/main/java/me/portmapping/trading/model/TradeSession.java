package me.portmapping.trading.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.ui.user.TradeMenu;
import me.portmapping.trading.utils.ItemStackBsonUtil;
import me.portmapping.trading.utils.chat.CC;
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
        if (playerId.equals(sender)) return target;
        if (playerId.equals(target)) return sender;
        return null;
    }

    public UUID getOther(Player player) {
        return getOther(player.getUniqueId());
    }

    public boolean isSender(UUID playerId) {
        return playerId.equals(sender);
    }

    public boolean isSender(Player player) {
        return isSender(player.getUniqueId());
    }

    public boolean isTarget(UUID playerId) {
        return playerId.equals(target);
    }

    public boolean isTarget(Player player) {
        return isTarget(player.getUniqueId());
    }

    public boolean hasConfirmed(UUID playerId) {
        if (playerId.equals(sender)) return senderConfirmed;
        if (playerId.equals(target)) return targetConfirmed;
        return false;
    }

    public boolean hasConfirmed(Player player) {
        return hasConfirmed(player.getUniqueId());
    }

    public boolean canAddItem(UUID playerId) {
        List<ItemStack> items = getPlayerItems(playerId);
        return items.size() < MAX_ITEMS_PER_PLAYER;
    }

    public boolean canAddItem(Player player) {
        return canAddItem(player.getUniqueId());
    }

    public boolean addItemToTrade(UUID playerId, ItemStack item) {
        if (!canAddItem(playerId)) {
            return false;
        }

        List<ItemStack> items = getPlayerItems(playerId);
        items.add(item);
        markChanged();
        return true;
    }

    public boolean addItemToTrade(Player player, ItemStack item) {
        return addItemToTrade(player.getUniqueId(), item);
    }

    public void removeItemFromTrade(UUID playerId, int index) {
        List<ItemStack> items = getPlayerItems(playerId);
        if (index >= 0 && index < items.size()) {
            items.remove(index);
            markChanged();
        }
    }

    public void removeItemFromTrade(Player player, int index) {
        removeItemFromTrade(player.getUniqueId(), index);
    }

    public void markChanged() {
        lastChangeTime = System.currentTimeMillis();
        resetConfirmation();
        startCountdownTask();
    }

    public boolean canConfirm() {
        return System.currentTimeMillis() - lastChangeTime >= CHANGE_COOLDOWN_MS;
    }

    public long getRemainingCooldown() {
        long elapsed = System.currentTimeMillis() - lastChangeTime;
        return Math.max(0, CHANGE_COOLDOWN_MS - elapsed);
    }

    public int getRemainingSeconds() {
        long remaining = getRemainingCooldown();
        return (int) Math.ceil(remaining / 1000.0);
    }

    public void startCountdownTask() {
        cancelCountdownTask();

        if (lastChangeTime > 0) {
            countdownTaskId = Bukkit.getScheduler().runTaskTimer(Tausch.getInstance(), new Runnable() {
                @Override
                public void run() {
                    if (canConfirm()) {
                        reopenMenusForBothPlayers();
                        cancelCountdownTask();
                        return;
                    }
                    reopenMenusForBothPlayers();
                }
            }, 0L, 20L).getTaskId();
        }
    }

    public void cancelCountdownTask() {
        if (countdownTaskId != -1) {
            Bukkit.getScheduler().cancelTask(countdownTaskId);
            countdownTaskId = -1;
        }
    }

    public void toggleConfirmation(Player player) {
        toggleConfirmation(player.getUniqueId());
    }

    public boolean bothConfirmed() {
        return senderConfirmed && targetConfirmed;
    }

    public List<ItemStack> getPlayerItems(UUID playerId) {
        if (playerId.equals(sender)) return senderItems;
        if (playerId.equals(target)) return targetItems;
        return Collections.emptyList();
    }

    public List<ItemStack> getPlayerItems(Player player) {
        return getPlayerItems(player.getUniqueId());
    }

    public List<ItemStack> getOtherPlayerItems(UUID playerId) {
        if (playerId.equals(sender)) return targetItems;
        if (playerId.equals(target)) return senderItems;
        return Collections.emptyList();
    }

    public List<ItemStack> getOtherPlayerItems(Player player) {
        return getOtherPlayerItems(player.getUniqueId());
    }

    public void toggleConfirmation(UUID playerId) {
        if (!canConfirm()) {
            return;
        }

        if (playerId.equals(sender)) {
            senderConfirmed = !senderConfirmed;
        } else if (playerId.equals(target)) {
            targetConfirmed = !targetConfirmed;
        }
    }

    public void resetConfirmation() {
        senderConfirmed = false;
        targetConfirmed = false;
    }

    public void cancel() {
        senderItems.clear();
        targetItems.clear();
        resetConfirmation();
        lastChangeTime = 0;
        cancelCountdownTask();
    }

    public boolean hasInventorySpace(Player player, List<ItemStack> items) {
        if (items.isEmpty()) return true;

        return player.getInventory().firstEmpty() != -1 ||
                player.getInventory().addItem(items.toArray(new ItemStack[0])).isEmpty();
    }

    public void giveItemsToPlayer(Player player, List<ItemStack> items) {
        if (!items.isEmpty()) {
            player.getInventory().addItem(items.toArray(new ItemStack[0]));
        }
    }

    public void removeItemsFromPlayer(Player player, List<ItemStack> items) {
        if (!items.isEmpty()) {
            player.getInventory().removeItem(items.toArray(new ItemStack[0]));
        }
    }

    public void reopenMenusForBothPlayers() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (senderPlayer != null) {
            new TradeMenu(this).openMenu(senderPlayer);
        }
        if (targetPlayer != null) {
            new TradeMenu(this).openMenu(targetPlayer);
        }
    }

    public boolean completeTrade() {
        Player senderPlayer = Bukkit.getPlayer(sender);
        Player targetPlayer = Bukkit.getPlayer(target);

        if (senderPlayer == null || targetPlayer == null) {
            handleOfflinePlayer(senderPlayer, targetPlayer);
            return false;
        }

        List<ItemStack> toSender = new ArrayList<>(targetItems);
        List<ItemStack> toTarget = new ArrayList<>(senderItems);

        if (!hasInventorySpace(senderPlayer, toSender) || !hasInventorySpace(targetPlayer, toTarget)) {
            handleInsufficientSpace(senderPlayer, targetPlayer, toSender, toTarget);
            return false;
        }

        executeTradeExchange(senderPlayer, targetPlayer, toSender, toTarget);
        return true;
    }

    private void handleOfflinePlayer(Player senderPlayer, Player targetPlayer) {
        String offlineMessage = CC.t("&cTrade cancelled, player went offline.");
        if (senderPlayer != null) senderPlayer.sendMessage(offlineMessage);
        if (targetPlayer != null) targetPlayer.sendMessage(offlineMessage);
        cancel();
    }

    private void handleInsufficientSpace(Player senderPlayer, Player targetPlayer,
                                         List<ItemStack> toSender, List<ItemStack> toTarget) {
        String spaceMessage = CC.t("&cTrade failed: someone needs more empty slots!");
        senderPlayer.sendMessage(spaceMessage);
        targetPlayer.sendMessage(spaceMessage);

        removeItemsFromPlayer(senderPlayer, toSender);
        removeItemsFromPlayer(targetPlayer, toTarget);

        reopenMenusForBothPlayers();
    }

    private void executeTradeExchange(Player senderPlayer, Player targetPlayer,
                                      List<ItemStack> toSender, List<ItemStack> toTarget) {
        Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
            giveItemsToPlayer(senderPlayer, toSender);
            giveItemsToPlayer(targetPlayer, toTarget);

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

    public Document toBson() {
        return new Document()
                .append("sender", sender.toString())
                .append("target", target.toString())
                .append("senderConfirmed", senderConfirmed)
                .append("targetConfirmed", targetConfirmed)
                .append("senderItems", senderItems.stream()
                        .map(ItemStackBsonUtil::serializeItemStack)
                        .collect(Collectors.toList()))
                .append("targetItems", targetItems.stream()
                        .map(ItemStackBsonUtil::serializeItemStack)
                        .collect(Collectors.toList()));
    }

    public static TradeSession fromBson(Document doc) {
        TradeSession session = new TradeSession(
                UUID.fromString(doc.getString("sender")),
                UUID.fromString(doc.getString("target"))
        );

        session.senderConfirmed = doc.getBoolean("senderConfirmed", false);
        session.targetConfirmed = doc.getBoolean("targetConfirmed", false);

        List<Document> senderDocs = doc.getList("senderItems", Document.class, Collections.emptyList());
        session.senderItems.addAll(
                senderDocs.stream()
                        .map(ItemStackBsonUtil::deserializeItemStack)
                        .collect(Collectors.toList())
        );

        List<Document> targetDocs = doc.getList("targetItems", Document.class, Collections.emptyList());
        session.targetItems.addAll(
                targetDocs.stream()
                        .map(ItemStackBsonUtil::deserializeItemStack)
                        .collect(Collectors.toList())
        );

        return session;
    }

}