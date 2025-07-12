package me.portmapping.trading.utils.item;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryUtil {
    
    public static boolean hasInventorySpace(Player player, List<ItemStack> items) {
        if (items.isEmpty()) return true;
        return player.getInventory().firstEmpty() != -1 ||
                player.getInventory().addItem(items.toArray(new ItemStack[0])).isEmpty();
    }
}