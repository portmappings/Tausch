package me.portmapping.trading.ui.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.portmapping.trading.model.TradeSession;
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
@Getter
public class TradeMenu extends Menu {

    private final TradeSession tradeSession;

    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40};
    private static final int INPUT_SLOT = 39;
    private static final int CONFIRMATION_STATUS_SLOT = 41;

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
        UUID otherUUID = tradeSession.getOther(player);
        Player other = Bukkit.getPlayer(otherUUID);
        return other != null ? other.getName() : "Unknown" + "⇄" + player.getName() ;
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        addDividerButtons(buttons);
        addItemButtons(buttons, player);
        addInputButton(buttons, player);
        addConfirmationStatusButton(buttons, player);

        return buttons;
    }

    private void addDividerButtons(Map<Integer, Button> buttons) {
        ItemStack divider = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        for (int slot : DIVIDER_SLOTS) {
            buttons.put(slot, new StaticItemButton(divider));
        }
    }

    private void addItemButtons(Map<Integer, Button> buttons, Player player) {
        List<ItemStack> myItems = tradeSession.getPlayerItems(player);
        List<ItemStack> otherItems = tradeSession.getOtherPlayerItems(player);

        addMyItemButtons(buttons, myItems);
        addOtherItemButtons(buttons, otherItems);
    }

    private void addMyItemButtons(Map<Integer, Button> buttons, List<ItemStack> myItems) {
        for (int i = 0; i < myItems.size() && i < LEFT_SLOTS.size(); i++) {
            int itemIndex = i;
            buttons.put(LEFT_SLOTS.get(i), new ClickableItemButton(myItems.get(i), () -> {
                returnItemToPlayer(myItems, itemIndex);
            }));
        }
    }

    private void addOtherItemButtons(Map<Integer, Button> buttons, List<ItemStack> otherItems) {
        for (int i = 0; i < otherItems.size() && i < RIGHT_SLOTS.size(); i++) {
            buttons.put(RIGHT_SLOTS.get(i), new StaticItemButton(otherItems.get(i)));
        }
    }

    private void returnItemToPlayer(List<ItemStack> items, int index) {
        Player senderPlayer = Bukkit.getPlayer(tradeSession.getSender());
        Player targetPlayer = Bukkit.getPlayer(tradeSession.getTarget());

        if (senderPlayer == null || targetPlayer == null) return;

        Player itemOwner = items == tradeSession.getSenderItems() ? senderPlayer : targetPlayer;

        if (itemOwner.getInventory().firstEmpty() != -1) {
            itemOwner.getInventory().addItem(items.get(index));
            items.remove(index);
            tradeSession.resetConfirmation();
            tradeSession.reopenMenusForBothPlayers();
        } else {
            itemOwner.sendMessage("§cYour inventory is full!");
        }
    }

    private void addInputButton(Map<Integer, Button> buttons, Player player) {
        buttons.put(INPUT_SLOT, new InputButton(tradeSession, player));
    }

    private void addConfirmationStatusButton(Map<Integer, Button> buttons, Player player) {
        buttons.put(CONFIRMATION_STATUS_SLOT, new ConfirmationStatusButton(tradeSession, player));
    }

    private static class StaticItemButton extends Button {
        private final ItemStack item;

        private StaticItemButton(ItemStack item) {
            this.item = item.clone();
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return item;
        }
    }

    private static class ClickableItemButton extends Button {
        private final ItemStack item;
        private final Runnable onClick;

        private ClickableItemButton(ItemStack item, Runnable onClick) {
            this.item = item.clone();
            this.onClick = onClick;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return item;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            onClick.run();
        }
    }

    private static class InputButton extends Button {
        private final TradeSession session;
        private final Player player;

        private InputButton(TradeSession session, Player player) {
            this.session = session;
            this.player = player;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            List<ItemStack> myItems = session.getPlayerItems(player);
            List<ItemStack> otherItems = session.getOtherPlayerItems(player);
            boolean confirmed = session.hasConfirmed(player);

            return createInputButtonItem(myItems, otherItems, confirmed);
        }

        private ItemStack createInputButtonItem(List<ItemStack> myItems, List<ItemStack> otherItems, boolean confirmed) {
            if (confirmed) {
                return new ItemBuilder(Material.GREEN_TERRACOTTA)
                        .setDisplayName("&aDeal accepted!")
                        .addToLore("&7You are still waiting for the")
                        .addToLore("&7other side to accept")
                        .build();
            }

            if (myItems.isEmpty() && otherItems.isEmpty()) {
                return new ItemBuilder(Material.GREEN_TERRACOTTA)
                        .setDisplayName("&aTrading")
                        .addToLore("&7You need to click an item in your")
                        .addToLore("&7inventory to offer it for trade")
                        .build();
            }

            if (myItems.isEmpty() && !otherItems.isEmpty()) {
                return new ItemBuilder(Material.BLUE_TERRACOTTA)
                        .setDisplayName("&bGift!")
                        .addToLore("&7You are receiving items without")
                        .addToLore("&7offering anything in return")
                        .build();
            }

            if (!myItems.isEmpty() && otherItems.isEmpty()) {
                return new ItemBuilder(Material.ORANGE_TERRACOTTA)
                        .setDisplayName("&eWarning!")
                        .addToLore("&7You are offering items without")
                        .addToLore("&7getting anything in return")
                        .build();
            }

            return new ItemBuilder(Material.LIME_TERRACOTTA)
                    .setDisplayName("&eDeal!")
                    .addToLore("&7Trades cannot be reverted!")
                    .addToLore("&7Make sure to review the trade")
                    .addToLore("&7before accepting")
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            session.toggleConfirmation(player);
            session.reopenMenusForBothPlayers();

            if (session.bothConfirmed()) {
                session.completeTrade();
            }
        }
    }

    private static class ConfirmationStatusButton extends Button {
        private final TradeSession session;
        private final Player player;

        private ConfirmationStatusButton(TradeSession session, Player player) {
            this.session = session;
            this.player = player;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            UUID otherUUID = session.getOther(player);
            Player other = Bukkit.getPlayer(otherUUID);
            String otherName = other != null ? other.getName() : "Unknown";
            boolean otherConfirmed = session.hasConfirmed(otherUUID);

            if (otherConfirmed) {
                return new ItemBuilder(Material.LIME_DYE)
                        .setDisplayName("&aOther player confirmed")
                        .addToLore("&7Trading with: " + otherName)
                        .build();
            } else {
                return new ItemBuilder(Material.GRAY_DYE)
                        .setDisplayName("&ePending their confirm")
                        .addToLore("&7Trading with: " + otherName)
                        .build();
            }
        }
    }
}