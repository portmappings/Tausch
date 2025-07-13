package me.portmapping.trading.manager;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.commands.admin.TradeHistoryCommand;
import me.portmapping.trading.commands.exceptions.CommandExceptionHandler;
import me.portmapping.trading.commands.user.TradeCommand;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.util.stream.Collectors;

public class CommandManager {

	private final Tausch plugin;
	private final BukkitCommandHandler handler;

	public CommandManager(Tausch plugin) {
		this.plugin = plugin;

		handler = BukkitCommandHandler.create(plugin);
		handler.registerDependency(Tausch.class, plugin);
		handler.setExceptionHandler(new CommandExceptionHandler());

		handler.getAutoCompleter().registerParameterSuggestions(Player.class, (args, sender, command) -> plugin.getServer().getOnlinePlayers()
				.stream()
				.map(Player::getName)
				.collect(Collectors.toList())
		);

		register();
	}

	private void register() {
		handler.register(new Tausch());
		handler.register(new TradeCommand());
		handler.register(new TradeHistoryCommand());

	}
}
