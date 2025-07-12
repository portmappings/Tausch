package me.portmapping.trading.ui.user.button;

import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class ConfirmationStatusButton extends Button {
    private final TradeSession session;
    private final Player player;

    public ConfirmationStatusButton(TradeSession session, Player player) {
        this.session = session;
        this.player = player;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        UUID playerId = player.getUniqueId();
        UUID otherUUID = session.getOther(playerId);
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