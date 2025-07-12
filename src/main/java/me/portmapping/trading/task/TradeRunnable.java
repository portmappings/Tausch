package me.portmapping.trading.task;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;


//Pased from
public class TradeRunnable extends BukkitRunnable {

    @Override
    public void run() {
        Set<TradeSession> processedSessions = new HashSet<>();

        // Avoid creating lambda inside the loop every tick
        for (TradeSession session : Tausch.getInstance().getTradeManager().getActiveTrades().values()) {
            if (!processedSessions.add(session)) continue;

            int countdown = session.getCountdown();
            if (countdown > 1) {
                session.setCountdown(countdown - 1);

                // Schedule the expensive menu update off the main logic tick
                Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> session.reopenMenus());
            }
        }
    }
}
