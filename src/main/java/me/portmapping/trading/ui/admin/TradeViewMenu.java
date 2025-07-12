package me.portmapping.trading.ui.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@RequiredArgsConstructor
@Getter
public class TradeViewMenu extends Menu {

    private final TradeSession tradeSession;
    private final String senderName;
    private final String targetName;
    private final TradeHistoryMenu parentMenu;

    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40};
    private static final int INFO_SLOT = 39;
    private static final int STATUS_SLOT = 41;
    private static final int BACK_BUTTON_SLOT = 49;

    private static final List<Integer> LEFT_SLOTS = Arrays.asList(
            0, 1, 2, 3,
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30
    );

    private static final List<Integer> RIGHT_SLOTS = Arrays.asList(
            5, 6, 7, 8,
            14, 15, 16, 17,
            23, 24, 25, 26,
            32, 33, 34, 35
    );

    @Override
    public String getTitle(Player player) {
        return senderName + " ⇄ " + targetName;
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Dividers
        ItemStack divider = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();
        for (int slot : DIVIDER_SLOTS) {
            buttons.put(slot, new StaticButton(divider));
        }

        // Left side sender items
        List<ItemStack> senderItems = tradeSession.getSenderItems();
        for (int i = 0; i < LEFT_SLOTS.size(); i++) {
            int slot = LEFT_SLOTS.get(i);
            if (i < senderItems.size() && senderItems.get(i) != null) {
                ItemStack item = senderItems.get(i).clone();
                ItemBuilder builder = new ItemBuilder(item);
                builder.addToLore("");
                builder.addToLore("&7Offered by: &a" + senderName);
                buttons.put(slot, new StaticButton(builder.build()));
            } else {
                ItemStack empty = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setDisplayName("&7Empty Slot")
                        .addToLore("&7" + senderName + " offered nothing here")
                        .build();
                buttons.put(slot, new StaticButton(empty));
            }
        }

        // Right side target items
        List<ItemStack> targetItems = tradeSession.getTargetItems();
        for (int i = 0; i < RIGHT_SLOTS.size(); i++) {
            int slot = RIGHT_SLOTS.get(i);
            if (i < targetItems.size() && targetItems.get(i) != null) {
                ItemStack item = targetItems.get(i).clone();
                ItemBuilder builder = new ItemBuilder(item);
                builder.addToLore("");
                builder.addToLore("&7Offered by: &b" + targetName);
                buttons.put(slot, new StaticButton(builder.build()));
            } else {
                ItemStack empty = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                        .setDisplayName("&7Empty Slot")
                        .addToLore("&7" + targetName + " offered nothing here")
                        .build();
                buttons.put(slot, new StaticButton(empty));
            }
        }

        // Info button at slot 39
        buttons.put(INFO_SLOT, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                ItemBuilder builder = new ItemBuilder(Material.BOOK)
                        .setDisplayName("&eTrading Information")
                        .addToLore("&7Sender: &a" + senderName)
                        .addToLore("&7Target: &b" + targetName)
                        .addToLore("");

                int senderCount = senderItems.size();
                int targetCount = targetItems.size();

                builder.addToLore("&a" + senderName + " offered: &f" + senderCount + " items");
                builder.addToLore("&b" + targetName + " offered: &f" + targetCount + " items");
                builder.addToLore("");

                if (senderCount > 0 && targetCount > 0) {
                    builder.addToLore("&7Trade Type: &eNormal Trade");
                } else if (senderCount > 0) {
                    builder.addToLore("&7Trade Type: &6Gift to " + targetName);
                } else if (targetCount > 0) {
                    builder.addToLore("&7Trade Type: &6Gift to " + senderName);
                } else {
                    builder.addToLore("&7Trade Type: &cEmpty Trade");
                }

                builder.addToLore("");
                builder.addToLore("&7Status: &aCompleted");

                return builder.build();
            }
        });

        // Status button at slot 41
        buttons.put(STATUS_SLOT, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                boolean senderConfirmed = tradeSession.isSenderConfirmed();
                boolean targetConfirmed = tradeSession.isTargetConfirmed();

                ItemBuilder builder = new ItemBuilder(Material.LIME_DYE)
                        .setDisplayName("&aConfirmation Status")
                        .addToLore("&7This trade was completed")
                        .addToLore("");

                builder.addToLore(senderConfirmed ? "&a✓ " + senderName + " confirmed" : "&c✗ " + senderName + " did not confirm");
                builder.addToLore(targetConfirmed ? "&a✓ " + targetName + " confirmed" : "&c✗ " + targetName + " did not confirm");

                builder.addToLore("");
                builder.addToLore("&7Both players confirmed to complete");

                return builder.build();
            }
        });

        // Back button at slot 49
        buttons.put(BACK_BUTTON_SLOT, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.ARROW)
                        .setDisplayName("&cBack to Trade History")
                        .addToLore("&7Click to return to the trade history")
                        .build();
            }

            @Override
            public void clicked(Player player, int slot, org.bukkit.event.inventory.ClickType clickType, int hotbarButton) {
                if (parentMenu != null) {
                    parentMenu.openMenu(player);
                } else {
                    player.closeInventory();
                }
            }
        });

        return buttons;
    }

    private static class StaticButton extends Button {
        private final ItemStack item;

        public StaticButton(ItemStack item) {
            this.item = item.clone();
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return item;
        }
    }
}
