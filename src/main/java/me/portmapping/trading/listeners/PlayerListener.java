package me.portmapping.trading.listeners;

import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.user.TradeMenu;
import me.portmapping.trading.utils.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (!TradeMenu.currentlyOpenedMenus.containsKey(player.getName())) return;

        TradeMenu tradeMenu = (TradeMenu) TradeMenu.currentlyOpenedMenus.get(player.getName());

        if (event.getSlot() == event.getRawSlot()) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        event.setCancelled(true);

        TradeSession session = tradeMenu.getTradeSession();

        // Use the addItem function from TradeSession
        if (!session.addItem(player.getUniqueId(), clickedItem.clone())) {
            player.sendMessage(CC.t("&cYou can't add more items to the trade!"));
            return;
        }

        player.getInventory().setItem(event.getSlot(), null);

        TradeMenu newTradeMenu = new TradeMenu(session);
        newTradeMenu.openMenu(player);
        Player other = Bukkit.getPlayer(session.getOther(player));
        if (other != null && other.isOnline()) {
            newTradeMenu.openMenu(other);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player quitter = event.getPlayer();

        if (!TradeMenu.currentlyOpenedMenus.containsKey(quitter.getName())) {
            return;
        }

        TradeMenu tradeMenu = (TradeMenu) TradeMenu.currentlyOpenedMenus.remove(quitter.getName());
        TradeSession session = tradeMenu.getTradeSession();
        session.cancelTrade();

        Player partner = Bukkit.getPlayer(session.getOther(quitter));
        if (partner != null && partner.isOnline()) {
            TradeMenu.currentlyOpenedMenus.remove(partner.getName());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (!TradeMenu.currentlyOpenedMenus.containsKey(player.getName())) {
            return;
        }

        TradeMenu tradeMenu = (TradeMenu) TradeMenu.currentlyOpenedMenus.get(player.getName());

        // Check if the menu was closed manually (not by the plugin)
        if (!tradeMenu.isClosedByMenu()) {
            // Player manually closed the menu (ESC key)
            TradeMenu.currentlyOpenedMenus.remove(player.getName());
            TradeSession session = tradeMenu.getTradeSession();
            session.cancelTrade();

            Player partner = Bukkit.getPlayer(session.getOther(player));
            if (partner != null && partner.isOnline()) {
                TradeMenu.currentlyOpenedMenus.remove(partner.getName());
                partner.closeInventory(); // Close the partner's menu too
                partner.sendMessage(CC.t("&cThe trade has been cancelled."));
            }

            player.sendMessage(CC.t("&cTrade cancelled."));
        }
    }
}