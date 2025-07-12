package me.portmapping.trading.ui.admin.button;

import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.ui.admin.TradeHistoryMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class RefreshButton extends Button {

    private final TradeHistoryMenu menu;

    public RefreshButton(TradeHistoryMenu menu) {
        this.menu = menu;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.EMERALD)
                .setDisplayName(CC.t("&a&lRefresh"))
                .setLore(Arrays.asList(
                        CC.t("&7Click to refresh the trade history"),
                        CC.t("&7and load any new trades.")
                ))
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        menu.getTradeHistory().clear();
        menu.setLoading(true);
        menu.openMenu(player);
    }
}
