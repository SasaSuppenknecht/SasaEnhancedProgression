package sasa.progression.sasaEnhancedProgression.techinterface;

import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.misc.ItemTagHandler;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.HashMap;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
class TechResearchMenu implements InventoryHolder {

    private final Technology technology;
    private final Inventory inventory;

    TechResearchMenu(Technology technology, Player player) {
        this.technology = technology;

        String technologyName = PlainTextComponentSerializer.plainText().serialize(technology.getConnectedAdvancement().displayName());
        technologyName = technologyName.substring(1, technologyName.length() - 1); // remove [ and ]
        inventory = Bukkit.createInventory(this, 9 * 6, Component.text(technologyName));


        ItemStack gray = createNamedItemStack(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, gray);
        }

        ItemStack acceptButton = createNamedItemStack(Material.LIME_STAINED_GLASS_PANE, "Research!");
        inventory.setItem(53, acceptButton);

        int parts = technology.getParts();
        List<AbstractMaterialRequirement> requirements = technology.getRequirements();

        int index = 0;
        assert requirements.size() <= 9;
        for (AbstractMaterialRequirement requirement : requirements) {
            int partSize = Math.ceilDiv(requirement.getNeeded(), parts);
            ItemType itemType;
            switch (requirement) {
                case MaterialRequirement mr -> {
                    itemType = mr.getItemType();
                }
                case MaterialTagRequirement tr -> {
                    Tag<ItemType> tag = tr.getTag();
                    // Pick one random material from the materials behind tag
                    itemType = ItemTagHandler.getRandomItemFromItemTag(tag);
                    assert itemType != null;
                }
                default -> throw new IllegalStateException("Unexpected value: " + requirement);
            }

            ItemStack item = itemType.createItemStack();
            ItemMeta meta = item.getItemMeta();
            meta.lore(List.of(
                    Component.text("Part: %d / %d".formatted(requirement.getGiven(), partSize)), // todo track part progress
                    Component.text("Total: %d / %d".formatted(requirement.getGiven(), requirement.getNeeded()))
            ));
            item.setItemMeta(meta);

            inventory.setItem(index, item);
            index++;
        }
        ItemStack light_gray = createNamedItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "");
        for (; index < 9; index++) {
            inventory.setItem(index, light_gray);
        }
    }

    private ItemStack createNamedItemStack(Material material, String name) {
        ItemStack item = ItemStack.of(material);
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text(name));
        item.setItemMeta(itemMeta);
        return item;
    }

    public void updateInventory(HashMap<ItemType, Integer> usedItems) {

    }

    public void updateButtons() {

    }

    Technology getTechnology() {
        return technology;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
