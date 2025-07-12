package me.portmapping.trading.ui.admin.button;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.ui.admin.TradeHistoryMenu;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.config.ConfigCursor;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

public class RefreshButton extends Button {

    private static final ConfigCursor CURSOR = new ConfigCursor(Tausch.getInstance().getMenusConfig(), "refresh-button");

    private final TradeHistoryMenu menu;

    public RefreshButton(TradeHistoryMenu menu) {
        this.menu = menu;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        String matName = CURSOR.getString("material");
        Material material = matName == null ? Material.EMERALD : Material.valueOf(matName.toUpperCase());
        String display = CC.t(CURSOR.getString("display-name") == null ? "&a&lRefresh" : CURSOR.getString("display-name"));
        List<String> lore = CURSOR.getStringList("lore").stream().map(CC::t).collect(Collectors.toList());
        boolean glow = CURSOR.getBoolean("glow");
        ItemBuilder builder = new ItemBuilder(material).setDisplayName(display).setLore(lore);
        if (glow) builder.glowing(true);
        return builder.build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        menu.getTradeHistory().clear();
        menu.setLoading(true);
        menu.openMenu(player);
    }
}
