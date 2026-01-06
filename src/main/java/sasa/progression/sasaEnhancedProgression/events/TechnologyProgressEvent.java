package sasa.progression.sasaEnhancedProgression.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;

import java.util.HashMap;

public class TechnologyProgressEvent extends Event {

    private static final HandlerList HANDLER_LIST = new HandlerList();

    private final Player player;
    private final Technology technology;
    private final AbstractMaterialRequirement requirement;
    private final ItemType itemType;
    private final int amount;


    public TechnologyProgressEvent(Player player,
                                   Technology technology,
                                   AbstractMaterialRequirement requirement,
                                   ItemType itemType,
                                   int amount) {
        this.player = player;
        this.technology = technology;
        this.requirement = requirement;
        this.itemType = itemType;
        this.amount = amount;
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

    public AbstractMaterialRequirement getRequirement() {
        return requirement;
    }

    public ItemType getItemType() {
        return itemType;
    }

    public int getAmount() {
        return amount;
    }
}
