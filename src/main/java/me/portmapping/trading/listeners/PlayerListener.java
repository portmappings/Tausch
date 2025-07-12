package me.portmapping.trading.listeners;

import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.user.TradeMenu;
import me.portmapping.trading.utils.chat.CC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    private TradeMenu getActiveTradeMenu(Player p) {
        Object menu = TradeMenu.currentlyOpenedMenus.get(p.getName());
        return (menu instanceof TradeMenu tm) ? tm : null;
    }

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        TradeMenu tradeMenu = getActiveTradeMenu(player);
        if (tradeMenu == null) return;

        if (event.getSlot() == event.getRawSlot()) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        event.setCancelled(true);

        TradeSession session = tradeMenu.getTradeSession();

        if (session.isCompleted()) {
            return;
        }

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

        TradeMenu tradeMenu = getActiveTradeMenu(quitter);
        if (tradeMenu == null) return;

        TradeSession session = tradeMenu.getTradeSession();

        if (session.isCompleted()) {
            TradeMenu.currentlyOpenedMenus.remove(quitter.getName());
            return;
        }

        session.cancelTrade();
        TradeMenu.currentlyOpenedMenus.remove(quitter.getName());

        Player partner = Bukkit.getPlayer(session.getOther(quitter));
        if (partner != null && partner.isOnline()) {
            TradeMenu.currentlyOpenedMenus.remove(partner.getName());
            partner.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        TradeMenu tradeMenu = getActiveTradeMenu(player);
        if (tradeMenu == null) return;

        TradeSession session = tradeMenu.getTradeSession();

        if (session.isCompleted() || tradeMenu.isClosedByMenu()) {
            if (!session.isCompleted()) {
                TradeMenu.currentlyOpenedMenus.remove(player.getName());
            }
            return;
        }

        if (session.bothConfirmed()) {
            return;
        }

        TradeMenu.currentlyOpenedMenus.remove(player.getName());
        session.cancelTrade();

        Player partner = Bukkit.getPlayer(session.getOther(player));
        if (partner != null && partner.isOnline()) {
            TradeMenu partnerMenu = getActiveTradeMenu(partner);
            if (partnerMenu != null) {
                TradeMenu.currentlyOpenedMenus.remove(partner.getName());
                partner.closeInventory();
            }
        }
    }
}