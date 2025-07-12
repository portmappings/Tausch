package me.portmapping.trading.ui.user.button;

import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class InputButton extends Button {
        private final TradeSession session;
        private final Player player;

        public InputButton(TradeSession session, Player player) {
            this.session = session;
            this.player = player;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            UUID playerId = player.getUniqueId();
            List<ItemStack> myItems = session.getPlayerItems(playerId);
            List<ItemStack> otherItems = session.getOtherPlayerItems(playerId);
            boolean confirmed = session.hasConfirmed(playerId);

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
                return new ItemBuilder(Material.GRAY_TERRACOTTA)
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
            UUID playerId = player.getUniqueId();
            session.toggleConfirmation(playerId);
            session.reopenMenus();

            if (session.bothConfirmed()) {
                session.completeTrade();
            }
        }
    }