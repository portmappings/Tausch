package me.portmapping.trading.ui.admin.button;

import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.admin.TradeViewMenu;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TradeHistoryButton extends Button {
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

            List<String> lore = new ArrayList<>();
            lore.add(CC.t("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            lore.add(CC.t("&7Traded with: &f" + otherName));
            lore.add("");

            if (isViewer) {
                lore.add(CC.t("&a&lYour Items Given: &7(" + viewerItems.size() + " items)"));
                lore.add(CC.t("&c&lItems Received: &7(" + otherItems.size() + " items)"));
            } else {
                lore.add(CC.t("&a&lTheir Items Given: &7(" + viewerItems.size() + " items)"));
                lore.add(CC.t("&c&lItems Received: &7(" + otherItems.size() + " items)"));
            }

            lore.add("");
            lore.add(CC.t("&6&lStatus: &a&lCompleted"));
            lore.add("");
            lore.add(CC.t("&7━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"));
            lore.add(CC.t("&e&lClick to view detailed trade information!"));

            return new ItemBuilder(Material.CHEST)
                    .setDisplayName(CC.t("&6&lTrade with " + otherName))
                    .setLore(lore)
                    .glowing(true)
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            new TradeViewMenu(tradeSession, viewingPlayerId).openMenu(player);
        }
    }