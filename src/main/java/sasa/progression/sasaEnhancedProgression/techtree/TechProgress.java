package sasa.progression.sasaEnhancedProgression.techtree;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.io.TechnologyConfigReader;

import java.util.*;

public class TechProgress implements Listener {

    private final HashSet<Technology> completedTech = new HashSet<>();
    private final HashSet<Technology> openTech = new HashSet<>();
    private final HashSet<Technology> remainingTech = new HashSet<>();

    private final HashMap<NamespacedKey, Technology> keyToTechnologyMap = new HashMap<>();

    public TechProgress(TechnologyConfigReader technologyConfigReader) {
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
                openTech.add(tech);
//                if (tech.hasNoRequirements()) {
//                    unlockTechnology(tech);
//                }
            } else {
                remainingTech.add(tech);
            }
        }

        Bukkit.getServer().getPluginManager().registerEvents(this, SasaEnhancedProgression.plugin);
    }

    public Set<Technology> getOpenTech() {
        return Set.copyOf(openTech);
    }

    public void progressTechnology(Technology technology, ItemStack itemStack) {
        System.out.println(itemStack.getType().name());
        System.out.println(technology.getRequirements());
        MaterialRequirement requirement = technology.getRequirements().stream().filter(
                materialRequirement -> materialRequirement.getMaterial() == itemStack.getType()
        ).findFirst().orElseThrow();
        requirement.increaseGiven(itemStack.getAmount());

        boolean allRequirementsCompleted = technology.getRequirements().stream().allMatch(MaterialRequirement::isFulfilled);
        if (allRequirementsCompleted) {
            unlockTechnology(technology);
        }
    }

    public void unlockTechnology(Technology technology) {
        assert openTech.contains(technology);
        openTech.remove(technology);
        completedTech.add(technology);

        for (Iterator<Technology> it = remainingTech.iterator(); it.hasNext(); ) {
            Technology tech = it.next();
            Technology primaryDependency = keyToTechnologyMap.get(tech.getPrimaryDependency());
            if (!completedTech.contains(primaryDependency)) {
                continue;
            }

            if (tech.getSecondaryDependency() != null) {
                Technology secondaryDependency = keyToTechnologyMap.get(tech.getSecondaryDependency());
                if (!completedTech.contains(secondaryDependency)) {
                    continue;
                }
            }

            it.remove();
            openTech.add(tech);
            if (tech.hasNoRequirements()) {
                // unlock technology on next server tick
                Bukkit.getScheduler().runTaskLater(SasaEnhancedProgression.plugin, () -> unlockTechnology(tech), 1);
            }
        }

        for (Player player : Bukkit.getOnlinePlayers()) {
            awardAdvancement(player, technology.getAdvancementKey());
        }
    }


    private void awardAdvancement(Player player, NamespacedKey key) {
        AdvancementProgress advancementProgress = player.getAdvancementProgress(Bukkit.getAdvancement(key));
        for (String criteria : advancementProgress.getRemainingCriteria()) {
            advancementProgress.awardCriteria(criteria);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        System.out.println(completedTech);
        for (Technology technology : completedTech) {
            awardAdvancement(event.getPlayer(), technology.getAdvancementKey());
        }
    }

}
