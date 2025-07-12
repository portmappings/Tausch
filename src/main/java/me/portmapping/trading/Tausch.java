package me.portmapping.trading;

import lombok.Getter;
import me.portmapping.trading.listeners.PlayerListener;
import me.portmapping.trading.manager.CommandManager;
import me.portmapping.trading.manager.TradeManager;
import me.portmapping.trading.utils.menu.ButtonListener;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class Tausch extends JavaPlugin {
    @Getter
    private static Tausch instance;
    private CommandManager commandManager;
    private TradeManager tradeManager;

    @Override
    public void onEnable() {
        instance = this;

        this.commandManager = new CommandManager(this);
        this.tradeManager = new TradeManager(this);

        this.getServer().getPluginManager().registerEvents(new ButtonListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
