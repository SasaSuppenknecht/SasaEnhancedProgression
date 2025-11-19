package sasa.progression.sasaEnhancedProgression.techinterface;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.MerchantView;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.techtree.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;

import java.util.*;

@SuppressWarnings("UnstableApiUsage")
public class TechMenu implements Listener {


    private final TechProgress techProgress;
    private final TechTimeout techTimeout;

    private final HashMap<Inventory, HashMap<ItemStack, Technology>> activeSelectionInventories = new HashMap<>();
    private final ArrayList<TechInventory> activeResearchWindows = new ArrayList<>();

    TechMenu(TechProgress techProgress) {
        this.techProgress = techProgress;
        this.techTimeout = new TechTimeout();
        SasaEnhancedProgression.plugin.getServer().getPluginManager().registerEvents(this, SasaEnhancedProgression.plugin);
    }


    void openSelectionMenuForPlayer(Player player) {
        // If player has already invested in research before, the player may not choose a different one until some
        // time later
        if (techTimeout.playerHasActiveResearch(player)) {
            TechInventory techInventory = techTimeout.getActiveResearchOfPlayer(player);
            activeResearchWindows.add(techInventory);
            player.openInventory(techInventory.getView());
            return;
        }
        // create new inventory as menu, where each item represents a technology
        Inventory inventory = Bukkit.createInventory(player, 9 * 3, Component.text("Technology Selection"));
        HashMap<ItemStack, Technology> buttonMap = new HashMap<>();
        for (Technology technology : techProgress.getOpenTech()) {
            Advancement advancement = technology.getConnectedAdvancement();
            ItemStack icon = Objects.requireNonNull(advancement.getDisplay()).icon();

            ItemMeta itemMeta = icon.getItemMeta();
            String title = PlainTextComponentSerializer.plainText().serialize(advancement.getDisplay().title());
            itemMeta.displayName(Component.text(title));
            ArrayList<Component> requirementsText = new ArrayList<>();
            // Each technology also lists total requirements and the already provided requirements in its lore
            for (MaterialRequirement requirement : technology.getRequirements()) {
                Component text = Component.translatable(requirement.getMaterial().translationKey())
                        .append(Component.text(":"))
                        .appendSpace()
                        .append(Component.text("%d / %d".formatted(requirement.getGiven(), requirement.getNeeded())));
                requirementsText.add(text);
            }
            itemMeta.lore(requirementsText);
            icon.setItemMeta(itemMeta);

            inventory.addItem(icon);
            buttonMap.put(icon, technology);
        }
        activeSelectionInventories.put(inventory, buttonMap);
        player.openInventory(inventory);
    }


    void openProgressMenuForPlayer(Technology technology, Player player) {
        TechInventory activeResearch = techTimeout.getActiveResearchOfPlayer(player);
        if (activeResearch == null) {
            activeResearch = new TechInventory(technology, player);
        }

        activeResearchWindows.add(activeResearch);
        player.openInventory(activeResearch.getView());
    }


    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory topInventory = event.getView().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();
        if (activeSelectionInventories.containsKey(topInventory) && event.isShiftClick()) {
            event.setCancelled(true);
        } else if (activeSelectionInventories.containsKey(clickedInventory)) {
            event.setCancelled(true);
            ItemStack getClickedItem = event.getCurrentItem();
            Technology selectedTechnology = activeSelectionInventories.get(clickedInventory).get(getClickedItem);
            if (selectedTechnology != null) {
                openProgressMenuForPlayer(selectedTechnology, (Player) event.getWhoClicked());
            }
        } else if (clickedInventory instanceof MerchantInventory merchantInventory &&
                activeResearchWindows.stream().anyMatch(techInventory -> techInventory.getView() == event.getView()) &&
                event.getSlotType() == InventoryType.SlotType.RESULT) {
            event.setCancelled(true);
            MerchantRecipe selectedRecipe = merchantInventory.getSelectedRecipe();
            if (selectedRecipe != null) {
                List<ItemStack> ingredients = selectedRecipe.getIngredients();
                assert ingredients.size() == 1 : "Technology has not exactly one ingredient";
                ItemStack ingredient = ingredients.getFirst();
                int index = 0;
                ItemStack relevantSlot = merchantInventory.getItem(index);
                if (relevantSlot == null) { // first slot is empty -> use second one
                    index = 1;
                    relevantSlot = merchantInventory.getItem(index);
                }
                assert relevantSlot != null;
                System.out.println(relevantSlot.getType().name());

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
                    TechInventory techInventory = activeResearchWindows
                            .stream()
                            .filter(inv -> inv.getView() == event.getView())
                            .findFirst()
                            .get();
                    techTimeout.setActiveResearchOfPlayer(player, techInventory);
                    techProgress.progressTechnology(techInventory.getTechnology(), ItemStack.of(ingredient.getType(), toSubtract));
                    //player.sendMessage("Technology progressed");
                }
            }
        }
    }


    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        activeSelectionInventories.remove(event.getInventory());
        if (event.getView() instanceof MerchantView merchantView) {
            activeResearchWindows.removeIf(techInventory -> techInventory.getView() == merchantView);
        }
    }
}
