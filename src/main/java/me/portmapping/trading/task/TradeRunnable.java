package me.portmapping.trading.task;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;


//Pased from 0.08% server thread usage to "0.00%" using spark
public class TradeRunnable extends BukkitRunnable {

    @Override
    public void run() {
        Set<TradeSession> processedSessions = new HashSet<>();

        for (TradeSession session : Tausch.getInstance().getTradeManager().getActiveTrades().values()) {
            if (!processedSessions.add(session)) continue;

            int countdown = session.getCountdown();
            if (countdown > 1) {
                session.setCountdown(countdown - 1);

                Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> session.reopenMenus());
            }
        }
    }
}
