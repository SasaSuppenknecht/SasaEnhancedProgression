package sasa.progression.sasaEnhancedProgression.techtree;


import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.ItemType;
import sasa.progression.sasaEnhancedProgression.misc.ItemTagHandler;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.*;

public class Technology {

    private final NamespacedKey advancement;
    private final NamespacedKey primaryDependency;
    private final NamespacedKey secondaryDependency;

    private final int parts; // TODO
    private final List<AbstractMaterialRequirement> requirements = new ArrayList<>();

    public Technology(NamespacedKey advancement, TechnologyRequirementBundle requirementsData) {
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

        if (requirementsData != null) {
            this.parts = requirementsData.parts;

            for (Map.Entry<String, TechnologyRequirementBundle.RequirementInformationBundle> requirement : requirementsData.requirements.entrySet()) {
                // todo use scaling flag
                String key = requirement.getKey();
                if (!key.startsWith("#")) {
                    ItemType itemType = Registry.ITEM.get(Key.key(key));
                    assert itemType != null : "Key " + key + " does not correspond to any itemtype";
                    requirements.add(new MaterialRequirement(itemType, requirement.getValue().amount()));
                } else {
                    key = key.substring(1);
                    Tag<ItemType> tag = Registry.ITEM.getTag(TagKey.create(RegistryKey.ITEM, key));
                    requirements.add(new MaterialTagRequirement(tag, requirement.getValue().amount()));
                }
            }
        } else {
            parts = 1;
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
