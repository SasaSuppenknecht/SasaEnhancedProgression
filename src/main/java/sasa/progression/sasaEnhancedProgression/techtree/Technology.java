package sasa.progression.sasaEnhancedProgression.techtree;


import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.misc.ItemTagHandler;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.*;

public class Technology implements Comparable<Technology> {

    private final NamespacedKey advancementKey;
    private final NamespacedKey primaryDependency;
    private final NamespacedKey secondaryDependency;

    private final int parts;
    private final List<AbstractMaterialRequirement> requirements = new ArrayList<>();
    private final HashMap<Player, int[]> partProgressMap = new HashMap<>();

    public final int depth;

    public Technology(Advancement advancement, TechnologyRequirementBundle requirementsData) {
        this.advancementKey = advancement.getKey();

        var criteriaIterator = advancement.getCriteria().iterator();
        // criteria must have at least one element
        primaryDependency = NamespacedKey.fromString(criteriaIterator.next());
        if (criteriaIterator.hasNext()) {
            secondaryDependency = NamespacedKey.fromString(criteriaIterator.next());
        } else {
            secondaryDependency = null;
        }

        assert !criteriaIterator.hasNext() : "Advancement " + advancement + " has more than 2 criteria";

        if (requirementsData != null) {
            this.parts = requirementsData.parts;

            for (Map.Entry<String, Integer> requirement : requirementsData.requirements.entrySet()) {
                String key = requirement.getKey();
                if (!key.startsWith("#")) {
                    ItemType itemType = Registry.ITEM.get(Key.key(key));
                    assert itemType != null : "Key " + key + " does not correspond to any itemtype";
                    requirements.add(new MaterialRequirement(itemType, requirement.getValue()));
                } else {
                    key = key.substring(1);
                    Tag<ItemType> tag = Registry.ITEM.getTag(TagKey.create(RegistryKey.ITEM, key));
                    requirements.add(new MaterialTagRequirement(tag, requirement.getValue()));
                }
            }
        } else {
            parts = 1;
        }

        int parentCount = 0;
        Advancement parent = advancement.getParent();
        while (parent != null) {
            parent = parent.getParent();
            parentCount++;
        }
        depth = parentCount;
    }

    public NamespacedKey getAdvancementKey() {
        return advancementKey;
    }

    public Advancement getConnectedAdvancement() {
        return Bukkit.getAdvancement(advancementKey);
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

    public List<AbstractMaterialRequirement> getRequirements() {
        return List.copyOf(requirements);
    }

    public Set<ItemType> getRemainingRequirementsItemTypes() {
        Set<ItemType> requirementsItemTypes = new HashSet<>();
        for (AbstractMaterialRequirement requirement : requirements) {
            if (!requirement.isFulfilled()) {
                switch (requirement) {
                    case MaterialRequirement mr -> requirementsItemTypes.add(mr.getItemType());
                    case MaterialTagRequirement tr ->
                            requirementsItemTypes.addAll(ItemTagHandler.getItemTypesInItemTag(tr.getTag()));
                    default -> throw new RuntimeException();
                }
            }
        }
        return requirementsItemTypes;
    }

    public int[] getPartProgress(Player player) {
        if (!partProgressMap.containsKey(player)) {
            return new int[requirements.size()];
        } else {
            return partProgressMap.get(player).clone();
        }
    }

    public void updatePartProgress(Player player, int index, int value) {
        if (player == null) return;
        if (!partProgressMap.containsKey(player)) {
            partProgressMap.put(player, new int[requirements.size()]);
        }
        partProgressMap.get(player)[index] = value;
    }

    public void resetPartProgress(Player player) {
        partProgressMap.remove(player);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Technology technology) {
            return technology.advancementKey.equals(this.advancementKey);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Technology(%s)".formatted(advancementKey.toString());
    }

    @Override
    public int compareTo(@NotNull Technology o) {
        return this.depth - o.depth;
    }


}
