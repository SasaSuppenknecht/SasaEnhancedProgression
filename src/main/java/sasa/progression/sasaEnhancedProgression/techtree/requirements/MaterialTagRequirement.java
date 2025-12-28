package sasa.progression.sasaEnhancedProgression.techtree.requirements;

import io.papermc.paper.registry.tag.Tag;
import org.bukkit.inventory.ItemType;

public class MaterialTagRequirement extends AbstractMaterialRequirement {

    private final Tag<ItemType> tag;

    public MaterialTagRequirement(Tag<ItemType> tag, int needed) {
        super(needed);
        this.tag = tag;
    }

    public Tag<ItemType> getTag() {
        return tag;
    }

    @Override
    public String toString() {
        return "%s (%d / %d)".formatted(tag.toString(), given, needed);
    }
}