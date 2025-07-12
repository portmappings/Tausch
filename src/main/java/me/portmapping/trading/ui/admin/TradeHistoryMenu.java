package me.portmapping.trading.ui.admin;

import com.mongodb.client.FindIterable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import me.portmapping.trading.Tausch;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.admin.button.LoadingButton;
import me.portmapping.trading.ui.admin.button.NoTradeHistoryButton;
import me.portmapping.trading.ui.admin.button.RefreshButton;
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
@Getter
@Setter
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
            buttons.put(0, new LoadingButton());

            // Load data asynchronously
            loadTradeHistoryAsync(player);
            return buttons;
        }

        if (tradeHistory.isEmpty()) {
            buttons.put(0,new NoTradeHistoryButton());
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
                        .limit(500);

                List<TradeSession> loadedSessions = new ArrayList<>();

                for (Document doc : results) {
                    try {
                        TradeSession session = TradeSession.fromBson(doc);
                        loadedSessions.add(session);
                    } catch (Exception e) {
                        e.printStackTrace(); // Log and skip invalid
                    }
                }

                loadedSessions.sort((s1, s2) -> Long.compare(s2.getCompletedAt(), s1.getCompletedAt()));

                Map<Integer, TradeSession> loadedTrades = new LinkedHashMap<>();
                int index = 0;
                for (TradeSession session : loadedSessions) {
                    loadedTrades.put(index++, session);
                }


                Bukkit.getScheduler().runTask(Tausch.getInstance(), () -> {
                    tradeHistory.clear();
                    tradeHistory.putAll(loadedTrades);
                    isLoading = false;

                    if (viewer.isOnline()) {
                        openMenu(viewer);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();

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

        for (int i = 45; i < 54; i++) {
            buttons.put(i, Button.placeholder(Material.GRAY_STAINED_GLASS_PANE));
        }

        // Add refresh button
        buttons.put(49, new RefreshButton(this));

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