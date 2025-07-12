package me.portmapping.trading.manager;

import lombok.Getter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.task.TradeRunnable;
import me.portmapping.trading.ui.user.TradeMenu;
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

    public boolean sendTradeRequest(Player sender, Player target) {
        if (sender.equals(target)) return false;
        if (hasPendingRequest(target)) return false;

        pendingRequests.put(target.getUniqueId(), sender.getUniqueId());
        return true;
    }

    public boolean acceptTradeRequest(Player target) {
        UUID senderId = pendingRequests.remove(target.getUniqueId());
        if (senderId == null) return false;

        Player sender = Bukkit.getPlayer(senderId);
        if (sender == null || !sender.isOnline()) return false;

        TradeSession session = new TradeSession(sender.getUniqueId(), target.getUniqueId());
        activeTrades.put(sender.getUniqueId(), session);
        activeTrades.put(target.getUniqueId(), session);

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
            session.cancel();
            activeTrades.remove(session.getOther(player));
        }
    }

    public boolean hasPendingRequest(Player player) {
        return pendingRequests.containsKey(player.getUniqueId());
    }

    public TradeSession getSession(Player player) {
        return activeTrades.get(player.getUniqueId());
    }
}
