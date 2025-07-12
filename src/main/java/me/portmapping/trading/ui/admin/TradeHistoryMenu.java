package me.portmapping.trading.ui.admin;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import lombok.Getter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.utils.menu.pagination.PageButton;
import me.portmapping.trading.utils.menu.pagination.PaginatedMenu;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.CompletableFuture;

@Getter
public class TradeHistoryMenu extends PaginatedMenu {

    private final UUID targetUUID;
    private final String targetName;
    private final List<Document> allTrades = new ArrayList<>();
    private boolean isLoaded = false;
    private boolean isLoading = false;

    public TradeHistoryMenu(UUID targetUUID, String targetName) {
        this.targetUUID = targetUUID;
        this.targetName = targetName;
        loadAllTrades();
    }

    @Override
    public String getPrePaginatedTitle(Player player) {
        return "Trade History: " + targetName;
    }

    @Override
    public int getSize() {
        return 54;
    }

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 28; // 4 rows of 7 items (excluding borders)
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        if (!isLoaded) {
            // Show loading indicator
            buttons.put(13, new LoadingButton());
            return buttons;
        }

        int maxItems = getMaxItemsPerPage(player);
        int startIndex = (getPage() - 1) * maxItems;

        for (int i = 0; i < maxItems && startIndex + i < allTrades.size(); i++) {
            Document trade = allTrades.get(startIndex + i);
            buttons.put(i, new TradeHistoryButton(trade));
        }

        return buttons;
    }

    @Override
    public int getPages(Player player) {
        if (!isLoaded || allTrades.isEmpty()) return 1;
        return (int) Math.ceil(allTrades.size() / (double) getMaxItemsPerPage(player));
    }

    private void loadAllTrades() {
        if (isLoading) return;

        isLoading = true;

        CompletableFuture.runAsync(() -> {
            try {
                List<Document> trades = Tausch.getInstance().getMongoHandler().getTradeHistory()
                        .find(Filters.or(
                                Filters.eq("sender", targetUUID.toString()),
                                Filters.eq("target", targetUUID.toString())
                        ))
                        .sort(Sorts.descending("_id"))
                        .into(new ArrayList<>());

                // Update on main thread
                Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
                    allTrades.clear();
                    allTrades.addAll(trades);
                    isLoaded = true;
                    isLoading = false;
                });

            } catch (Exception e) {
                e.printStackTrace();
                Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
                    isLoading = false;
                });
            }
        });
    }


    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Add border items
        ItemStack border = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        // Top and bottom borders
        for (int i = 0; i < 9; i++) {
            buttons.put(i, new StaticButton(border));
            buttons.put(i + 45, new StaticButton(border));
        }

        // Side borders
        for (int i = 1; i < 5; i++) {
            buttons.put(i * 9, new StaticButton(border));
            buttons.put(i * 9 + 8, new StaticButton(border));
        }

        // Navigation buttons (only show if loaded and there are multiple pages)
        if (isLoaded && getPages(player) > 1) {
            if (getPage() > 1) {
                buttons.put(0, new PageButton(-1, this));
            }
            if (getPage() < getPages(player)) {
                buttons.put(8, new PageButton(1, this));
            }
        }

        // Info button
        buttons.put(4, new InfoButton());

        return buttons;
    }

    private class TradeHistoryButton extends Button {
        private final Document tradeDoc;

        public TradeHistoryButton(Document tradeDoc) {
            this.tradeDoc = tradeDoc;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            try {
                TradeSession session = TradeSession.fromBson(tradeDoc);

                UUID senderUUID = UUID.fromString(tradeDoc.getString("sender"));
                UUID targetUUID = UUID.fromString(tradeDoc.getString("target"));

                String senderName = "Unknown";
                String targetName = "Unknown";

                // Try to get player names
                try {
                    Player senderPlayer = Bukkit.getPlayer(senderUUID);
                    if (senderPlayer != null) senderName = senderPlayer.getName();

                    Player targetPlayer = Bukkit.getPlayer(targetUUID);
                    if (targetPlayer != null) targetName = targetPlayer.getName();
                } catch (Exception e) {
                    // Fallback to UUID if name lookup fails
                    senderName = senderUUID.toString().substring(0, 8);
                    targetName = targetUUID.toString().substring(0, 8);
                }

                ItemBuilder builder = new ItemBuilder(Material.PAPER)
                        .setDisplayName("&eTrade Record")
                        .addToLore("&7Sender: &f" + senderName)
                        .addToLore("&7Target: &f" + targetName)
                        .addToLore("");

                // Add sender items
                if (!session.getSenderItems().isEmpty()) {
                    builder.addToLore("&aSender Items:");
                    for (ItemStack item : session.getSenderItems()) {
                        if (item != null) {
                            String itemName = item.getType().name().toLowerCase().replace("_", " ");
                            builder.addToLore("  &7- &f" + item.getAmount() + "x " + itemName);
                        }
                    }
                } else {
                    builder.addToLore("&aSender Items: &cNone");
                }

                builder.addToLore("");

                // Add target items
                if (!session.getTargetItems().isEmpty()) {
                    builder.addToLore("&bTarget Items:");
                    for (ItemStack item : session.getTargetItems()) {
                        if (item != null) {
                            String itemName = item.getType().name().toLowerCase().replace("_", " ");
                            builder.addToLore("  &7- &f" + item.getAmount() + "x " + itemName);
                        }
                    }
                } else {
                    builder.addToLore("&bTarget Items: &cNone");
                }

                builder.addToLore("");
                builder.addToLore("&7Click to view detailed trade information");

                return builder.build();

            } catch (Exception e) {
                e.printStackTrace();
                return new ItemBuilder(Material.RED_STAINED_GLASS)
                        .setDisplayName("&cError loading trade")
                        .addToLore("&7" + e.getMessage())
                        .build();
            }
        }

        @Override
        public void clicked(Player player, int slot, org.bukkit.event.inventory.ClickType clickType, int hotbarButton) {
            try {
                TradeSession session = TradeSession.fromBson(tradeDoc);

                UUID senderUUID = UUID.fromString(tradeDoc.getString("sender"));
                UUID targetUUID = UUID.fromString(tradeDoc.getString("target"));

                String senderName = "Unknown";
                String targetName = "Unknown";

                // Try to get player names
                try {
                    Player senderPlayer = Bukkit.getPlayer(senderUUID);
                    if (senderPlayer != null) senderName = senderPlayer.getName();

                    Player targetPlayer = Bukkit.getPlayer(targetUUID);
                    if (targetPlayer != null) targetName = targetPlayer.getName();
                } catch (Exception e) {
                    // Fallback to UUID if name lookup fails
                    senderName = senderUUID.toString().substring(0, 8);
                    targetName = targetUUID.toString().substring(0, 8);
                }

                // Open the detailed trade view menu
                TradeViewMenu tradeViewMenu = new TradeViewMenu(session, senderName, targetName, TradeHistoryMenu.this);
                tradeViewMenu.openMenu(player);

            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage("§cError opening trade details: " + e.getMessage());
            }
        }
    }

    private class InfoButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            ItemBuilder builder = new ItemBuilder(Material.BOOK)
                    .setDisplayName("&eInfo")
                    .addToLore("&7Player: &f" + targetName);

            if (isLoaded) {
                builder.addToLore("&7Total Trades: &f" + allTrades.size())
                        .addToLore("&7Current Page: &f" + getPage() + "/" + getPages(player));
            } else if (isLoading) {
                builder.addToLore("&7Status: &eLoading...");
            } else {
                builder.addToLore("&7Status: &cFailed to load");
            }

            builder.addToLore("")
                    .addToLore("&7Use the arrows to navigate");

            return builder.build();
        }
    }

    private static class LoadingButton extends Button {
        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.HOPPER)
                    .setDisplayName("&eLoading trades...")
                    .addToLore("&7Please wait while we fetch")
                    .addToLore("&7the trade history.")
                    .build();
        }
    }

    private static class StaticButton extends Button {
        private final ItemStack item;

        public StaticButton(ItemStack item) {
            this.item = item;
        }

        @Override
        public ItemStack getButtonItem(Player player) {
            return item;
        }
    }
}