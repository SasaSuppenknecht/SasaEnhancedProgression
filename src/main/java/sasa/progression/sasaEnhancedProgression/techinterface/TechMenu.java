package sasa.progression.sasaEnhancedProgression.techinterface;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.*;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class TechMenu implements Listener {


    private final TechProgress techProgress;
    private final TechTimeout techTimeout;

    TechMenu(TechProgress techProgress) {
        this.techProgress = techProgress;
        this.techTimeout = new TechTimeout();
        SasaEnhancedProgression.plugin.getServer().getPluginManager().registerEvents(this, SasaEnhancedProgression.plugin);
    }


    void openSelectionMenuForPlayer(Player player) {
        // If player has already invested in research before, the player may not choose a different one until some
        // time later
        if (techTimeout.playerHasActiveResearch(player)) {
            TechResearchMenu techResearchMenu = techTimeout.getActiveResearchOfPlayer(player);
            player.openInventory(techResearchMenu.getInventory());
            return;
        }

        player.openInventory(new TechSelectionMenu(techProgress.getOpenTech()).getInventory());
    }


    void openResearchMenuForPlayer(Technology technology, Player player) {
        TechResearchMenu activeResearch = techTimeout.getActiveResearchOfPlayer(player);
        if (activeResearch == null) {
            activeResearch = new TechResearchMenu(technology, player);
        }

        player.openInventory(activeResearch.getInventory());
    }


    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = event.getInventory();

        InventoryHolder inventoryHolder = inventory.getHolder(false);
        if (inventoryHolder instanceof TechSelectionMenu techSelectionMenu) {
            if (event.isShiftClick()) {
                event.setCancelled(true);
            } else if (clickedInventory == inventory) {
                event.setCancelled(true);
                ItemStack getClickedItem = event.getCurrentItem();
                Technology selectedTechnology = techSelectionMenu.buttonPress(getClickedItem);
                openResearchMenuForPlayer(selectedTechnology, (Player) event.getWhoClicked());
            }
        } else if (inventoryHolder instanceof TechResearchMenu techResearchMenu) {
            if (event.getRawSlot() < 18) { // first two rows of the research menu should not be interacted with
                event.setCancelled(true);
            } else if (event.getRawSlot() == 53) {
                event.setCancelled(true);

                Technology technology = techResearchMenu.getTechnology();
                Set<ItemType> requirementsItemTypes = technology.getRemainingRequirementsItemTypes();

                HashMap<ItemType, List<Integer>> slotsOfItemType = new HashMap<>();
                HashMap<ItemType, Integer> amountPerItemType = new HashMap<>();
                for (int i = 19; i < 53; i++) {
                    ItemStack itemStack = inventory.getItem(i);
                    if (itemStack == null) continue;
                    ItemType itemType = itemStack.getType().asItemType();
                    if (!requirementsItemTypes.contains(itemType)) continue;

                    if (!slotsOfItemType.containsKey(itemType)) {
                        slotsOfItemType.put(itemType, new ArrayList<>());
                    }
                    slotsOfItemType.get(itemType).add(i);

                    amountPerItemType.merge(itemType, itemStack.getAmount(), Integer::sum);
                }

                if (slotsOfItemType.isEmpty()) return;

                HashMap<ItemType, Integer> amountOfUsedItemTypes = techProgress.progressTechnology(technology, amountPerItemType);
                // todo update research and gui
            }
        }
//        } else if (inventory instanceof MerchantInventory merchantInventory
//                && playerWithCurrentlyOpenedReserachMenu.containsKey(event.getWhoClicked())
//                && event.getSlotType() == InventoryType.SlotType.RESULT) {
//            event.setCancelled(true);
//            MerchantRecipe selectedRecipe = merchantInventory.getSelectedRecipe();
//            if (selectedRecipe != null) {
//                List<ItemStack> ingredients = selectedRecipe.getIngredients();
//                assert ingredients.size() == 1 : "Technology does not have exactly one ingredient";
//                ItemStack ingredient = ingredients.getFirst();
//                int index = 0;
//                ItemStack relevantSlot = merchantInventory.getItem(index);
//                if (relevantSlot == null) { // first slot is empty -> use second one
//                    index = 1;
//                    relevantSlot = merchantInventory.getItem(index);
//                }
//                assert relevantSlot != null;
//
//                if (ingredient.getType() == relevantSlot.getType() &&
//                        selectedRecipe.getUses() != selectedRecipe.getMaxUses()) {
//
//                    int toSubtract = 1;
//                    if (event.isShiftClick()) {
//                        toSubtract = Math.min(selectedRecipe.getMaxUses() - selectedRecipe.getUses(), relevantSlot.getAmount());
//                    }
//                    relevantSlot.subtract(toSubtract);
//                    merchantInventory.setItem(index, relevantSlot);
//                    selectedRecipe.setUses(selectedRecipe.getUses() + toSubtract);
//
//                    // todo interface does not update properly
//                    Player player = (Player) event.getWhoClicked();
//                    player.updateInventory();
//                    // .get implicitly check through initial if
//
//                    assert inventory.getViewers().size() == 1;
//                    Player viewingPlayer = (Player) inventory.getViewers().getFirst();
//
//                    TechResearchMenu techResearchMenu = playerWithCurrentlyOpenedReserachMenu.get(viewingPlayer);
//                    techTimeout.setActiveResearchOfPlayer(player, techResearchMenu);
//                    techProgress.progressTechnology(techResearchMenu.getTechnology(), ItemStack.of(ingredient.getType(), toSubtract));
//                    //player.sendMessage("Technology progressed");
//                }
//            }
//        }
    }
}
