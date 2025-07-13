package me.portmapping.trading.manager;

import lombok.Getter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.Profile;
import me.portmapping.trading.model.TradeRequestResult;
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

    public TradeManager(Tausch instance) {
        this.instance = instance;
        new TradeRunnable().runTaskTimerAsynchronously(instance, 0L, 22L);
    }

    @Getter
    private final Map<UUID, TradeSession> activeTrades = new ConcurrentHashMap<>();

    private final Map<UUID, Deque<UUID>> pendingRequests = new ConcurrentHashMap<>();



    public TradeRequestResult sendTradeRequest(Player sender, Player target) {
        if (sender.equals(target)) return TradeRequestResult.SELF_REQUEST;

        Profile targetProfile = instance.getProfileManager().getProfile(target.getUniqueId());
        if (targetProfile != null && targetProfile.isIgnoreTrades()) {
            return TradeRequestResult.TARGET_IGNORING;
        }

        UUID senderId = sender.getUniqueId();
        UUID targetId = target.getUniqueId();

        Deque<UUID> senderRequests = pendingRequests.get(senderId);
        if (senderRequests != null && senderRequests.contains(targetId)) {
            senderRequests.remove(targetId);

            Deque<UUID> targetRequests = pendingRequests.get(targetId);
            if (targetRequests != null) {
                targetRequests.remove(senderId);
                if (targetRequests.isEmpty()) pendingRequests.remove(targetId);
            }
            if (senderRequests.isEmpty()) pendingRequests.remove(senderId);

            TradeSession session = new TradeSession(senderId, targetId);
            activeTrades.put(senderId, session);
            activeTrades.put(targetId, session);

            TradeMenu tradeMenu = new TradeMenu(session);
            tradeMenu.openMenu(sender);
            tradeMenu.openMenu(target);

            sender.sendMessage(Language.TRADE_REQUEST_ACCEPTED.replace("%player%", target.getName()));
            target.sendMessage(Language.TRADE_REQUEST_ACCEPTED_BY_TARGET.replace("%player%", sender.getName()));

            return TradeRequestResult.SUCCESS;
        }

        Deque<UUID> targetRequests = pendingRequests.get(targetId);
        if (targetRequests != null && targetRequests.contains(senderId)) {
            return TradeRequestResult.ALREADY_HAS_PENDING_REQUEST;
        }

        pendingRequests.computeIfAbsent(targetId, k -> new ArrayDeque<>()).addLast(senderId);

        return TradeRequestResult.SUCCESS;
    }

    public boolean acceptTradeRequest(Player target) {
        UUID targetId = target.getUniqueId();

        Deque<UUID> requests = pendingRequests.get(targetId);
        if (requests == null || requests.isEmpty()) return false;

        UUID senderId = requests.removeLast();

        if (requests.isEmpty()) {
            pendingRequests.remove(targetId);
        }

        Player sender = Bukkit.getPlayer(senderId);
        if (sender == null || !sender.isOnline()) return false;

        pendingRequests.values().forEach(queue -> queue.remove(senderId));
        pendingRequests.entrySet().removeIf(e -> e.getValue().isEmpty());

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

        if (pendingRequests.containsKey(playerId)) {
            pendingRequests.remove(playerId);
        }

        pendingRequests.values().forEach(queue -> queue.remove(playerId));
        pendingRequests.entrySet().removeIf(e -> e.getValue().isEmpty());

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
        return pendingRequests.containsKey(player.getUniqueId()) && !pendingRequests.get(player.getUniqueId()).isEmpty();
    }

    public TradeSession getSession(Player player) {
        return activeTrades.get(player.getUniqueId());
    }
}
