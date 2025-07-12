package me.portmapping.trading.commands.exceptions;

import org.jetbrains.annotations.NotNull;
import revxrsal.commands.bukkit.exception.BukkitExceptionAdapter;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.command.CommandActor;
import revxrsal.commands.exception.MissingArgumentException;
import revxrsal.commands.exception.NoPermissionException;

public class CommandExceptionHandler extends BukkitExceptionAdapter {

	@Override
	public void missingArgument(@NotNull CommandActor actor,
								@NotNull MissingArgumentException exception) {
		actor.error("&cYou are missing arguments.");
	}

	@Override
	public void invalidPlayer(@NotNull CommandActor actor,
							  @NotNull InvalidPlayerException exception) {
		actor.error("&cThe player you specified is not online.");
	}

	@Override
	public void noPermission(@NotNull CommandActor actor,
							 @NotNull NoPermissionException exception) {
		actor.error("&cYou do not have permissions to execute this command.");
	}
}
