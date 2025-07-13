package me.portmapping.trading.manager;

import lombok.Getter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.Profile;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.task.TradeRunnable;
import me.portmapping.trading.ui.user.TradeMenu;
import me.portmapping.trading.utils.chat.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TradeManager {
    private final Tausch instance;

    public TradeManager(Tausch instance){
        this.instance = instance;
        new TradeRunnable().runTaskTimerAsynchronously(instance, 0L, 22L);
    }

    @Getter
    private final Map<UUID, TradeSession> activeTrades = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> pendingRequests = new ConcurrentHashMap<>();

    public enum TradeRequestResult {
        SUCCESS,
        TARGET_IGNORING,
        ALREADY_HAS_PENDING_REQUEST,
        SELF_REQUEST
    }

    public TradeRequestResult sendTradeRequest(Player sender, Player target) {
        if (sender.equals(target)) return TradeRequestResult.SELF_REQUEST;
        if (hasPendingRequest(target)) return TradeRequestResult.ALREADY_HAS_PENDING_REQUEST;

        Profile targetProfile = instance.getProfileManager().getProfile(target.getUniqueId());
        if (targetProfile != null && targetProfile.isIgnoreTrades()) {
            return TradeRequestResult.TARGET_IGNORING;
        }

        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());
        return TradeRequestResult.SUCCESS;
    }

    public boolean acceptTradeRequest(Player target) {
        UUID targetId = target.getUniqueId();

        // Get and remove the sender of the pending request to this target
        UUID senderId = pendingRequests.remove(targetId);
        if (senderId == null) return false;

        Player sender = Bukkit.getPlayer(senderId);
        if (sender == null || !sender.isOnline()) return false;

        // Clear all other pending requests involving these two players:
        pendingRequests.entrySet().removeIf(entry -> entry.getValue().equals(senderId));

        //Remove all pending requests sent TO the target (should be none since we removed the one above, but just to be safe)
        pendingRequests.entrySet().removeIf(entry -> entry.getKey().equals(targetId));

        // Create new trade session and add to active trades
        TradeSession session = new TradeSession(senderId, targetId);
        activeTrades.put(senderId, session);
        activeTrades.put(targetId, session);

        TradeMenu tradeMenu = new TradeMenu(session);
        tradeMenu.openMenu(target);
        tradeMenu.openMenu(sender);
        return true;
    }


    public void declineTrade(Player player) {
        UUID playerId = player.getUniqueId();

        pendingRequests.entrySet().removeIf(entry ->
                entry.getKey().equals(playerId) || entry.getValue().equals(playerId));

        TradeSession session = activeTrades.remove(playerId);
        if (session != null) {
            session.cancelTrade();
            activeTrades.remove(session.getOther(player));
        }
    }

    public TradeSession getActiveSession(Player player) {
        return activeTrades.get(player.getUniqueId());
    }

    public boolean hasPendingRequest(Player player) {
        return pendingRequests.containsKey(player.getUniqueId());
    }

    public TradeSession getSession(Player player) {
        return activeTrades.get(player.getUniqueId());
    }
}
