package sasa.progression.sasaEnhancedProgression.techtree;


import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Technology {

    private final NamespacedKey advancement;
    private final NamespacedKey primaryDependency;
    private final NamespacedKey secondaryDependency;
    private final List<MaterialRequirement> requirements = new ArrayList<>();

    public Technology(NamespacedKey advancement, HashMap<String, Integer> requirementsMap) {
        this.advancement = advancement;

        var criteriaIterator = Bukkit.getAdvancement(advancement).getCriteria().iterator();
        // criteria must have at least one element
        primaryDependency = NamespacedKey.fromString(criteriaIterator.next());
        if (criteriaIterator.hasNext()) {
            secondaryDependency = NamespacedKey.fromString(criteriaIterator.next());
        } else {
            secondaryDependency = null;
        }

        assert !criteriaIterator.hasNext() : "Advancement " + advancement + " has more than 2 criteria";

        if (requirementsMap != null) {
            for (Map.Entry<String, Integer> requirement : requirementsMap.entrySet()) {
                String key = requirement.getKey();
                Material material = Material.matchMaterial(key);
                assert material != null : "Key " + key + " does not correspond to any material";
                requirements.add(new MaterialRequirement(material, requirement.getValue()));
            }
        }
    }

    public NamespacedKey getAdvancementKey() {
        return advancement;
    }

    public Advancement getConnectedAdvancement() {
        return Bukkit.getAdvancement(advancement);
    }

    public NamespacedKey getPrimaryDependency() {
        return primaryDependency;
    }

    public NamespacedKey getSecondaryDependency() {
        return secondaryDependency;
    }

    public boolean hasRequirements() {
        return !requirements.isEmpty();
    }

    public List<MaterialRequirement> getRequirements() {
        return List.copyOf(requirements);
    }


    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Technology technology) {
            return technology.advancement.equals(this.advancement);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Technology(%s)".formatted(advancement.toString());
    }

    public static class MaterialRequirement {

        private final Material material;
        private int needed;
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

        public void setGiven(int given) {
            this.given = given;
        }

        public int getNeeded() {
            return needed;
        }

        public void setNeeded(int needed) {
            this.needed = needed;
        }
    }

}
