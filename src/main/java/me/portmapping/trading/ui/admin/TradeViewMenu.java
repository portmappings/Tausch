package me.portmapping.trading.ui.admin;

import lombok.RequiredArgsConstructor;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.admin.button.TradeHistoryButton;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.config.ConfigCursor;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TradeViewMenu extends Menu {

    private static final ConfigCursor CURSOR = new ConfigCursor(Tausch.getInstance().getMenusConfig(), "trade-view");
    private static final ConfigCursor BACK_CURSOR = new ConfigCursor(Tausch.getInstance().getMenusConfig(), "trade-view.back-button");
    private static final ConfigCursor INFO_CURSOR = new ConfigCursor(Tausch.getInstance().getMenusConfig(), "trade-view.info-button");

    private final TradeSession tradeSession;
    private final UUID viewingPlayerId;

    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40, 49};
    private static final int BACK_BUTTON_SLOT = 45;
    private static final int INFO_BUTTON_SLOT = 53;

    private static final List<Integer> LEFT_SLOTS = Arrays.asList(0, 1, 2, 3, 9, 10, 11, 12, 18, 19, 20, 21, 27, 28, 29, 30, 36, 37, 38, 39);
    private static final List<Integer> RIGHT_SLOTS = Arrays.asList(5, 6, 7, 8, 14, 15, 16, 17, 23, 24, 25, 26, 32, 33, 34, 35, 41, 42, 43, 44);

    @Override
    public String getTitle(Player player) {
        UUID otherPlayerId = tradeSession.getOther(viewingPlayerId);
        Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
        String otherName = otherPlayer != null ? otherPlayer.getName() : "Unknown";
        String template = CURSOR.getString("title") == null ? "&8Trade View: &7{other}" : CURSOR.getString("title");
        return CC.t(template.replace("{other}", otherName));
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();
        addDividerButtons(buttons);
        addItemButtons(buttons);
        addBackButton(buttons);
        addInfoButton(buttons);
        addBorderButtons(buttons);
        return buttons;
    }

    private void addDividerButtons(Map<Integer, Button> buttons) {
        String matName = CURSOR.getString("divider-material");
        Material dividerMaterial = matName == null ? Material.GRAY_STAINED_GLASS_PANE : Material.valueOf(matName.toUpperCase());
        for (int slot : DIVIDER_SLOTS) {
            buttons.put(slot, Button.placeholder(dividerMaterial));
        }
    }

    private void addItemButtons(Map<Integer, Button> buttons) {
        List<ItemStack> viewerItems = tradeSession.getPlayerItems(viewingPlayerId);
        List<ItemStack> otherItems = tradeSession.getOtherPlayerItems(viewingPlayerId);
        for (int i = 0; i < viewerItems.size() && i < LEFT_SLOTS.size(); i++) {
            ItemStack item = viewerItems.get(i);
            buttons.put(LEFT_SLOTS.get(i), new StaticTradeItemButton(item, true, viewingPlayerId, tradeSession));
        }
        for (int i = 0; i < otherItems.size() && i < RIGHT_SLOTS.size(); i++) {
            ItemStack item = otherItems.get(i);
            buttons.put(RIGHT_SLOTS.get(i), new StaticTradeItemButton(item, false, viewingPlayerId, tradeSession));
        }
    }

    private void addBackButton(Map<Integer, Button> buttons) {
        String matName = BACK_CURSOR.getString("material");
        Material material = matName == null ? Material.ARROW : Material.valueOf(matName.toUpperCase());
        String display = CC.t(BACK_CURSOR.getString("display-name") == null ? "&c&lBack" : BACK_CURSOR.getString("display-name"));
        List<String> lore = BACK_CURSOR.getStringList("lore").stream().map(CC::t).collect(Collectors.toList());
        boolean glow = BACK_CURSOR.getBoolean("glow");
        buttons.put(BACK_BUTTON_SLOT, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemBuilder builder = new ItemBuilder(material).setDisplayName(display).setLore(lore);
                if (glow) builder.glowing(true);
                return builder.build();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                new TradeHistoryMenu(viewingPlayerId).openMenu(player);
            }
        });
    }

    private void addInfoButton(Map<Integer, Button> buttons) {
        UUID otherPlayerId = tradeSession.getOther(viewingPlayerId);
        Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
        String otherName = otherPlayer != null ? otherPlayer.getName() : "Unknown";
        Player viewingPlayer = Bukkit.getPlayer(viewingPlayerId);
        String viewingName = viewingPlayer != null ? viewingPlayer.getName() : "Unknown";
        String matName = INFO_CURSOR.getString("material");
        Material material = matName == null ? Material.BOOK : Material.valueOf(matName.toUpperCase());
        String display = CC.t(INFO_CURSOR.getString("display-name") == null ? "&6&lTrade Information" : INFO_CURSOR.getString("display-name"));
        List<String> loreTemplates = INFO_CURSOR.getStringList("lore");
        String viewerItemsCount = String.valueOf(tradeSession.getPlayerItems(viewingPlayerId).size());
        String otherItemsCount = String.valueOf(tradeSession.getOtherPlayerItems(viewingPlayerId).size());
        List<String> lore = new ArrayList<>();
        for (String line : loreTemplates) {
            lore.add(CC.t(line.replace("{viewer}", viewingName)
                    .replace("{other}", otherName)
                    .replace("{viewer_items}", viewerItemsCount)
                    .replace("{other_items}", otherItemsCount)));
        }
        boolean glow = INFO_CURSOR.getBoolean("glow");
        buttons.put(INFO_BUTTON_SLOT, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemBuilder builder = new ItemBuilder(material).setDisplayName(display).setLore(lore);
                if (glow) builder.glowing(true);
                return builder.build();
            }
        });
    }

    private void addBorderButtons(Map<Integer, Button> buttons) {
        String matName = CURSOR.getString("border-material");
        Material borderMaterial = matName == null ? Material.GRAY_STAINED_GLASS_PANE : Material.valueOf(matName.toUpperCase());
        for (int i = 45; i < 54; i++) {
            if (i != BACK_BUTTON_SLOT && i != INFO_BUTTON_SLOT) buttons.put(i, Button.placeholder(borderMaterial));
        }
    }

    @Override
    public int getSize() {
        return 54;
    }

    private static class StaticTradeItemButton extends Button {

        private final ItemStack item;
        private final boolean isViewerItem;
        private final UUID viewingPlayerId;
        private final TradeSession tradeSession;

        public StaticTradeItemButton(ItemStack item, boolean isViewerItem, UUID viewingPlayerId, TradeSession tradeSession) {
            this.item = item;
            this.isViewerItem = isViewerItem;
            this.viewingPlayerId = viewingPlayerId;
            this.tradeSession = tradeSession;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            if (item == null || item.getType() == Material.AIR) return new ItemStack(Material.AIR);
            String offeredBy;
            if (isViewerItem) offeredBy = player.getName();
            else {
                UUID otherId = tradeSession.getOther(viewingPlayerId);
                Player other = Bukkit.getPlayer(otherId);
                offeredBy = other != null ? other.getName() : "Unknown";
            }
            return new ItemBuilder(item.clone()).addToLore("").addToLore("&7Offered by: &f" + offeredBy).build();
        }
    }
}
