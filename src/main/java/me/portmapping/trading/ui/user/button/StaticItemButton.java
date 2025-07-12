package me.portmapping.trading.ui.user.button;

import me.portmapping.trading.utils.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class StaticItemButton extends Button {
        private final ItemStack item;

        public StaticItemButton(ItemStack item) {
            this.item = item.clone();
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return item;
        }
    }