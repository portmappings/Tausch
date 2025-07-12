package me.portmapping.trading.ui.admin.button;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.admin.TradeViewMenu;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.config.ConfigCursor;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class TradeHistoryButton extends Button {

    private static final ConfigCursor configCursor = new ConfigCursor(Tausch.getInstance().getMenusConfig(), "trade-history-button");

    private final TradeSession tradeSession;
    private final UUID viewingPlayerId;

    public TradeHistoryButton(TradeSession tradeSession, UUID viewingPlayerId) {
        this.tradeSession = tradeSession;
        this.viewingPlayerId = viewingPlayerId;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        UUID otherPlayerId = tradeSession.getOther(viewingPlayerId);
        Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
        String otherName = otherPlayer != null ? otherPlayer.getName() : "Unknown";
        boolean isViewer = viewingPlayerId.equals(player.getUniqueId());
        List<ItemStack> viewerItems = tradeSession.getPlayerItems(viewingPlayerId);
        List<ItemStack> otherItems = tradeSession.getOtherPlayerItems(viewingPlayerId);
        String matName = configCursor.getString("material");
        Material material = matName == null ? Material.CHEST : Material.valueOf(matName.toUpperCase());
        String displayTemplate = configCursor.getString("display-name") == null ? "&6&lTrade with {other}" : configCursor.getString("display-name");
        String display = CC.t(displayTemplate.replace("{other}", otherName));
        List<String> loreTemplates = configCursor.getStringList("lore");
        String viewerCount = String.valueOf(viewerItems.size());
        String otherCount = String.valueOf(otherItems.size());
        List<String> lore = loreTemplates.stream()
                .map(s -> s.replace("{other}", otherName)
                        .replace("{viewer_items}", viewerCount)
                        .replace("{other_items}", otherCount))
                .map(CC::t)
                .collect(Collectors.toList());
        boolean glow = configCursor.getBoolean("glow");
        ItemBuilder builder = new ItemBuilder(material).setDisplayName(display).setLore(lore);
        if (glow) builder.glowing(true);
        return builder.build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        new TradeViewMenu(tradeSession, viewingPlayerId).openMenu(player);
    }
}
