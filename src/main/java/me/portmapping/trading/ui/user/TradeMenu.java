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

/**
 * A symmetrical trading GUI.
 * • Each player can offer up to <strong>16 distinct ItemStacks</strong>.
 * • Your items are always shown on the <em>left</em>, your partner's on the <em>right</em>.
 * • A vertical divider keeps it tidy.
 */
@RequiredArgsConstructor
@Getter
public class TradeMenu extends Menu {

    private final TradeSession tradeSession;

    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40};

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
        UUID otherUUID = player.getUniqueId().equals(tradeSession.getSender())
                ? tradeSession.getTarget()
                : tradeSession.getSender();
        Player other = Bukkit.getPlayer(otherUUID);
        return other != null ? "Trading with " + other.getName() : "Trading";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        boolean isSender = player.getUniqueId().equals(tradeSession.getSender());

        // 1) Divider bar (only 5 slots)
        ItemStack divider = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE).setDisplayName(" ").build();
        for (int s : DIVIDER_SLOTS) buttons.put(s, new StaticItemButton(divider));

        // 2) Show items
        List<ItemStack> myItems = isSender ? tradeSession.getSenderItems() : tradeSession.getTargetItems();
        List<ItemStack> otherItems = isSender ? tradeSession.getTargetItems() : tradeSession.getSenderItems();

        for (int i = 0; i < myItems.size() && i < LEFT_SLOTS.size(); i++) {
            int index = i;
            buttons.put(LEFT_SLOTS.get(i), new ClickableItemButton(myItems.get(i), () -> {
                Player onlinePlayer = Bukkit.getPlayer(player.getUniqueId());
                if (onlinePlayer != null && onlinePlayer.getInventory().firstEmpty() != -1) {
                    onlinePlayer.getInventory().addItem(myItems.get(index));
                    myItems.remove(index);
                    // Reopen to update both
                    Player s = Bukkit.getPlayer(tradeSession.getSender());
                    Player t = Bukkit.getPlayer(tradeSession.getTarget());
                    if (s != null) new TradeMenu(tradeSession).openMenu(s);
                    if (t != null) new TradeMenu(tradeSession).openMenu(t);
                } else {
                    player.sendMessage("§cYour inventory is full!");
                }
            }));
        }

        for (int i = 0; i < otherItems.size() && i < RIGHT_SLOTS.size(); i++) {
            buttons.put(RIGHT_SLOTS.get(i), new StaticItemButton(otherItems.get(i)));
        }

        // 3) Input button at slot 39
        buttons.put(39, new InputButton(tradeSession, isSender));

        // 4) Confirmation status button at slot 41
        buttons.put(41, new ConfirmationStatusButton(tradeSession, isSender));

        return buttons;
    }

    /* ────────────────────────────────────────────────────────────────────────── */

    private static class StaticItemButton extends Button {
        private final ItemStack item;
        private StaticItemButton(ItemStack item) { this.item = item.clone(); }
        @Override public ItemStack getButtonItem(Player player) { return item; }
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
        private final boolean isSender;

        private InputButton(TradeSession s, boolean sender) {
            this.session = s;
            this.isSender = sender;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            List<ItemStack> myItems = isSender ? session.getSenderItems() : session.getTargetItems();
            List<ItemStack> otherItems = isSender ? session.getTargetItems() : session.getSenderItems();
            boolean myConfirmed = isSender ? session.isSenderConfirmed() : session.isTargetConfirmed();

            if (myConfirmed) {
                return new ItemBuilder(Material.GREEN_TERRACOTTA)
                        .setDisplayName("&aDeal accepted!")
                        .addToLore("&7You are still waiting for the")
                        .addToLore("&7other side to accept")
                        .build();
            } else if (myItems.isEmpty() && otherItems.isEmpty()) {
                return new ItemBuilder(Material.GREEN_TERRACOTTA)
                        .setDisplayName("&aTrading")
                        .addToLore("&7You need to click an item in your")
                        .addToLore("&7inventory to offer it for trade")
                        .build();
            } else if (myItems.isEmpty() && !otherItems.isEmpty()) {
                return new ItemBuilder(Material.BLUE_TERRACOTTA)
                        .setDisplayName("&bGift!")
                        .addToLore("&7You are receiving items without")
                        .addToLore("&7offering anything in return")
                        .build();
            } else if (!myItems.isEmpty() && otherItems.isEmpty()) {
                return new ItemBuilder(Material.ORANGE_TERRACOTTA)
                        .setDisplayName("&eWarning!")
                        .addToLore("&7You are offering items without")
                        .addToLore("&7getting anything in return")
                        .build();
            } else {
                return new ItemBuilder(Material.LIME_TERRACOTTA)
                        .setDisplayName("&eDeal!")
                        .addToLore("&7Trades cannot be reverted!")
                        .addToLore("&7Make sure to review the trade")
                        .addToLore("&7before accepting")
                        .build();
            }
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            if (isSender) session.setSenderConfirmed(!session.isSenderConfirmed());
            else           session.setTargetConfirmed(!session.isTargetConfirmed());

            Player s = Bukkit.getPlayer(session.getSender());
            Player t = Bukkit.getPlayer(session.getTarget());
            if (s != null) new TradeMenu(session).openMenu(s);
            if (t != null) new TradeMenu(session).openMenu(t);

            if (session.isSenderConfirmed() && session.isTargetConfirmed()) {
                s.sendMessage("§aTrade complete!");
                t.sendMessage("§aTrade complete!");
                // TODO: Implement safe item exchange logic here
                session.cancel(); // temporary clear
                s.closeInventory();
                t.closeInventory();
            }
        }
    }

    private static class ConfirmationStatusButton extends Button {
        private final TradeSession session;
        private final boolean isSender;

        private ConfirmationStatusButton(TradeSession s, boolean sender) {
            this.session = s;
            this.isSender = sender;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            boolean otherConfirmed = isSender ? session.isTargetConfirmed() : session.isSenderConfirmed();
            UUID otherUUID = player.getUniqueId().equals(session.getSender())
                    ? session.getTarget()
                    : session.getSender();
            Player other = Bukkit.getPlayer(otherUUID);
            String otherName = other != null ? other.getName() : "Unknown";

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