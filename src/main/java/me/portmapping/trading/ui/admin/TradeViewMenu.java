package me.portmapping.trading.ui.admin;

import lombok.RequiredArgsConstructor;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@RequiredArgsConstructor
public class TradeViewMenu extends Menu {

    private final TradeSession tradeSession;
    private final UUID viewingPlayerId;

    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40, 49};
    private static final int BACK_BUTTON_SLOT = 45;
    private static final int INFO_BUTTON_SLOT = 53;

    private static final List<Integer> LEFT_SLOTS = Arrays.asList(
            0, 1, 2, 3,
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30,
            36, 37, 38, 39
    );

    private static final List<Integer> RIGHT_SLOTS = Arrays.asList(
            5, 6, 7, 8,
            14, 15, 16, 17,
            23, 24, 25, 26,
            32, 33, 34, 35,
            41, 42, 43, 44
    );

    @Override
    public String getTitle(Player player) {
        UUID otherPlayerId = tradeSession.getOther(viewingPlayerId);
        Player otherPlayer = Bukkit.getPlayer(otherPlayerId);
        String otherName = otherPlayer != null ? otherPlayer.getName() : "Unknown";
        return CC.t("&8Trade View: &7" + otherName);
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
        ItemStack divider = new ItemBuilder(Material.PURPLE_STAINED_GLASS_PANE)
                .setDisplayName(CC.t("&5&l⬌ &d&lTrade Divider &5&l⬌"))
                .setLore(Arrays.asList(
                        CC.t("&7This divides the items that"),
                        CC.t("&7were traded between players.")
                ))
                .build();

        for (int slot : DIVIDER_SLOTS) {
            buttons.put(slot, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return divider;
                }
            });
        }
    }

    private void addItemButtons(Map<Integer, Button> buttons) {
        List<ItemStack> viewerItems = tradeSession.getPlayerItems(viewingPlayerId);
        List<ItemStack> otherItems = tradeSession.getOtherPlayerItems(viewingPlayerId);

        // Add viewer's items (left side)
        for (int i = 0; i < viewerItems.size() && i < LEFT_SLOTS.size(); i++) {
            ItemStack item = viewerItems.get(i);
            buttons.put(LEFT_SLOTS.get(i), new StaticTradeItemButton(item, true));
        }

        // Add other player's items (right side)
        for (int i = 0; i < otherItems.size() && i < RIGHT_SLOTS.size(); i++) {
            ItemStack item = otherItems.get(i);
            buttons.put(RIGHT_SLOTS.get(i), new StaticTradeItemButton(item, false));
        }
    }

    private void addBackButton(Map<Integer, Button> buttons) {
        buttons.put(BACK_BUTTON_SLOT, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.ARROW)
                        .setDisplayName(CC.t("&c&lBack"))
                        .setLore(Arrays.asList(
                                CC.t("&7Click to return to the"),
                                CC.t("&7trade history menu.")
                        ))
                        .build();
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

        buttons.put(INFO_BUTTON_SLOT, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                List<String> lore = new ArrayList<>();
                lore.add(CC.t("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
                lore.add(CC.t("&6&lTrade Information:"));
                lore.add("");
                lore.add(CC.t("&7Player 1: &f" + viewingName));
                lore.add(CC.t("&7Player 2: &f" + otherName));
                lore.add("");
                lore.add(CC.t("&a&lItems Given by " + viewingName + ": &7" +
                        tradeSession.getPlayerItems(viewingPlayerId).size()));
                lore.add(CC.t("&c&lItems Given by " + otherName + ": &7" +
                        tradeSession.getOtherPlayerItems(viewingPlayerId).size()));
                lore.add("");
                lore.add(CC.t("&6&lStatus: &a&lCompleted Successfully"));
                lore.add(CC.t("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

                return new ItemBuilder(Material.BOOK)
                        .setDisplayName(CC.t("&6&lTrade Information"))
                        .setLore(lore)
                        .glowing(true)
                        .build();
            }
        });
    }

    private void addBorderButtons(Map<Integer, Button> buttons) {
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        // Add border to bottom row (except back and info buttons)
        for (int i = 45; i < 54; i++) {
            if (i != BACK_BUTTON_SLOT && i != INFO_BUTTON_SLOT) {
                buttons.put(i, new Button() {
                    @Override
                    public ItemStack getButtonItem(Player player) {
                        return borderItem;
                    }
                });
            }
        }
    }

    @Override
    public int getSize() {
        return 54; // 6 rows
    }

    private static class StaticTradeItemButton extends Button {
        private final ItemStack item;
        private final boolean isViewerItem;

        public StaticTradeItemButton(ItemStack item, boolean isViewerItem) {
            this.item = item;
            this.isViewerItem = isViewerItem;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            if (item == null || item.getType() == Material.AIR) {
                return new ItemStack(Material.AIR);
            }

            // Clone the item to avoid modifying the original
            ItemStack displayItem = item.clone();

            // Add trade information to the lore
            List<String> lore = new ArrayList<>();
            if (displayItem.hasItemMeta() && displayItem.getItemMeta().hasLore()) {
                lore.addAll(displayItem.getItemMeta().getLore());
                lore.add("");
            }

            lore.add(CC.t("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            if (isViewerItem) {
                lore.add(CC.t("&a&lItem Given Away"));
                lore.add(CC.t("&7This item was traded away"));
            } else {
                lore.add(CC.t("&c&lItem Received"));
                lore.add(CC.t("&7This item was received"));
            }
            lore.add(CC.t("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));

            return new ItemBuilder(displayItem)
                    .setLore(lore)
                    .glowing(true)
                    .build();
        }

//        @Override
//        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
//            // Do nothing - this is just for display
//        }
    }
}