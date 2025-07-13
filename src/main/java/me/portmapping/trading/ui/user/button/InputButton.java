package me.portmapping.trading.ui.user.button;

import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.config.ConfigCursor;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class InputButton extends Button {

    private final TradeSession session;

    public InputButton(TradeSession session) {
        this.session = session;
    }

    private boolean canReceiveAfterTrade(Player player,
                                         List<ItemStack> toRemove,
                                         List<ItemStack> toAdd) {

        ItemStack[] contents = player.getInventory().getContents();

        int freedSlots = 0;
        for (ItemStack removeStack : toRemove) {
            if (removeStack == null) continue;

            int amountToRemove = removeStack.getAmount();
            for (ItemStack invStack : contents) {
                if (invStack == null || invStack.getType() == Material.AIR) continue;

                if (invStack.isSimilar(removeStack)) {
                    int removeFromStack = Math.min(invStack.getAmount(), amountToRemove);
                    amountToRemove -= removeFromStack;

                    if (removeFromStack == invStack.getAmount()) {
                        freedSlots++;
                    }

                    if (amountToRemove <= 0) break;
                }
            }
        }

        int emptySlots = 0;
        for (ItemStack stack : contents) {
            if (stack == null || stack.getType() == Material.AIR) {
                emptySlots++;
            }
        }

        ItemStack[] simulatedContents = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            simulatedContents[i] = contents[i] == null ? null : contents[i].clone();
        }

        for (ItemStack removeStack : toRemove) {
            if (removeStack == null) continue;

            int amountToRemove = removeStack.getAmount();
            for (int i = 0; i < simulatedContents.length; i++) {
                ItemStack invStack = simulatedContents[i];
                if (invStack == null || invStack.getType() == Material.AIR) continue;

                if (invStack.isSimilar(removeStack)) {
                    int removeFromStack = Math.min(invStack.getAmount(), amountToRemove);
                    amountToRemove -= removeFromStack;

                    invStack.setAmount(invStack.getAmount() - removeFromStack);
                    if (invStack.getAmount() <= 0) {
                        simulatedContents[i] = null;
                    }

                    if (amountToRemove <= 0) break;
                }
            }
        }

        int invSize = player.getInventory().getSize();
        int roundedSize = (invSize / 9) * 9; // rounds down
        if (roundedSize > 54) roundedSize = 54;
        if (roundedSize < 9) roundedSize = 9;


        Inventory tempInv = Bukkit.createInventory(null, roundedSize);
        tempInv.setContents(simulatedContents);

        Map<Integer, ItemStack> leftovers = tempInv.addItem(toAdd.toArray(new ItemStack[0]));

        return leftovers.isEmpty();
    }

    @Override
    public ItemStack getButtonItem(Player player) {
        UUID playerId      = player.getUniqueId();
        List<ItemStack> myItems    = session.getPlayerItems(playerId);        // items he gives
        List<ItemStack> otherItems = session.getOtherPlayerItems(playerId);   // items he gets
        boolean confirmed = session.hasConfirmed(playerId);
        int countdown     = session.getCountdown();

        boolean fits = canReceiveAfterTrade(player, myItems, otherItems);
        String mode   = Tausch.getInstance()
                .getSettingsConfig()
                .getConfig()
                .getString("TRADE.FULL_INVENTORY_BEHAVIOR", "BLOCK");
        boolean dropMode = "DROP".equalsIgnoreCase(mode);
        boolean blockNoSpace = !dropMode && !fits;

        return createInputButtonItem(myItems, otherItems, confirmed, countdown, blockNoSpace);
    }

    private ItemStack createInputButtonItem(List<ItemStack> myItems,
                                            List<ItemStack> otherItems,
                                            boolean confirmed,
                                            int countdown,
                                            boolean blockNoSpace) {

        ConfigCursor cfg = new ConfigCursor(Tausch.getInstance().getMenusConfig(), "input-button");
        int amount = Math.max(1, countdown);

        if (blockNoSpace) {
            ConfigCursor s = new ConfigCursor(cfg.getFileConfig(), cfg.getPath() + ".no-space");
            return new ItemBuilder(
                    Material.matchMaterial(s.getString("material")))
                    .setDisplayName(s.getString("display-name"))
                    .setLore(s.getStringList("lore"))
                    .setAmount(amount)
                    .build();
        }

        if (confirmed) {
            ConfigCursor s = new ConfigCursor(cfg.getFileConfig(), cfg.getPath() + ".confirmed");
            return new ItemBuilder(
                    Material.matchMaterial(s.getString("material")))
                    .setDisplayName(s.getString("display-name"))
                    .setLore(s.getStringList("lore"))
                    .setAmount(amount)
                    .build();
        }

        if (countdown > 1) {
            ConfigCursor s = new ConfigCursor(cfg.getFileConfig(), cfg.getPath() + ".countdown");
            List<String> lore = s.getStringList("lore");
            lore.replaceAll(l -> l.replace("{countdown}", String.valueOf(countdown)));
            return new ItemBuilder(
                    Material.matchMaterial(s.getString("material")))
                    .setDisplayName(s.getString("display-name")
                            .replace("{countdown}", String.valueOf(countdown)))
                    .setLore(lore)
                    .setAmount(amount)
                    .build();
        }

        if (myItems.isEmpty() && otherItems.isEmpty()) {
            ConfigCursor s = new ConfigCursor(cfg.getFileConfig(), cfg.getPath() + ".empty");
            return new ItemBuilder(
                    Material.matchMaterial(s.getString("material")))
                    .setDisplayName(s.getString("display-name"))
                    .setLore(s.getStringList("lore"))
                    .setAmount(amount)
                    .build();
        }
        if (myItems.isEmpty()) {
            ConfigCursor s = new ConfigCursor(cfg.getFileConfig(), cfg.getPath() + ".gift");
            return new ItemBuilder(
                    Material.matchMaterial(s.getString("material")))
                    .setDisplayName(s.getString("display-name"))
                    .setLore(s.getStringList("lore"))
                    .setAmount(amount)
                    .build();
        }
        if (otherItems.isEmpty()) {
            ConfigCursor s = new ConfigCursor(cfg.getFileConfig(), cfg.getPath() + ".warning");
            return new ItemBuilder(
                    Material.matchMaterial(s.getString("material")))
                    .setDisplayName(s.getString("display-name"))
                    .setLore(s.getStringList("lore"))
                    .setAmount(amount)
                    .build();
        }

        ConfigCursor s = new ConfigCursor(cfg.getFileConfig(), cfg.getPath() + ".deal");
        return new ItemBuilder(
                Material.matchMaterial(s.getString("material")))
                .setDisplayName(s.getString("display-name"))
                .setLore(s.getStringList("lore"))
                .setAmount(amount)
                .build();
    }

    @Override
    public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
        UUID playerId      = player.getUniqueId();
        List<ItemStack> myItems    = session.getPlayerItems(playerId);
        List<ItemStack> otherItems = session.getOtherPlayerItems(playerId);

        String mode = Tausch.getInstance()
                .getSettingsConfig()
                .getConfig()
                .getString("TRADE.FULL_INVENTORY_BEHAVIOR", "BLOCK");
        boolean dropMode = "DROP".equalsIgnoreCase(mode);
        boolean fits = canReceiveAfterTrade(player, myItems, otherItems);

        if (!dropMode && !fits) {
            player.sendMessage("§cYou don't have enough space to receive the trade items.");
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1F, 1F);
            return;
        }

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
