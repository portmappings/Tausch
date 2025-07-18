package me.portmapping.trading.utils.menu;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.utils.menu.pagination.PaginatedMenu;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

public class ButtonListener implements Listener {

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onButtonPress(InventoryClickEvent event) {
		Player player = (Player) event.getWhoClicked();
		Menu openMenu = Menu.currentlyOpenedMenus.get(player.getName());

		if (openMenu != null) {
			if (event.getSlot() != event.getRawSlot()) {
				if ((event.getClick() == ClickType.SHIFT_LEFT
						|| event.getClick() == ClickType.SHIFT_RIGHT)) {
					event.setCancelled(true);
				}

				return;
			}

			if (openMenu.getButtons().containsKey(event.getSlot())) {
				Button button = openMenu.getButtons().get(event.getSlot());
				boolean cancel = button.shouldCancel(player, event.getSlot(), event.getClick());

				if (!cancel && (event.getClick() == ClickType.SHIFT_LEFT
						|| event.getClick() == ClickType.SHIFT_RIGHT)) {
					event.setCancelled(true);
					if (event.getCurrentItem() != null) {
						player.getInventory().addItem(event.getCurrentItem());
					}
				} else {
					event.setCancelled(cancel);
				}

				button.clicked(player, event.getSlot(), event.getClick(), event.getHotbarButton());
				if (Menu.currentlyOpenedMenus.containsKey(player.getName())) {
					Menu newMenu = Menu.currentlyOpenedMenus.get(player.getName());
					if (newMenu == openMenu && openMenu.isUpdateAfterClick()) {
						openMenu.setClosedByMenu(true);
						newMenu.openMenu(player);
					}
				} else if (button.shouldUpdate(player, event.getSlot(), event.getClick())) {
					openMenu.setClosedByMenu(true);
					openMenu.openMenu(player);
				}

				if (event.isCancelled()) {
					Bukkit.getScheduler().runTaskLater(Tausch.getInstance(), player::updateInventory, 1L);
				}
			} else {
				if ((event.getClick() == ClickType.SHIFT_LEFT || event.getClick() == ClickType.SHIFT_RIGHT
						|| event.getAction().equals(InventoryAction.MOVE_TO_OTHER_INVENTORY)
						|| event.getAction().equals(InventoryAction.HOTBAR_MOVE_AND_READD) || event.getAction()
						.equals(InventoryAction.HOTBAR_SWAP))) {
					event.setCancelled(true);
				}
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void onInventoryClose(InventoryCloseEvent event) {
		Player player = (Player) event.getPlayer();
		Menu openMenu = Menu.currentlyOpenedMenus.get(player.getName());
		if (openMenu != null) {
			openMenu.onClose(player);
			Menu.currentlyOpenedMenus.remove(player.getName());
			if (openMenu instanceof PaginatedMenu) {
				return;
			}
		}

		player.setMetadata("scanglitch", new FixedMetadataValue(Tausch.getInstance(), true));
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		if (player.hasMetadata("scanglitch")) {
			player.removeMetadata("scanglitch", Tausch.getInstance());
			for (ItemStack it : player.getInventory().getContents()) {
				if (it != null) {
					ItemMeta meta = it.getItemMeta();
					if (meta != null && meta.hasDisplayName() && meta.getDisplayName().contains("§b§c§d§e")) {
						player.getInventory().remove(it);
					}
				}
			}
		}
	}
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onInventoryDrag(InventoryDragEvent event) {
		Player player = (Player) event.getWhoClicked();
		if (Menu.currentlyOpenedMenus.containsKey(player.getName())) {
			event.setCancelled(true);
		}
	}

}