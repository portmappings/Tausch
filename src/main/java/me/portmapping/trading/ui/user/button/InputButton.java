package me.portmapping.trading.ui.user.button;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.config.ConfigCursor;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.UUID;

public class InputButton extends Button {
    private final TradeSession session;

    public InputButton(TradeSession session) {
        this.session = session;
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        UUID playerId = player.getUniqueId();
        List<ItemStack> myItems = session.getPlayerItems(playerId);
        List<ItemStack> otherItems = session.getOtherPlayerItems(playerId);
        boolean confirmed = session.hasConfirmed(playerId);
        int countdown = session.getCountdown();

        return createInputButtonItem(myItems, otherItems, confirmed, countdown);
    }

    private ItemStack createInputButtonItem(List<ItemStack> myItems, List<ItemStack> otherItems, boolean confirmed, int countdown) {
        ConfigCursor config = new ConfigCursor(Tausch.getInstance().getMenusConfig(), "input-button");

        int amount = Math.max(1, countdown);

        if (confirmed) {
            ConfigCursor section = new ConfigCursor(config.getFileConfig(), config.getPath() + ".confirmed");
            Material material = Material.matchMaterial(section.getString("material"));
            String displayName = section.getString("display-name");
            List<String> lore = section.getStringList("lore");
            return new ItemBuilder(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .setAmount(amount)
                    .build();
        }

        if (countdown > 1) {
            ConfigCursor section = new ConfigCursor(config.getFileConfig(), config.getPath() + ".countdown");
            Material material = Material.matchMaterial(section.getString("material"));
            String displayName = section.getString("display-name").replace("{countdown}", String.valueOf(countdown));
            List<String> lore = section.getStringList("lore");
            lore.replaceAll(line -> line.replace("{countdown}", String.valueOf(countdown)));
            return new ItemBuilder(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .setAmount(amount)
                    .build();
        }

        if (myItems.isEmpty() && otherItems.isEmpty()) {
            ConfigCursor section = new ConfigCursor(config.getFileConfig(), config.getPath() + ".empty");
            Material material = Material.matchMaterial(section.getString("material"));
            String displayName = section.getString("display-name");
            List<String> lore = section.getStringList("lore");
            return new ItemBuilder(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .setAmount(amount)
                    .build();
        }

        if (myItems.isEmpty() && !otherItems.isEmpty()) {
            ConfigCursor section = new ConfigCursor(config.getFileConfig(), config.getPath() + ".gift");
            Material material = Material.matchMaterial(section.getString("material"));
            String displayName = section.getString("display-name");
            List<String> lore = section.getStringList("lore");
            return new ItemBuilder(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .setAmount(amount)
                    .build();
        }

        if (!myItems.isEmpty() && otherItems.isEmpty()) {
            ConfigCursor section = new ConfigCursor(config.getFileConfig(), config.getPath() + ".warning");
            Material material = Material.matchMaterial(section.getString("material"));
            String displayName = section.getString("display-name");
            List<String> lore = section.getStringList("lore");
            return new ItemBuilder(material)
                    .setDisplayName(displayName)
                    .setLore(lore)
                    .setAmount(amount)
                    .build();
        }

        ConfigCursor section = new ConfigCursor(config.getFileConfig(), config.getPath() + ".deal");
        Material material = Material.matchMaterial(section.getString("material"));
        String displayName = section.getString("display-name");
        List<String> lore = section.getStringList("lore");
        return new ItemBuilder(material)
                .setDisplayName(displayName)
                .setLore(lore)
                .setAmount(amount)
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        UUID playerId = player.getUniqueId();
        List<ItemStack> myItems = session.getPlayerItems(playerId);
        List<ItemStack> otherItems = session.getOtherPlayerItems(playerId);
        if (myItems.isEmpty() && otherItems.isEmpty()) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
            return;
        }

        session.toggleConfirmation(playerId);
        session.reopenMenus();

        if (session.bothConfirmed()) {
            session.completeTrade();
        }
    }
}
