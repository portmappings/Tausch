package me.portmapping.trading.ui.user.button;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;
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

        ConfigurationSection section = Tausch.getInstance().getMenusConfig().getConfig().getConfigurationSection("confirmation-status-button");
        ConfigurationSection stateSection = otherConfirmed ? section.getConfigurationSection("confirmed") : section.getConfigurationSection("pending");

        Material material = Material.matchMaterial(stateSection.getString("material", "GRAY_DYE"));
        String displayName = stateSection.getString("display-name", "&ePending their confirm").replace("{other}", otherName);
        List<String> loreList = stateSection.getStringList("lore");
        for (int i = 0; i < loreList.size(); i++) {
            loreList.set(i, loreList.get(i).replace("{other}", otherName));
        }

        return new ItemBuilder(material)
                .setDisplayName(displayName)
                .setLore(loreList)
                .build();
    }
}
