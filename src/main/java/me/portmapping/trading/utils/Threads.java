package me.portmapping.trading.utils;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import me.portmapping.trading.Tausch;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@UtilityClass
public final class Threads {

	private final Tausch plugin = Tausch.getInstance();
	private final BukkitScheduler scheduler = Bukkit.getScheduler();

	@Getter
	private ExecutorService databaseExecutor;
	private ScheduledExecutorService scheduledExecutor;

	private boolean shutdown = false;

	public void init() {
		final int databaseThreads = Math.max(Bukkit.getMaxPlayers() / 30, 1);
		databaseExecutor = Executors.newFixedThreadPool(databaseThreads,
				new ThreadFactoryBuilder().setNameFormat("Frost Database Thread %d").build());
		scheduledExecutor = Executors.newScheduledThreadPool(4,
				new ThreadFactoryBuilder().setNameFormat("Frost Scheduler Thread %d").build());
	}

	public void ensureMain(Runnable runnable) {
		if (shutdown) {
			return;
		}

		if (!Bukkit.isPrimaryThread()) {
			Threads.sync(runnable);
		} else {
			runnable.run();
		}
	}

	public void sync(Runnable runnable) {
		if (shutdown) {
			return;
		}

		scheduler.runTask(plugin, runnable);
	}

	public void sync(Runnable runnable, long delay) {
		if (shutdown) {
			return;
		}

		scheduler.runTaskLater(plugin, runnable, delay);
	}

	public void sync(Runnable runnable, long delay, long period) {
		if (shutdown) {
			return;
		}

		scheduler.runTaskTimer(plugin, runnable, delay, period);
	}

	public void executeData(Runnable runnable) {
		if (shutdown) {
			return;
		}

		databaseExecutor.execute(runnable);
	}

	public void scheduleData(Runnable runnable, long delay) {
		if (shutdown) {
			return;
		}

		scheduledExecutor.schedule(runnable, delay, TimeUnit.SECONDS);
	}

	public void scheduleData(Runnable runnable, long delay, long period) {
		if (shutdown) {
			return;
		}

		scheduledExecutor.scheduleAtFixedRate(runnable, delay, period, TimeUnit.SECONDS);
	}

	public void async(Runnable runnable) {
		if (shutdown) {
			return;
		}

		scheduler.runTaskAsynchronously(plugin, runnable);
	}

	public void async(Runnable runnable, long delay) {
		if (shutdown) {
			return;
		}

		scheduler.runTaskLaterAsynchronously(plugin, runnable, delay);
	}

	public void async(Runnable runnable, long delay, long period) {
		if (shutdown) {
			return;
		}

		scheduler.runTaskTimerAsynchronously(plugin, runnable, delay, period);
	}

	public void close() {
		try {
			shutdown = true;
			plugin.getLogger().info("Shutting down database executor now...");
			Threads.shutdownAndAwaitTermination();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void shutdownAndAwaitTermination() {
		databaseExecutor.shutdown();
		try {
			if (databaseExecutor.awaitTermination(60L, TimeUnit.SECONDS)) {
				databaseExecutor.shutdownNow();
				if (!databaseExecutor.awaitTermination(60L, TimeUnit.SECONDS)) {
					plugin.getLogger().severe("Pool did not terminate!");
				}
			}
		} catch (InterruptedException ie) {
			databaseExecutor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}
}

