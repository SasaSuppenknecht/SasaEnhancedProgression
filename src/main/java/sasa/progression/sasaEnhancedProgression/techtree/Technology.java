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

    private final int parts = 1; // TODO
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

    public int getParts() {
        return parts;
    }

    public boolean hasNoRequirements() {
        return requirements.isEmpty();
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

}
