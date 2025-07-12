package me.portmapping.trading.ui.user;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.portmapping.trading.model.TradeSession;
import me.portmapping.trading.ui.user.button.ClickableItemButton;
import me.portmapping.trading.ui.user.button.ConfirmationStatusButton;
import me.portmapping.trading.ui.user.button.InputButton;
import me.portmapping.trading.ui.user.button.StaticItemButton;
import me.portmapping.trading.utils.item.ItemBuilder;
import me.portmapping.trading.utils.menu.Button;
import me.portmapping.trading.utils.menu.Menu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@RequiredArgsConstructor
@Getter
public class TradeMenu extends Menu {

    private final TradeSession tradeSession;

    private static final int[] DIVIDER_SLOTS = {4, 13, 22, 31, 40};
    private static final int INPUT_SLOT = 39;
    private static final int CONFIRMATION_STATUS_SLOT = 41;

    private static final List<Integer> LEFT_SLOTS = Arrays.asList(
            0, 1, 2, 3,
            9, 10, 11, 12,
            18, 19, 20, 21,
            27, 28, 29, 30
    );

    private static final List<Integer> RIGHT_SLOTS = Arrays.asList(
            5, 6, 7, 8,
            14, 15, 16, 17,
            23, 24, 25, 26,
            32, 33, 34, 35
    );

    @Override
    public String getTitle(Player player) {
        UUID otherUUID = tradeSession.getOther(player.getUniqueId());
        Player other = Bukkit.getPlayer(otherUUID);
        return other != null ? other.getName() : "Unknown" + "⇄" + player.getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        addDividerButtons(buttons);
        addItemButtons(buttons, player);
        addInputButton(buttons, player);
        addConfirmationStatusButton(buttons, player);

        return buttons;
    }

    private void addDividerButtons(Map<Integer, Button> buttons) {
        ItemStack divider = new ItemBuilder(Material.BLACK_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        for (int slot : DIVIDER_SLOTS) {
            buttons.put(slot, new StaticItemButton(divider));
        }
    }

    private void addItemButtons(Map<Integer, Button> buttons, Player player) {
        UUID playerId = player.getUniqueId();
        List<ItemStack> myItems = tradeSession.getPlayerItems(playerId);
        List<ItemStack> otherItems = tradeSession.getOtherPlayerItems(playerId);

        addMyItemButtons(buttons, myItems, playerId);
        addOtherItemButtons(buttons, otherItems);
    }

    private void addMyItemButtons(Map<Integer, Button> buttons, List<ItemStack> myItems, UUID playerId) {
        for (int i = 0; i < myItems.size() && i < LEFT_SLOTS.size(); i++) {
            int itemIndex = i;
            buttons.put(LEFT_SLOTS.get(i), new ClickableItemButton(tradeSession, myItems.get(i), itemIndex));
        }
    }

    private void addOtherItemButtons(Map<Integer, Button> buttons, List<ItemStack> otherItems) {
        for (int i = 0; i < otherItems.size() && i < RIGHT_SLOTS.size(); i++) {
            buttons.put(RIGHT_SLOTS.get(i), new StaticItemButton(otherItems.get(i)));
        }
    }



    private void addInputButton(Map<Integer, Button> buttons, Player player) {
        buttons.put(INPUT_SLOT, new InputButton(tradeSession, player));
    }

    private void addConfirmationStatusButton(Map<Integer, Button> buttons, Player player) {
        buttons.put(CONFIRMATION_STATUS_SLOT, new ConfirmationStatusButton(tradeSession, player));
    }



}
