package sasa.progression.sasaEnhancedProgression.techtree;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.config.TechnologyConfigReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TechProgress {

    private final HashSet<Technology> completedTech = new HashSet<>();
    private final HashSet<Technology> openTech = new HashSet<>();
    private final HashSet<Technology> openTechWithoutRequirements = new HashSet<>();
    private final HashSet<Technology> remainingTech = new HashSet<>();

    private final HashMap<NamespacedKey, Technology> keyToTechnologyMap = new HashMap<>();

    public TechProgress() {
        TechnologyConfigReader technologyConfigReader = new TechnologyConfigReader();
        // iterate over all advancements and bundle necessary information (such as dependent technologies and material requirements)
        for (var it = Bukkit.advancementIterator(); it.hasNext();) {
            Advancement advancement = it.next();
            var requirements = technologyConfigReader.getTechnologyRequirements(advancement.getKey());
            Technology technology = new Technology(advancement.getKey(), requirements);

            if (advancement.getCriteria().contains("join")) {
                completedTech.add(technology);
            }
            keyToTechnologyMap.put(advancement.getKey(), technology);
        }

        // For any given technology, if its dependencies are fulfilled, it can be added to open technologies
        for (Technology tech : keyToTechnologyMap.values()) {
            if (completedTech.contains(keyToTechnologyMap.get(tech.getPrimaryDependency())) &&
                    (tech.getSecondaryDependency() == null || completedTech.contains(keyToTechnologyMap.get(tech.getSecondaryDependency())))) {
                if (tech.hasRequirements()) {
                    openTech.add(tech);
                } else {
                    openTechWithoutRequirements.add(tech);
                }
            } else {
                remainingTech.add(tech);
            }
        }
    }

    public Set<Technology> getOpenTech() {
        return Set.copyOf(openTech);
    }

}
