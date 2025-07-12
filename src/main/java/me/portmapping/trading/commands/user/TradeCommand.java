package me.portmapping.trading.commands.user;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.manager.TradeManager;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.*;

@Command("trade")
public class TradeCommand {
    @Dependency
    private Tausch instance;

    @DefaultFor("trade")
    public void tradeCommand(Player player, @Optional @Named("target") Player target) {
        if (target == null) {
            // Send help message
            CC.sendMessage(player, "&eUsage: /trade <player>");
            return;
        }

        if (target.equals(player)) {
            CC.sendMessage(player, "&cYou cannot trade with yourself.");
            return;
        }

        boolean sent = instance.getTradeManager().sendTradeRequest(player, target);
        if (sent) {
            CC.sendMessage(player, "&aYou sent a trade request to " + target.getName());
            CC.sendMessage(target, "&e" + player.getName() + " has sent you a trade request. Use /trade accept or /trade decline.");
        } else {
            CC.sendMessage(player, "&cTrade request could not be sent. Maybe the target already has a pending request.");
        }
    }

    @Subcommand("accept")
    public void tradeAcceptCommand(Player player){
        boolean hasAcceptedRequest = instance.getTradeManager().acceptTradeRequest(player);

        if (hasAcceptedRequest){
            TradeSession tradeSession = instance.getTradeManager().getSession(player);
            if (tradeSession != null) {

                Player target = Bukkit.getPlayer(tradeSession.getTarget());
                Player sender = Bukkit.getPlayer(tradeSession.getSender());
                CC.sendMessage(target, "&aYou accepted a trading request from " + sender.getName());
                CC.sendMessage(sender, "&a" + player.getName() + " accepted your trade request.");
            }
        } else {
            CC.sendMessage(player, "&cYou don't have any pending trade request.");
        }
    }

    @Subcommand("decline")
    public void declineAcceptCommand(Player player){
        if (!instance.getTradeManager().hasPendingRequest(player)){
            CC.sendMessage(player, "&cYou don't have a pending trade request.");
            return;
        }
        instance.getTradeManager().declineTrade(player);
        CC.sendMessage(player, "&cYou declined the trade request.");
    }
}
