package sasa.progression.sasaEnhancedProgression.techtree;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.config.TechnologyConfigReader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class TechProgress {

    private HashSet<NamespacedKey> completedTech = new HashSet<>();
    private HashSet<NamespacedKey> openTech = new HashSet<>();
    private HashSet<NamespacedKey> openTechWithoutRequirements = new HashSet<>();
    private HashSet<NamespacedKey> remainingTech = new HashSet<>();

    public TechProgress() {
        TechnologyConfigReader technologyConfigReader = new TechnologyConfigReader();
        ArrayList<Technology> technologies = new ArrayList<>();
        // iterate over all advancements and bundle necessary information (such as dependent technologies and material requirements)
        for (var it = Bukkit.advancementIterator(); it.hasNext();) {
            Advancement advancement = it.next();
            var requirements = technologyConfigReader.getTechnologyRequirements(advancement.getKey());
            Technology technology = new Technology(advancement, requirements);

            if (advancement.getCriteria().contains("join")) {
                completedTech.add(advancement.getKey());
            } else {
                technologies.add(technology);
            }
        }

        // For any given technology, if its dependencies are fulfilled, it can be added to open technologies
        for (Technology tech : technologies) {
            if (completedTech.contains(tech.getPrimaryDependency().getKey()) &&
                    (tech.getSecondaryDependency() == null || completedTech.contains(tech.getSecondaryDependency().getKey()))) {
                if (tech.hasRequirements()) {
                    openTech.add(tech.getConnectedAdvancement().getKey());
                } else {
                    openTechWithoutRequirements.add(tech.getConnectedAdvancement().getKey());
                }
            } else {
                remainingTech.add(tech.getConnectedAdvancement().getKey());
            }
        }
    }

    public Set<Advancement> getOpenTech() {
        HashSet<Advancement> openTechAdvancements = new HashSet<>();
        for (NamespacedKey key : openTech) {
            openTechAdvancements.add(Bukkit.getAdvancement(key));
        }
        return openTechAdvancements;
    }

}
