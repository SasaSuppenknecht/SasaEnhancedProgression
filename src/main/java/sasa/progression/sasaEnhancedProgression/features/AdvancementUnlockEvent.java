package sasa.progression.sasaEnhancedProgression.features;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class AdvancementUnlockEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final NamespacedKey advancementKey;

    public AdvancementUnlockEvent(NamespacedKey advancementKey) {
        this.advancementKey = advancementKey;
    }

    @Override
    public boolean callEvent() {
        return super.callEvent();
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public NamespacedKey getAdvancementKey() {
        return advancementKey;
    }
}
