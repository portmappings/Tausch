package me.portmapping.trading.ui.user.button;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.config.ConfigCursor;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ConfirmationStatusButton extends Button {

    private final TradeSession session;

    public ConfirmationStatusButton(TradeSession session, Player ignoredViewer) {
        this.session = session;
    }

    @Override
    public ItemStack getButtonItem(Player viewer) {
        UUID viewerId = viewer.getUniqueId();
        UUID partnerId = session.getOther(viewerId);
        Player partner = Bukkit.getPlayer(partnerId);
        String partnerName = partner != null ? partner.getName() : "Unknown";
        boolean partnerConfirmed = session.hasConfirmed(partnerId);

        String stateKey = partnerConfirmed ? "confirmed" : "pending";
        ConfigCursor cursor = new ConfigCursor(Tausch.getInstance().getMenusConfig(),
                "confirmation-status-button." + stateKey);

        Material material = Optional.ofNullable(
                        Material.matchMaterial(cursor.getString("material", "GRAY_DYE")))
                .orElse(Material.GRAY_DYE);

        String display = ChatColor.translateAlternateColorCodes('&',
                cursor.getString("display-name", "&ePending {other} confirm")
                        .replace("{other}", partnerName));

        List<String> lore = cursor.getStringList("lore",
                        Collections.singletonList("&7Waiting for {other} to confirm..."))
                .stream()
                .map(s -> ChatColor.translateAlternateColorCodes('&',
                        s.replace("{other}", partnerName)))
                .collect(Collectors.toList());

        return new ItemBuilder(material)
                .setDisplayName(display)
                .setLore(lore)
                .build();
    }
}
