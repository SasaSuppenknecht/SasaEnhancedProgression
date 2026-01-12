package sasa.progression.sasaEnhancedProgression.techinterface;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

class TechSelectionMenu implements InventoryHolder {

    private final Inventory inventory;
    private final ArrayList<ButtonTechTuple> buttonTechTuples = new ArrayList<>();

    TechSelectionMenu(Set<Technology> openTech) {
        this.inventory = Bukkit.createInventory(this, 4 * 9, Component.text("Technology Selection"));

        // todo sort technologies
        for (Technology technology : openTech) {
            Advancement advancement = technology.getConnectedAdvancement();
            ItemStack icon = Objects.requireNonNull(advancement.getDisplay()).icon();

            ItemMeta itemMeta = icon.getItemMeta();
            String title = PlainTextComponentSerializer.plainText().serialize(advancement.getDisplay().title());
            itemMeta.displayName(Component.text(title));
            ArrayList<Component> requirementsText = new ArrayList<>();
            // Each technology also lists total requirements and the already provided requirements in its lore
            for (AbstractMaterialRequirement requirement : technology.getRequirements()) {
                String name = switch (requirement) {
                    case MaterialRequirement mr -> mr.getItemType().translationKey();
                    case MaterialTagRequirement tr -> "Any " + tr.getTag().tagKey().toString().split("\\s|:")[1].replace("_", " "); // does not work for non-english languages
                    default -> throw new IllegalStateException("Unexpected value: " + requirement);
                };

                Component text = Component.translatable(name)
                        .append(Component.text(":"))
                        .appendSpace()
                        .append(Component.text("%d / %d".formatted(requirement.getGiven(), requirement.getNeeded())));
                requirementsText.add(text);
            }
            itemMeta.lore(requirementsText);
            icon.setItemMeta(itemMeta);

            inventory.addItem(icon);
            buttonTechTuples.add(new ButtonTechTuple(icon, technology));
        }
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    /**
     * @param pressedButton The pressed ItemStack
     * @return The associated {@link Technology} with this ItemStack button
     */
    public Technology buttonPress(ItemStack pressedButton) {
        Optional<ButtonTechTuple> result = buttonTechTuples.stream()
                .filter(t -> t.button.isSimilar(pressedButton))
                .findFirst();

        if (result.isEmpty()) return null;

        return result.get().technology();
    }


    private record ButtonTechTuple(ItemStack button, Technology technology) {}
}
