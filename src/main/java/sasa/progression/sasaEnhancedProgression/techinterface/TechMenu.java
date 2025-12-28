package sasa.progression.sasaEnhancedProgression.techinterface;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class TechMenu implements Listener {


    private final TechProgress techProgress;
    private final TechTimeout techTimeout;

    private final HashMap<Player, TechResearchMenu> playerWithCurrentlyOpenedReserachMenu = new HashMap<>();

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
            techResearchMenu.getView().open();
            playerWithCurrentlyOpenedReserachMenu.put(player, techResearchMenu);
            return;
        }

        player.openInventory(new TechSelectionMenu(techProgress.getOpenTech()).getInventory());
    }


    void openResearchMenuForPlayer(Technology technology, Player player) {
        TechResearchMenu activeResearch = techTimeout.getActiveResearchOfPlayer(player);
        if (activeResearch == null) {
            activeResearch = new TechResearchMenu(technology, player);
        }

        activeResearch.getView().open();
        playerWithCurrentlyOpenedReserachMenu.put(player, activeResearch);
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

        } else if (inventory instanceof MerchantInventory merchantInventory
                && playerWithCurrentlyOpenedReserachMenu.containsKey(event.getWhoClicked())
                && event.getSlotType() == InventoryType.SlotType.RESULT) {
            event.setCancelled(true);
            MerchantRecipe selectedRecipe = merchantInventory.getSelectedRecipe();
            if (selectedRecipe != null) {
                List<ItemStack> ingredients = selectedRecipe.getIngredients();
                assert ingredients.size() == 1 : "Technology does not have exactly one ingredient";
                ItemStack ingredient = ingredients.getFirst();
                int index = 0;
                ItemStack relevantSlot = merchantInventory.getItem(index);
                if (relevantSlot == null) { // first slot is empty -> use second one
                    index = 1;
                    relevantSlot = merchantInventory.getItem(index);
                }
                assert relevantSlot != null;

                if (ingredient.getType() == relevantSlot.getType() &&
                        selectedRecipe.getUses() != selectedRecipe.getMaxUses()) {

                    int toSubtract = 1;
                    if (event.isShiftClick()) {
                        toSubtract = Math.min(selectedRecipe.getMaxUses() - selectedRecipe.getUses(), relevantSlot.getAmount());
                    }
                    relevantSlot.subtract(toSubtract);
                    merchantInventory.setItem(index, relevantSlot);
                    selectedRecipe.setUses(selectedRecipe.getUses() + toSubtract);

                    // todo interface does not update properly
                    Player player = (Player) event.getWhoClicked();
                    player.updateInventory();
                    // .get implicitly check through initial if

                    assert inventory.getViewers().size() == 1;
                    Player viewingPlayer = (Player) inventory.getViewers().getFirst();

                    TechResearchMenu techResearchMenu = playerWithCurrentlyOpenedReserachMenu.get(viewingPlayer);
                    techTimeout.setActiveResearchOfPlayer(player, techResearchMenu);
                    techProgress.progressTechnology(techResearchMenu.getTechnology(), ItemStack.of(ingredient.getType(), toSubtract));
                    //player.sendMessage("Technology progressed");
                }
            }
        }
        //else if (clickedInventory instanceof MerchantInventory merchantInventory &&
        //        activeResearchWindows.stream().anyMatch(techInventory -> techInventory.getView() == event.getView()) &&
        //        event.getSlotType() == InventoryType.SlotType.RESULT) {
    }

        @EventHandler
        public void onInventoryCloseEvent(InventoryCloseEvent event) {
            Inventory inventory = event.getInventory();
            if (inventory instanceof MerchantInventory merchantInventory
                    && playerWithCurrentlyOpenedReserachMenu.containsKey(event.getPlayer())) {
                assert inventory.getViewers().size() == 1;
                Player viewingPlayer = (Player) inventory.getViewers().getFirst();

                playerWithCurrentlyOpenedReserachMenu.remove(viewingPlayer);
            }
        }
}
