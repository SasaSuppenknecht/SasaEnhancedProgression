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

    private final Advancement advancement;
    private final Advancement primaryDependency;
    private final Advancement secondaryDependency;
    private final List<MaterialRequirement> requirements = new ArrayList();

    public Technology(Advancement advancement, HashMap<String, Integer> requirementsMap) {
        this.advancement = advancement;

        var criteriaIterator = advancement.getCriteria().iterator();
        // criteria must have at least one element
        primaryDependency = getAdvancementFromString(criteriaIterator.next());
        if (criteriaIterator.hasNext()) {
            secondaryDependency = getAdvancementFromString(criteriaIterator.next());
        } else {
            secondaryDependency = null;
        }

        assert !criteriaIterator.hasNext() : "Advancement " + advancement.getKey() + " has more than 2 criteria";

        if (requirementsMap != null) {
            for (Map.Entry<String, Integer> requirement : requirementsMap.entrySet()) {
                String key = requirement.getKey();
                Material material = Material.matchMaterial(key);
                assert material != null : "Key " + key + " does not correspond to any material";
                requirements.add(new MaterialRequirement(material, requirement.getValue()));
            }
        }
    }


    public Advancement getConnectedAdvancement() {
        return advancement;
    }

    public Advancement getPrimaryDependency() {
        return primaryDependency;
    }

    public Advancement getSecondaryDependency() {
        return secondaryDependency;
    }

    public boolean hasRequirements() {
        return !requirements.isEmpty();
    }

    private Advancement getAdvancementFromString(String key) {
        NamespacedKey advancementKey = NamespacedKey.fromString(key);
        assert advancementKey != null : "Ill-formatted namespacedkey: " + key + " in advancement " + advancement.getKey();
        Advancement advancementFromKey = Bukkit.getAdvancement(advancementKey);
        assert advancementFromKey != null : "Cannot find " + advancementKey + " coming from " + advancement.getKey();

        return advancementFromKey;
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

    }

}
