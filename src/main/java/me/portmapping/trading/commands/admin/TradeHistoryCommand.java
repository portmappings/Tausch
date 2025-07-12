package me.portmapping.trading.commands.admin;

import me.portmapping.trading.ui.admin.TradeHistoryMenu;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.Named;
import revxrsal.commands.bukkit.annotation.CommandPermission;

public class TradeHistoryCommand {
    @Command("tradehistory")
    @CommandPermission("tausch.admin")
    public void onTradeHistoryCommand(Player self, @Named("target") OfflinePlayer target){
        new TradeHistoryMenu(target.getUniqueId(), target.getName()).openMenu(self);
    }
}