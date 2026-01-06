package sasa.progression.sasaEnhancedProgression.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;

public class TechnologyTimeoutEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Technology technology;

    public TechnologyTimeoutEvent(Player player, Technology technology) {
        this.player = player;
        this.technology = technology;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return HANDLER_LIST;
    }

    public Player getPlayer() {
        return player;
    }

    public Technology getTechnology() {
        return technology;
    }
}
