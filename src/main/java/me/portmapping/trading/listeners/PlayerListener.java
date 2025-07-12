package me.portmapping.trading.listeners;

import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.user.TradeMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        if (event.getClick() != ClickType.SHIFT_LEFT && event.getClick() != ClickType.SHIFT_RIGHT) return;
        if (!TradeMenu.currentlyOpenedMenus.containsKey(player.getName())) return;

        TradeMenu tradeMenu = (TradeMenu) TradeMenu.currentlyOpenedMenus.get(player.getName());

        // Ignore clicks inside the trade GUI
        if (event.getSlot() == event.getRawSlot()) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().isAir()) return;

        event.setCancelled(true); // Prevent item from actually moving

        TradeSession session = tradeMenu.getTradeSession();
        boolean isSender = player.getUniqueId().equals(session.getSender());

        if (isSender) {
            session.getSenderItems().add(clickedItem.clone());
        } else {
            session.getTargetItems().add(clickedItem.clone());
        }

        session.resetConfirmation();

        player.getInventory().setItem(event.getSlot(), null);

        tradeMenu.openMenu(player); // Update self
        Player other = Bukkit.getPlayer(session.getOther(player));
        if (other != null && other.isOnline()) {
            tradeMenu.openMenu(other); // Update other player
        }
    }
}
