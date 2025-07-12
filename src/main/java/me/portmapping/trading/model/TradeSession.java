package me.portmapping.trading.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
@Setter
@RequiredArgsConstructor
public class TradeSession {

    private final UUID sender;
    private final UUID target;

    private boolean senderConfirmed = false;
    private boolean targetConfirmed = false;

    private final List<ItemStack> senderItems = new ArrayList<>();
    private final List<ItemStack> targetItems = new ArrayList<>();

    public UUID getOther(UUID playerId) {
        if (playerId.equals(sender)) return target;
        else if (playerId.equals(target)) return sender;
        return null;
    }

    public UUID getOther(Player player) {
        return getOther(player.getUniqueId());
    }

    public boolean hasConfirmed(UUID playerId) {
        return playerId.equals(sender) ? senderConfirmed
                : playerId.equals(target) && targetConfirmed;
    }

    public void resetConfirmation() {
        senderConfirmed = false;
        targetConfirmed = false;
    }

    public void cancel() {
        senderItems.clear();
        targetItems.clear();
        senderConfirmed = false;
        targetConfirmed = false;
    }
}
