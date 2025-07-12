package me.portmapping.trading.ui.admin;

import com.mongodb.client.FindIterable;
import lombok.RequiredArgsConstructor;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.admin.button.TradeHistoryButton;
import me.portmapping.trading.utils.chat.CC;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.utils.menu.pagination.PaginatedMenu;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RequiredArgsConstructor
public class TradeHistoryMenu extends PaginatedMenu {

    private final UUID targetPlayerId;
    private final Map<Integer, TradeSession> tradeHistory = new ConcurrentHashMap<>();
    private boolean isLoading = true;

    @Override
    public String getPrePaginatedTitle(Player player) {
        Player targetPlayer = Bukkit.getPlayer(targetPlayerId);
        String targetName = targetPlayer != null ? targetPlayer.getName() : "Unknown";
        return CC.t("&8Trade History: &7" + targetName);
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        if (isLoading) {
            buttons.put(0, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return new ItemBuilder(Material.HOPPER)
                            .setDisplayName(CC.t("&6&lLoading..."))
                            .setLore(Arrays.asList(
                                    CC.t("&7Fetching trade history from database..."),
                                    CC.t("&7Please wait a moment.")
                            ))
                            .build();
                }
            });

            // Load data asynchronously
            loadTradeHistoryAsync(player);
            return buttons;
        }

        if (tradeHistory.isEmpty()) {
            buttons.put(0, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return new ItemBuilder(Material.BARRIER)
                            .setDisplayName(CC.t("&c&lNo Trade History"))
                            .setLore(Arrays.asList(
                                    CC.t("&7This player has no recorded trades."),
                                    CC.t("&7Trades will appear here once completed.")
                            ))
                            .build();
                }
            });
            return buttons;
        }

        int index = 0;
        for (Map.Entry<Integer, TradeSession> entry : tradeHistory.entrySet()) {
            TradeSession session = entry.getValue();
            buttons.put(index++, new TradeHistoryButton(session, targetPlayerId));
        }

        return buttons;
    }

    private void loadTradeHistoryAsync(Player viewer) {
        Bukkit.getScheduler().runTaskAsynchronously(Tausch.getInstance(), () -> {
            try {
                Document query = new Document("$or", Arrays.asList(
                        new Document("sender", targetPlayerId.toString()),
                        new Document("target", targetPlayerId.toString())
                ));

                FindIterable<Document> results = Tausch.getInstance()
                        .getMongoHandler()
                        .getTradeHistory()
                        .find(query)
                        .sort(new Document("_id", -1)) // Sort by newest first
                        .limit(1000); // Limit to prevent memory issues

                Map<Integer, TradeSession> loadedTrades = new HashMap<>();
                int index = 0;

                for (Document doc : results) {
                    try {
                        TradeSession session = TradeSession.fromBson(doc);
                        loadedTrades.put(index++, session);
                    } catch (Exception e) {
                        // Skip invalid documents
                        e.printStackTrace();
                    }
                }

                // Switch back to main thread to update UI
                Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
                    tradeHistory.clear();
                    tradeHistory.putAll(loadedTrades);
                    isLoading = false;

                    // Reopen menu to show loaded data
                    if (viewer.isOnline()) {
                        openMenu(viewer);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();

                // Handle error on main thread
                Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
                    isLoading = false;
                    if (viewer.isOnline()) {
                        viewer.sendMessage(CC.t("&cError loading trade history. Please try again."));
                        viewer.closeInventory();
                    }
                });
            }
        });
    }

    @Override
    public Map<Integer, Button> getGlobalButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        // Add decorative border
        ItemStack borderItem = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        // Fill bottom row with decorative items
        for (int i = 45; i < 54; i++) {
            buttons.put(i, new Button() {
                @Override
                public ItemStack getButtonItem(Player player) {
                    return borderItem;
                }
            });
        }

        // Add refresh button
        buttons.put(49, new Button() {
            @Override
            public ItemStack getButtonItem(Player player) {
                return new ItemBuilder(Material.EMERALD)
                        .setDisplayName(CC.t("&a&lRefresh"))
                        .setLore(Arrays.asList(
                                CC.t("&7Click to refresh the trade history"),
                                CC.t("&7and load any new trades.")
                        ))
                        .build();
            }

            @Override
            public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
                tradeHistory.clear();
                isLoading = true;
                openMenu(player);
            }
        });

        return buttons;
    }

    @Override
    public int getSize() {
        return 54; // 6 rows
    }

    @Override
    public int getMaxItemsPerPage(Player player) {
        return 28; // 4 rows of 7 items (avoiding borders)
    }


}