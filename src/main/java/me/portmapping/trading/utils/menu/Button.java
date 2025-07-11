package me.portmapping.trading.utils.menu;

import me.portmapping.trading.Tausch;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class Button {

	protected final Tausch plugin = Tausch.getInstance();

	public static Button placeholder(final Material material, String... title) {
		return (new Button() {
			public ItemStack getButtonItem(Player player) {
				ItemStack it = new ItemStack(material, 1);
				ItemMeta meta = it.getItemMeta();

				meta.setDisplayName(StringUtils.join(title));
				it.setItemMeta(meta);

				return it;
			}
		});
	}

	public static void playFail(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_GRASS_BREAK, 20F, 0.1F);
	}

	public static void playSuccess(Player player) {
		player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 20F, 15F);
	}

	public static void playNeutral(Player player) {
		player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 20F, 1F);
	}

	public abstract ItemStack getButtonItem(Player player);

	public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
	}

	public boolean shouldCancel(Player player, int slot, ClickType clickType) {
		return (true);
	}

	public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
		return (false);
	}

}