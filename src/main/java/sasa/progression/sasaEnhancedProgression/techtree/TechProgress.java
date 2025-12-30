package sasa.progression.sasaEnhancedProgression.techtree;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
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
import sasa.progression.sasaEnhancedProgression.features.AdvancementUnlockEvent;
import sasa.progression.sasaEnhancedProgression.io.TechnologyConfigReader;
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


    public Technology getTechnologyFromKey(NamespacedKey key) {
        return keyToTechnologyMap.get(key);
    }


    public void progressTechnology(Technology technology, ItemStack itemStack) {
        // note: does not check whether there is another requirement needing the same item or an element of the same tag group
        AbstractMaterialRequirement requirement = technology.getRequirements().stream().filter(
            abstractMaterialRequirement -> switch (abstractMaterialRequirement) {
                case MaterialRequirement mr -> mr.getItemType() == itemStack.getType().asItemType();
                case MaterialTagRequirement tr -> tr.getTag().contains(TypedKey.create(RegistryKey.ITEM, itemStack.getType().asItemType().getKey()));
                default -> throw new IllegalStateException();
            }
        ).findFirst().orElseThrow();
        requirement.increaseGiven(itemStack.getAmount());

        boolean allRequirementsCompleted = technology.getRequirements().stream().allMatch(AbstractMaterialRequirement::isFulfilled);
        if (allRequirementsCompleted) {
            unlockTechnology(technology);
        }
    }

    public void unlockTechnology(Technology technology) {
        if (completedTech.contains(technology)) {
            return;
        }
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

        new AdvancementUnlockEvent(technology.getAdvancementKey()).callEvent();
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

}
