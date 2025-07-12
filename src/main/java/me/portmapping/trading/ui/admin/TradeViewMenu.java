package me.portmapping.trading.ui.admin;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.utils.menu.Menu;
import org.bukkit.Bukkit;
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
        return  senderName + " ⇄ " + targetName;
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        addDividerButtons(buttons);
        addItemButtons(buttons);
        addInfoButton(buttons);
        addStatusButton(buttons);
        addBackButton(buttons);

        return buttons;
    }

    private void addDividerButtons(Map<Integer, Button> buttons) {
        ItemStack divider = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        for (int slot : DIVIDER_SLOTS) {
            buttons.put(slot, new StaticButton(divider));
        }
    }

    private void addItemButtons(Map<Integer, Button> buttons) {
        List<ItemStack> senderItems = tradeSession.getSenderItems();
        List<ItemStack> targetItems = tradeSession.getTargetItems();

        addSenderItemButtons(buttons, senderItems);
        addTargetItemButtons(buttons, targetItems);
    }

    private void addSenderItemButtons(Map<Integer, Button> buttons, List<ItemStack> senderItems) {
        // Add sender items to left side
        for (int i = 0; i < senderItems.size() && i < LEFT_SLOTS.size(); i++) {
            ItemStack item = senderItems.get(i);
            if (item != null) {
                ItemStack displayItem = item.clone();
                
                // Add lore showing it belongs to sender
                ItemBuilder builder = new ItemBuilder(displayItem);
                builder.addToLore("");
                builder.addToLore("&7Offered by: &a" + senderName);
                
                buttons.put(LEFT_SLOTS.get(i), new StaticButton(builder.build()));
            }
        }
        
        // Fill empty slots with placeholder
        for (int i = senderItems.size(); i < LEFT_SLOTS.size(); i++) {
            ItemStack emptySlot = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .setDisplayName("&7Empty Slot")
                    .addToLore("&7" + senderName + " offered nothing here")
                    .build();
            buttons.put(LEFT_SLOTS.get(i), new StaticButton(emptySlot));
        }
    }

    private void addTargetItemButtons(Map<Integer, Button> buttons, List<ItemStack> targetItems) {
        // Add target items to right side
        for (int i = 0; i < targetItems.size() && i < RIGHT_SLOTS.size(); i++) {
            ItemStack item = targetItems.get(i);
            if (item != null) {
                ItemStack displayItem = item.clone();
                
                // Add lore showing it belongs to target
                ItemBuilder builder = new ItemBuilder(displayItem);
                builder.addToLore("");
                builder.addToLore("&7Offered by: &b" + targetName);
                
                buttons.put(RIGHT_SLOTS.get(i), new StaticButton(builder.build()));
            }
        }
        
        // Fill empty slots with placeholder
        for (int i = targetItems.size(); i < RIGHT_SLOTS.size(); i++) {
            ItemStack emptySlot = new ItemBuilder(Material.LIGHT_GRAY_STAINED_GLASS_PANE)
                    .setDisplayName("&7Empty Slot")
                    .addToLore("&7" + targetName + " offered nothing here")
                    .build();
            buttons.put(RIGHT_SLOTS.get(i), new StaticButton(emptySlot));
        }
    }

    private void addInfoButton(Map<Integer, Button> buttons) {
        buttons.put(INFO_SLOT, new TradeInfoButton());
    }

    private void addStatusButton(Map<Integer, Button> buttons) {
        buttons.put(STATUS_SLOT, new TradeStatusButton());
    }

    private void addBackButton(Map<Integer, Button> buttons) {
        buttons.put(BACK_BUTTON_SLOT, new BackButton());
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

    private class TradeInfoButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            List<ItemStack> senderItems = tradeSession.getSenderItems();
            List<ItemStack> targetItems = tradeSession.getTargetItems();

            ItemBuilder builder = new ItemBuilder(Material.BOOK)
                    .setDisplayName("&eTrading Information")
                    .addToLore("&7Sender: &a" + senderName)
                    .addToLore("&7Target: &b" + targetName)
                    .addToLore("");

            // Count items
            int senderItemCount = senderItems.size();
            int targetItemCount = targetItems.size();
            
            builder.addToLore("&a" + senderName + " offered: &f" + senderItemCount + " items");
            builder.addToLore("&b" + targetName + " offered: &f" + targetItemCount + " items");
            builder.addToLore("");

            // Trade type analysis
            if (senderItemCount > 0 && targetItemCount > 0) {
                builder.addToLore("&7Trade Type: &eNormal Trade");
            } else if (senderItemCount > 0 && targetItemCount == 0) {
                builder.addToLore("&7Trade Type: &6Gift to " + targetName);
            } else if (senderItemCount == 0 && targetItemCount > 0) {
                builder.addToLore("&7Trade Type: &6Gift to " + senderName);
            } else {
                builder.addToLore("&7Trade Type: &cEmpty Trade");
            }

            builder.addToLore("");
            builder.addToLore("&7Status: &aCompleted");

            return builder.build();
        }
    }

    private class TradeStatusButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            boolean senderConfirmed = tradeSession.isSenderConfirmed();
            boolean targetConfirmed = tradeSession.isTargetConfirmed();

            ItemBuilder builder = new ItemBuilder(Material.LIME_DYE)
                    .setDisplayName("&aConfirmation Status")
                    .addToLore("&7This trade was completed")
                    .addToLore("");

            if (senderConfirmed) {
                builder.addToLore("&a✓ " + senderName + " confirmed");
            } else {
                builder.addToLore("&c✗ " + senderName + " did not confirm");
            }

            if (targetConfirmed) {
                builder.addToLore("&a✓ " + targetName + " confirmed");
            } else {
                builder.addToLore("&c✗ " + targetName + " did not confirm");
            }

            builder.addToLore("");
            builder.addToLore("&7Both players confirmed to complete");

            return builder.build();
        }
    }

    private class BackButton extends Button {
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
    }
}