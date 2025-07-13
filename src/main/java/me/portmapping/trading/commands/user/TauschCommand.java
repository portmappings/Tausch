package me.portmapping.trading.commands.user;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.chat.Language;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Description;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

@Command("tausch")
@Description("Shows plugin info or reloads its config/messages.")
public class TauschCommand {

    private final Tausch plugin = Tausch.getInstance();

    @DefaultFor("tausch")
    public void info(CommandSender sender) {
        PluginDescriptionFile d = plugin.getDescription();

        sender.sendMessage(CC.t("&8&m--------------------------------------------------"));
        sender.sendMessage(CC.t("&b&lTausch"));
        sender.sendMessage("");
        sender.sendMessage(CC.t("&eVersion: &f" + d.getVersion()));
        sender.sendMessage(CC.t("&eAuthor: &f" + String.join(", ", d.getAuthors())));
        sender.sendMessage("");
        sender.sendMessage(CC.t("&8&m--------------------------------------------------"));
    }

    @Subcommand("reload")
    @CommandPermission("tausch.admin")      // add a simple permission node
    public void reload(CommandSender sender) {
        plugin.getMenusConfig().reload();
        plugin.getMessagesConfig().reload();
        plugin.getSettingsConfig().reload();
        Language.loadMessages();
        sender.sendMessage(CC.t("&aTausch configuration & messages reloaded successfully!"));
    }
}
