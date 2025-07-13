package me.portmapping.trading.utils.chat;

import me.portmapping.trading.Tausch;
import org.bukkit.ChatColor;

public class Language {


    public static String TRADE_FAILED_NO_SPACE;
    public static String TRADE_CANCELLED;
    public static String TRADE_COMPLETED;
    public static String ITEM_GIVEN_FORMAT;
    public static String ITEM_RECEIVED_FORMAT;

    public static String TRADE_USAGE;
    public static String TRADE_SELF;
    public static String TRADE_REQUEST_SENT;
    public static String TRADE_REQUEST_RECEIVED;
    public static String TRADE_REQUEST_FAILED;
    public static String TRADE_REQUEST_ACCEPTED_BY_TARGET;
    public static String TRADE_REQUEST_ACCEPTED;
    public static String TRADE_NO_PENDING_REQUEST;
    public static String TRADE_REQUEST_DECLINED;
    public static String TRADE_ITEMS_FULL;
    public static String INVENTORY_FULL;

    public static String TRADE_CLICK_ACCEPT;
    public static String TRADE_CLICK_DECLINE;
    public static String TRADE_CLICK_ACCEPT_HOVER;
    public static String TRADE_CLICK_DECLINE_HOVER;

    public static String TRADE_IGNORE_ON;
    public static String TRADE_IGNORE_OFF;
    public static String TRADE_TARGET_IGNORING;
    public static String TRADE_PROFILE_NOT_FOUND;

    public static void loadMessages() {
        Tausch plugin = Tausch.getInstance();
        TRADE_FAILED_NO_SPACE = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.failed_no_space", "&cTrade failed: someone needs more empty slots!"));
        TRADE_CANCELLED = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.trade_cancelled", "&cTrade cancelled."));
        TRADE_COMPLETED = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.trade_completed", "&a&l✓ Trade completed successfully!"));
        ITEM_GIVEN_FORMAT = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.item_given_format", "&c- &fx%amount% &7%item%"));
        ITEM_RECEIVED_FORMAT = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.item_received_format", "&a+ &fx%amount% &7%item%"));
        TRADE_USAGE = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.usage", "&eUsage: /trade <player>"));
        TRADE_SELF = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.self", "&cYou cannot trade with yourself."));
        TRADE_REQUEST_SENT = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.request_sent", "&aYou sent a trade request to %target%"));
        TRADE_REQUEST_RECEIVED = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.request_received", "&e%sender% has sent you a trade request. Use /trade accept or /trade decline."));
        TRADE_REQUEST_FAILED = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.request_failed", "&cTrade request could not be sent. Maybe the target already has a pending request."));
        TRADE_REQUEST_ACCEPTED_BY_TARGET = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.request_accepted_by_target", "&aYou accepted a trading request from %player%"));
        TRADE_REQUEST_ACCEPTED = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.request_accepted", "&a%player% accepted your trade request."));
        TRADE_NO_PENDING_REQUEST = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.no_pending_request", "&cYou don't have any pending trade request."));
        TRADE_REQUEST_DECLINED = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.request_declined", "&cYou declined the trade request."));
        TRADE_ITEMS_FULL = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.items_full", "&cYou cannot add more items."));
        INVENTORY_FULL = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.inventory_full", "&cYour inventory is full."));

        TRADE_CLICK_ACCEPT = CC.color(plugin.getMessagesConfig().getConfig().getString("hover.trade.accept", "&a✔ Click to accept %sender%'s trade request"));
        TRADE_CLICK_DECLINE = CC.color(plugin.getMessagesConfig().getConfig().getString("hover.trade.decline", "&c✖ Click to decline %sender%'s trade request"));
        TRADE_CLICK_ACCEPT_HOVER = CC.color(plugin.getMessagesConfig().getConfig().getString("hover.trade.accept_hover", "&7Accept %sender%'s trade request."));
        TRADE_CLICK_DECLINE_HOVER = CC.color(plugin.getMessagesConfig().getConfig().getString("hover.trade.decline_hover", "&7Decline %sender%'s trade request."));


        TRADE_IGNORE_ON = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.ignore_on", "&aYou are now ignoring trade requests."));
        TRADE_IGNORE_OFF = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.ignore_off", "&cYou are no longer ignoring trade requests."));
        TRADE_TARGET_IGNORING = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.target_ignoring", "&cThat player is ignoring trade requests."));
        TRADE_PROFILE_NOT_FOUND = CC.color(plugin.getMessagesConfig().getConfig().getString("messages.trade.profile_not_found", "&cProfile not found. Please relog or contact an admin."));
    }
}
