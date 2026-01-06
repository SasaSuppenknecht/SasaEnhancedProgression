package sasa.progression.sasaEnhancedProgression.events;

import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class TechnologyUnlockEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final NamespacedKey advancementKey;

    public TechnologyUnlockEvent(NamespacedKey advancementKey) {
        this.advancementKey = advancementKey;
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
