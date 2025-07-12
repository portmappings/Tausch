package me.portmapping.trading.ui.user.button;

import lombok.RequiredArgsConstructor;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;

@RequiredArgsConstructor
public class ClickableItemButton extends Button {
    private final TradeSession tradeSession;
    private final ItemStack item;
    private final int index;



    @Override
    public ItemStack getButtonItem(Player player) {
        return item;
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        if (player == null) return;

        List<ItemStack> items = tradeSession.getPlayerItems(player.getUniqueId());
        if (index >= items.size()) return;

        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(items.get(index));
            tradeSession.removeItem(player.getUniqueId(), index);
            tradeSession.reopenMenus();
        } else {
            player.sendMessage("§cYour inventory is full!");
        }
        return;
    }
}