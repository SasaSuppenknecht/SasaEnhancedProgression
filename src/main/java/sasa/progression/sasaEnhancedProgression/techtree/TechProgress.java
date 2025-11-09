package sasa.progression.sasaEnhancedProgression.techtree;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import sasa.progression.sasaEnhancedProgression.config.TechnologyConfigReader;

import java.util.ArrayList;
import java.util.HashSet;

public class TechProgress {

    private HashSet<Advancement> completedTech = new HashSet<>();
    private HashSet<Advancement> openTech = new HashSet<>();
    private HashSet<Advancement> remainingTech = new HashSet<>();

    public TechProgress() {
        TechnologyConfigReader technologyConfigReader = new TechnologyConfigReader();
        ArrayList<Technology> technologies = new ArrayList<>();
        // iterate over all advancements and bundle necessary information (such as dependent technologies and material requirements)
        for (var it = Bukkit.advancementIterator(); it.hasNext();) {
            Advancement advancement = it.next();
            var requirements = technologyConfigReader.getTechnologyRequirements(advancement.getKey());
            Technology technology = new Technology(advancement, requirements);

            if (advancement.getCriteria().contains("join")) {
                completedTech.add(advancement);
            } else {
                technologies.add(technology);
            }
        }

        // For any given technology, if its dependencies are fulfilled, it can be added to open technologies
        for (Technology tech : technologies) {
            if (completedTech.contains(tech.getPrimaryDependency()) &&
                    (tech.getSecondaryDependency() == null || completedTech.contains(tech.getSecondaryDependency()))) {
                if (tech.hasRequirements()) {
                    openTech.add(tech.getConnectedAdvancement());
                }
            } else {
                remainingTech.add(tech.getConnectedAdvancement());
            }
        }
    }

}
