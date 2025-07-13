package me.portmapping.trading.commands.user;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.manager.TradeManager;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.chat.Clickable;
import me.portmapping.trading.utils.chat.Language;
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
            player.sendMessage(Language.TRADE_USAGE);
            return;
        }

        if (target.equals(player)) {
            player.sendMessage(Language.TRADE_SELF);
            return;
        }

        boolean sent = instance.getTradeManager().sendTradeRequest(player, target);
        if (!sent) {
            player.sendMessage(Language.TRADE_REQUEST_FAILED);
            return;
        }

        player.sendMessage(Language.TRADE_REQUEST_SENT.replace("%target%", target.getName()));

        Clickable prompt = new Clickable("");
        prompt.add(
                Language.TRADE_CLICK_ACCEPT.replace("%sender%", player.getName()),
                Language.TRADE_CLICK_ACCEPT_HOVER.replace("%sender%", player.getName()),
                "/trade accept"
        );
        prompt.add(CC.t(" &7| "));
        prompt.add(
                Language.TRADE_CLICK_DECLINE.replace("%sender%", player.getName()),
                Language.TRADE_CLICK_DECLINE_HOVER.replace("%sender%", player.getName()),
                "/trade decline"
        );
        prompt.sendToPlayer(target);
    }

    @Subcommand("accept")
    public void tradeAcceptCommand(Player player) {
        boolean hasAcceptedRequest = instance.getTradeManager().acceptTradeRequest(player);

        if (hasAcceptedRequest) {
            TradeSession tradeSession = instance.getTradeManager().getSession(player);
            if (tradeSession != null) {
                Player target = Bukkit.getPlayer(tradeSession.getTarget());
                Player sender = Bukkit.getPlayer(tradeSession.getSender());
                if (target != null && sender != null) {
                    target.sendMessage(Language.TRADE_REQUEST_ACCEPTED_BY_TARGET.replace("%player%", sender.getName()));
                    sender.sendMessage(Language.TRADE_REQUEST_ACCEPTED.replace("%player%", player.getName()));
                }
            }
        } else {
            player.sendMessage(Language.TRADE_NO_PENDING_REQUEST);
        }
    }

    @Subcommand("decline")
    public void declineAcceptCommand(Player player) {
        if (!instance.getTradeManager().hasPendingRequest(player)) {
            player.sendMessage(Language.TRADE_NO_PENDING_REQUEST);
            return;
        }
        instance.getTradeManager().declineTrade(player);
        player.sendMessage(Language.TRADE_REQUEST_DECLINED);
    }
}
