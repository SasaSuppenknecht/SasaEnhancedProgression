package sasa.progression.sasaEnhancedProgression.techtree;

import org.bukkit.Material;

public class MaterialRequirement {

    private final Material material;
    private final int needed;
    private int given;

    public MaterialRequirement(Material material, int needed) {
        this.material = material;
        this.needed = needed;
        this.given = 0;
    }

    public Material getMaterial() {
        return material;
    }

    public int getGiven() {
        return given;
    }

    public void increaseGiven(int additional) {
        given += additional;
        if (given > needed) {
            given = needed;
        }
    }

    public int getNeeded() {
        return needed;
    }

    public boolean isFulfilled() {
        return given >= needed;
    }

    @Override
    public String toString() {
        return "%s (%d / %d)".formatted(material.name(), given, needed);
    }
}