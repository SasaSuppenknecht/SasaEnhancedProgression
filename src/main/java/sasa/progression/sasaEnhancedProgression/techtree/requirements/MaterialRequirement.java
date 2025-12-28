package sasa.progression.sasaEnhancedProgression.techtree.requirements;

import org.bukkit.inventory.ItemType;

public class MaterialRequirement extends AbstractMaterialRequirement {

    private final ItemType itemType;

    public MaterialRequirement(ItemType material, int needed) {
        super(needed);
        this.itemType = material;
    }

    public ItemType getItemType() {
        return itemType;
    }

    @Override
    public String toString() {
        return "%s (%d / %d)".formatted(itemType.translationKey(), given, needed);
    }
}