package sasa.progression.sasaEnhancedProgression.techtree;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemType;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.events.TechnologyProgressEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyTimeoutEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;
import sasa.progression.sasaEnhancedProgression.io.TechnologyConfigReader;
import sasa.progression.sasaEnhancedProgression.misc.ItemTagHandler;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.*;

public class TechProgress implements Listener {

    private final HashSet<Technology> completedTech = new HashSet<>();
    private final HashSet<Technology> openTech = new HashSet<>();
    private final HashSet<Technology> remainingTech = new HashSet<>();

    private final HashMap<NamespacedKey, Technology> keyToTechnologyMap = new HashMap<>();

    public TechProgress() {
        // iterate over all advancements and bundle necessary information (such as dependent technologies and material requirements)
        TechnologyConfigReader technologyConfigReader = new TechnologyConfigReader();

        for (var it = Bukkit.advancementIterator(); it.hasNext();) {
            Advancement advancement = it.next();
            var requirements = technologyConfigReader.getTechnologyRequirements(advancement.getKey());
            Technology technology = new Technology(advancement, requirements);

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
            } else {
                remainingTech.add(tech);
            }
        }

        Bukkit.getServer().getPluginManager().registerEvents(this, SasaEnhancedProgression.plugin);
    }

    public Set<Technology> getOpenTech() {
        return Set.copyOf(openTech);
    }

    public Set<Technology> getCompletedTech() {
        return Set.copyOf(completedTech);
    }

    public Set<Technology> getRemainingTech() {
        return Set.copyOf(remainingTech);
    }

    public Collection<Technology> getAllTechnologies() { return keyToTechnologyMap.values(); }

    public Technology getTechnologyFromKey(NamespacedKey key) {
        return keyToTechnologyMap.get(key);
    }

    public boolean progressTechnology(Player player, Technology technology, HashMap<ItemType, Integer> availableAmountPerItemType) {
        boolean progressed = false;
        int[] partProgressAmounts = technology.getPartProgress(player);

        for (Map.Entry<ItemType, Integer> item : availableAmountPerItemType.entrySet()) {
            ItemType itemType = item.getKey();
            int amount = item.getValue();
            Optional<AbstractMaterialRequirement> optionalRequirement = technology.getRequirements().stream().filter(
                    abstractMaterialRequirement -> switch (abstractMaterialRequirement) {
                        case MaterialRequirement mr -> mr.getItemType() == itemType;
                        case MaterialTagRequirement tr -> ItemTagHandler.isItemTypeInItemTag(tr.getTag(), itemType);
                        default -> throw new IllegalStateException();
                    }
            ).findFirst();
            if (optionalRequirement.isEmpty()) continue;

            AbstractMaterialRequirement requirement = optionalRequirement.get();

            if (requirement.isFulfilled()) continue;

            int requirementIndex = technology.getRequirements().indexOf(requirement);
            int partMaxSize = Math.ceilDiv(requirement.getNeeded(), technology.getParts());

            int remaining = Math.min(requirement.getNeeded() - requirement.getGiven(), partMaxSize - partProgressAmounts[requirementIndex]);
            int actualAmountUsed = amount - remaining;
            int used;
            if (actualAmountUsed >= 0) {
                requirement.increaseGiven(remaining);
                used = remaining;
            } else {
                requirement.increaseGiven(amount);
                used = amount;
            }

            if (used >= 0) {
                progressed = true;
                technology.updatePartProgress(player, requirementIndex, partProgressAmounts[requirementIndex] + used);
                partProgressAmounts = technology.getPartProgress(player);
                new TechnologyProgressEvent(player, technology, requirement, itemType, used).callEvent();

                boolean allRequirementsCompleted = technology.getRequirements().stream().allMatch(AbstractMaterialRequirement::isFulfilled);
                if (allRequirementsCompleted) {
                    unlockTechnology(technology);
                }
            }
        }

        return progressed;
    }


    public void unlockTechnology(Technology technology) {
        if (completedTech.contains(technology)) {
            return;
        }
        //assert openTech.contains(technology) : technology.toString();
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

        new TechnologyUnlockEvent(technology.getAdvancementKey()).callEvent();
        for (Player player : Bukkit.getOnlinePlayers()) {
            awardAdvancement(player, technology.getAdvancementKey());
        }
    }


    private void awardAdvancement(Player player, NamespacedKey key) {
        Advancement advancement = Bukkit.getAdvancement(key);
        assert advancement != null;
        AdvancementProgress advancementProgress = player.getAdvancementProgress(advancement);
        for (String criteria : advancementProgress.getRemainingCriteria()) {
            advancementProgress.awardCriteria(criteria);
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        for (Technology technology : completedTech) {
            awardAdvancement(event.getPlayer(), technology.getAdvancementKey());
        }
    }

    @EventHandler
    public void onTechnologyTimeout(TechnologyTimeoutEvent event) {
        Player player = event.getPlayer();
        event.getTechnology().resetPartProgress(player);
    }
}
