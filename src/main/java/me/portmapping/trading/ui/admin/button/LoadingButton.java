package me.portmapping.trading.ui.admin.button;

import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;

public class LoadingButton extends Button {

    @Override
    public ItemStack getButtonItem(Player player) {
        return new ItemBuilder(Material.HOPPER)
                .setDisplayName(CC.t("&6&lLoading..."))
                .setLore(Arrays.asList(
                        CC.t("&7Fetching trade history from database..."),
                        CC.t("&7Please wait a moment.")
                ))
                .build();
    }
}
