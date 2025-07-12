package me.portmapping.trading.ui.admin.button;

import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class NoTradeHistoryButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.BARRIER)
                .setDisplayName(CC.t("&c&lNo Trade History"))
                .setLore(Arrays.asList(
                        CC.t("&7This player has no recorded trades."),
                        CC.t("&7Trades will appear here once completed.")
                ))
                .build();
    }
}
