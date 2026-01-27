package sasa.progression.sasaEnhancedProgression.techinterface;

import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.events.TechnologyProgressEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyTimeoutEvent;
import sasa.progression.sasaEnhancedProgression.misc.ItemTagHandler;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
class TechResearchMenu implements InventoryHolder, Listener {

    private final Technology technology;
    private final Inventory inventory;
    private final Player player;

    private final List<ItemTagAnimation> animations = new ArrayList<>();
    private int requirementSize = 0;


    TechResearchMenu(Technology technology, Player player) {
        this.technology = technology;
        this.player = player;

        String technologyName = PlainTextComponentSerializer.plainText().serialize(technology.getConnectedAdvancement().displayName());
        technologyName = technologyName.substring(1, technologyName.length() - 1); // remove [ and ]
        inventory = Bukkit.createInventory(this, 9 * 6, Component.text(technologyName));

        ItemStack acceptButton = createNamedItemStack(Material.LIME_STAINED_GLASS_PANE, "Research!");
        inventory.setItem(53, acceptButton);
        ItemStack backButton = createNamedItemStack(Material.BLUE_STAINED_GLASS_PANE, "Back to Selection.");
        inventory.setItem(45, backButton);

        ItemStack gray = createNamedItemStack(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, gray);
        }

        List<AbstractMaterialRequirement> requirements = technology.getRequirements();

        assert requirements.size() <= 9;
        for (AbstractMaterialRequirement requirement : requirements) {

            ItemType itemType = switch (requirement) {
                case MaterialRequirement mr -> mr.getItemType();
                // Pick one random material from the materials behind tag
                case MaterialTagRequirement tr -> {
                    Tag<@NotNull ItemType> tag = tr.getTag();
                    yield ItemTagHandler.getRandomItemFromItemTag(tag);
                }
                default -> throw new RuntimeException("Unexpected value: " + requirement);
            };

            assert itemType != null;

            ItemStack item = itemType.createItemStack();

            if (requirement instanceof MaterialTagRequirement tr) {
                ItemMeta meta = item.getItemMeta();
                meta.displayName(Component.text("Any " + ItemTagHandler.getTagName(tr.getTag())));
                item.setItemMeta(meta);

                animations.add(new ItemTagAnimation(requirementSize, tr.getTag()));
            }

            inventory.setItem(requirementSize, item);
            requirementSize++;
        }
        ItemStack light_gray = createNamedItemStack(Material.LIGHT_GRAY_STAINED_GLASS_PANE, "");
        for (int index = requirementSize; index < 9; index++) {
            inventory.setItem(index, light_gray);
        }

        updateGUI();
    }


    private void updateGUI() {
        List<AbstractMaterialRequirement> requirements = technology.getRequirements();
        int parts = technology.getParts();
        int[] partProgress = technology.getPartProgress(player);

        for (int i = 0; i < requirementSize; i++) {
            AbstractMaterialRequirement requirement = requirements.get(i);
            int partSize = Math.ceilDiv(requirement.getNeeded(), parts);

            ItemStack item = inventory.getItem(i);
            assert item != null;

            ItemMeta meta = item.getItemMeta();
            meta.lore(List.of(
                    Component.text("Part: %d / %d".formatted(partProgress[i], partSize)),
                    Component.text("Total: %d / %d".formatted(requirement.getGiven(), requirement.getNeeded()))
            ));
            if (requirement.isFulfilled()) {
                meta.addEnchant(Enchantment.LURE, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            item.setItemMeta(meta);
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
        if (event.getTechnology() == technology) {
            if (inventory.getViewers().contains(player)) {
                ItemType itemType = event.getItemType();
                int amount = event.getAmount();

                int index = 18;
                while (amount > 0 && index < 53) {
                    if (index == 45) {
                        index++;
                        continue;
                    }
                    ItemStack itemStack = inventory.getItem(index);
                    index++;
                    if (itemStack == null || itemStack.getType().asItemType() != itemType) continue;
                    int itemStackAmount = itemStack.getAmount();
                    int toSubtract = Math.min(amount, itemStackAmount);
                    itemStack.subtract(toSubtract);
                    amount -= toSubtract;
                }
                assert amount <= 0;
            }
            updateGUI();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    void onTechnologyTimeoutEvent(TechnologyTimeoutEvent event) {
        if (event.getPlayer() == player) {
            updateGUI();
        }
    }

    void startAnimation() {
        for (ItemTagAnimation itemTagAnimation : animations) {
            itemTagAnimation.startAnimation();
        }
    }

    void stopAnimation() {
        for (ItemTagAnimation itemTagAnimation : animations) {
            itemTagAnimation.stopAnimation();
        }
    }

    public Technology getTechnology() {
        return technology;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    private class ItemTagAnimation {

        private final int itemSlotIndex;
        private final Tag<ItemType> tag;

        private int itemIndex = 0;
        private int taskID = -1;

        ItemTagAnimation(int itemSlotIndex, Tag<ItemType> tag) {
            this.itemSlotIndex = itemSlotIndex;
            this.tag = tag;
        }

        void startAnimation() {
            List<ItemType> itemTypeList = ItemTagHandler.getItemTypesInItemTag(tag).stream().toList();
            taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(SasaEnhancedProgression.plugin, () -> {
                itemIndex = (itemIndex + 1) % itemTypeList.size();
                ItemStack newItemStack = itemTypeList.get(itemIndex).createItemStack();

                ItemStack itemStack = inventory.getItem(itemSlotIndex);
                assert itemStack != null;

                ItemMeta meta = itemStack.getItemMeta();
                newItemStack.setItemMeta(meta);
                inventory.setItem(itemSlotIndex, newItemStack);
            }, 8, 20);
        }

        void stopAnimation() {
            Bukkit.getScheduler().cancelTask(taskID);
        }
    }
}
