package sasa.progression.sasaEnhancedProgression.techinterface;

import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.events.TechnologyProgressEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyTimeoutEvent;
import sasa.progression.sasaEnhancedProgression.misc.ItemTagHandler;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
class TechResearchMenu implements InventoryHolder, Listener {

    private final Technology technology;
    private final Inventory inventory;
    private final Player player;

    TechResearchMenu(Technology technology, Player player) {
        this.technology = technology;
        this.player = player;

        String technologyName = PlainTextComponentSerializer.plainText().serialize(technology.getConnectedAdvancement().displayName());
        technologyName = technologyName.substring(1, technologyName.length() - 1); // remove [ and ]
        inventory = Bukkit.createInventory(this, 9 * 6, Component.text(technologyName));

        ItemStack acceptButton = createNamedItemStack(Material.LIME_STAINED_GLASS_PANE, "Research!");
        inventory.setItem(53, acceptButton);

        ItemStack gray = createNamedItemStack(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, gray);
        }

        updateGUI();
    }


    private void updateGUI() {
        int parts = technology.getParts();
        List<AbstractMaterialRequirement> requirements = technology.getRequirements();

        int[] partProgress = technology.getPartProgress(player);

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
                default -> throw new RuntimeException("Unexpected value: " + requirement);
            }

            // todo properly name item when tag is involved
            ItemStack item = itemType.createItemStack();
            ItemMeta meta = item.getItemMeta();
            meta.lore(List.of(
                    Component.text("Part: %d / %d".formatted(partProgress[index], partSize)),
                    Component.text("Total: %d / %d".formatted(requirement.getGiven(), requirement.getNeeded()))
            ));
            item.setItemMeta(meta);

            inventory.setItem(index, item);
            index++;

            // todo different visualization for finished requirements
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

    @EventHandler
    public void onTechnologyProgressEvent(TechnologyProgressEvent event) {
        if (event.getTechnology() == technology && inventory.getViewers().contains(event.getPlayer())) {
            ItemType itemType = event.getItemType();
            int amount = event.getAmount();

            int index = 18;
            while (amount > 0 && index < 53) {
                ItemStack itemStack = inventory.getItem(index);
                index++;
                if (itemStack == null || itemStack.getType().asItemType() != itemType) continue;
                int itemStackAmount = itemStack.getAmount();
                int toSubtract = Math.min(amount, itemStackAmount);
                itemStack.subtract(toSubtract);
                amount -= toSubtract;
            }
            assert amount <= 0;

            updateGUI();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onTechnologyTimeoutEvent(TechnologyTimeoutEvent event) {
        if (event.getPlayer() == player) {
            updateGUI();
        }
    }

    public Technology getTechnology() {
        return technology;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
