package sasa.progression.sasaEnhancedProgression.techinterface;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.misc.ItemTagHandler;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.*;
import java.util.stream.Stream;

class TechSelectionMenu implements InventoryHolder {

    private final Inventory inventory;
    private final ArrayList<ButtonTechTuple> buttonTechTuples = new ArrayList<>();

    private static List<Technology> baseTechnologies = null;
    private static final int ROW_LENGTH = 9;

    // todo menu is not update upon progress supplied by other players
    TechSelectionMenu(TechProgress techProgress) {
        this.inventory = Bukkit.createInventory(this, 6 * ROW_LENGTH, Component.text("Technology Selection"));


        if (baseTechnologies == null) {
            baseTechnologies = techProgress.getAllTechnologies()
                    .stream()
                    .filter(t -> t.depth == 0)
                    .sorted(new Technology.NameSorter())
                    .toList();
        }

        for (Technology technology : techProgress.getOpenTech()) {
            ItemStack button = createButton(technology);
            buttonTechTuples.add(new ButtonTechTuple(button, technology));
        }

        HashMap<Technology, List<ItemStack>> buttonsPerCategory = new HashMap<>();
        for (Technology baseTechnology : baseTechnologies) {
            buttonsPerCategory.put(baseTechnology, new ArrayList<>());
        }
        for (ButtonTechTuple buttonTechTuple : buttonTechTuples) {
            Technology technology = buttonTechTuple.technology;
            ItemStack button = buttonTechTuple.button;
            Technology baseTechnology = techProgress.getTechnologyFromKey(technology.baseAdvancement.getKey());

            buttonsPerCategory.get(baseTechnology).add(button);
        }

        int row = 0;
        for (Technology baseTechnology : baseTechnologies) {
            List<ItemStack> buttons = buttonsPerCategory.get(baseTechnology);
            assert buttons.size() <= 8;
            buttons.sort((o1, o2) ->
                    getTextFromComponent(o1.displayName()).compareTo(getTextFromComponent(o2.displayName()))
            );
            inventory.setItem(row * ROW_LENGTH, createCategoryIcon(baseTechnology));

            int column = 1;
            for (ItemStack button : buttons) {
                int index = row * ROW_LENGTH + column;
                inventory.setItem(index, button);
                column++;
            }
            row++;
        }
    }

    private ItemStack createButton(Technology technology) {
        Advancement advancement = technology.getConnectedAdvancement();
        ItemStack button = Objects.requireNonNull(advancement.getDisplay()).icon();
        ItemMeta itemMeta = button.getItemMeta();
        String title = getTextFromComponent(advancement.getDisplay().title());
        itemMeta.displayName(Component.text(title));
        ArrayList<Component> requirementsText = new ArrayList<>();
        // Each technology also lists total requirements and the already provided requirements in its lore
        for (AbstractMaterialRequirement requirement : technology.getRequirements()) {
            String name = switch (requirement) {
                case MaterialRequirement mr -> mr.getItemType().translationKey();
                case MaterialTagRequirement tr -> "Any " + ItemTagHandler.getTagName(tr.getTag()); // does not work for non-english languages
                default -> throw new IllegalStateException("Unexpected value: " + requirement);
            };

            Component text = Component.translatable(name)
                    .append(Component.text(":"))
                    .appendSpace()
                    .append(Component.text("%d / %d".formatted(requirement.getGiven(), requirement.getNeeded())));
            requirementsText.add(text);
        }
        itemMeta.lore(requirementsText);
        button.setItemMeta(itemMeta);
        return button;
    }

    private ItemStack createCategoryIcon(Technology technology) {
        Advancement advancement = technology.getConnectedAdvancement();
        ItemStack icon = Objects.requireNonNull(advancement.getDisplay()).icon();
        ItemMeta itemMeta = icon.getItemMeta();

        String title = getTextFromComponent(advancement.getDisplay().title());
        itemMeta.displayName(Component.text(title));
        String description = getTextFromComponent(advancement.getDisplay().description());
        itemMeta.lore(List.of(Component.text(description)));
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemMeta.addEnchant(Enchantment.LURE, 1, true);

        icon.setItemMeta(itemMeta);
        return icon;
    }

    private String getTextFromComponent(Component component) {
        return PlainTextComponentSerializer.plainText().serialize(component);
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
