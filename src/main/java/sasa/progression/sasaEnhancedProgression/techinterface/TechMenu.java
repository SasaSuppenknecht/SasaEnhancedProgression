package sasa.progression.sasaEnhancedProgression.techinterface;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.*;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;

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
        if (techTimeout.hasActiveResearch(player)) {
            TechResearchMenu techResearchMenu = techTimeout.getActiveResearch(player);
            player.openInventory(techResearchMenu.getInventory());
            return;
        }

        player.openInventory(new TechSelectionMenu(techProgress).getInventory());
    }


    void openResearchMenuForPlayer(Technology technology, Player player) {
        TechResearchMenu activeResearch;
        if (!techTimeout.hasActiveResearch(player)) {
            activeResearch = new TechResearchMenu(technology, player);
        } else {
            activeResearch = techTimeout.getActiveResearch(player);
        }

        player.openInventory(activeResearch.getInventory());
    }


    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        Inventory inventory = event.getInventory();

        InventoryHolder inventoryHolder = inventory.getHolder(false);
        if (event.getWhoClicked() instanceof Player player) {
            if (inventoryHolder instanceof TechSelectionMenu techSelectionMenu) {
                if (event.isShiftClick()) {
                    event.setCancelled(true);
                } else if (clickedInventory == inventory) {
                    event.setCancelled(true);
                    ItemStack getClickedItem = event.getCurrentItem();
                    Technology selectedTechnology = techSelectionMenu.buttonPress(getClickedItem);
                    if (selectedTechnology == null) return;
                    if (!techTimeout.hasActiveResearch(player)) {
                        openResearchMenuForPlayer(selectedTechnology, player);
                    } else {
                        TechResearchMenu techResearchMenu = techTimeout.getActiveResearch(player);
                        Technology technology = techResearchMenu.getTechnology();
                        if (selectedTechnology == technology) {
                            openResearchMenuForPlayer(selectedTechnology, player);
                        } else {
                            TextComponent title = (TextComponent) technology.getConnectedAdvancement().getDisplay().title();
                            String technologyName = title.content();
                            int remainingTime = techTimeout.getRemainingTimeoutInSeconds(player);
                            player.sendMessage("You are still locked into %s for another %d seconds".formatted(technologyName, remainingTime));
                        }
                    }
                }
            } else if (inventoryHolder instanceof TechResearchMenu techResearchMenu) {
                if (event.getRawSlot() < 18) { // first two rows of the research menu should not be interacted with
                    event.setCancelled(true);
                } else if (event.getRawSlot() == 45) {
                    event.setCancelled(true);

                    player.openInventory(new TechSelectionMenu(techProgress).getInventory());

                } else if (event.getRawSlot() == 53) {
                    event.setCancelled(true);

                    Technology technology = techResearchMenu.getTechnology();
                    Set<ItemType> requirementsItemTypes = technology.getRemainingRequirementsItemTypes();

                    HashMap<ItemType, Integer> amountPerItemType = new HashMap<>();
                    for (int i = 18; i < 53; i++) {
                        if (i == 45) continue;
                        ItemStack itemStack = inventory.getItem(i);
                        if (itemStack == null) continue;
                        ItemType itemType = itemStack.getType().asItemType();
                        if (!requirementsItemTypes.contains(itemType)) continue;

                        amountPerItemType.merge(itemType, itemStack.getAmount(), Integer::sum);
                    }

                    if (amountPerItemType.isEmpty()) return;

                    boolean progressed = techProgress.progressTechnology(player, technology, amountPerItemType);
                    if (progressed) {
                        techTimeout.setActiveResearch(player, techResearchMenu);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof TechResearchMenu techResearchMenu) {
            Player player = (Player) event.getPlayer();

            ArrayList<ItemStack> itemList = new ArrayList<>();
            for (int i = 18; i < 53; i++) {
                if (i == 45) continue;
                ItemStack itemStack = inventory.getItem(i);
                if (itemStack != null) {
                    itemList.add(itemStack);
                }
            }
            inventory.removeItem(itemList.toArray(ItemStack[]::new));

            player.give(itemList, true);
            Bukkit.getScheduler().runTaskLater(SasaEnhancedProgression.plugin, player::updateInventory, 1);

            HandlerList.unregisterAll(techResearchMenu);
            techResearchMenu.stopAnimation();
        }
    }

    @EventHandler
    public void onInventoryOpenEvent(InventoryOpenEvent event) {
        if (event.getInventory().getHolder() instanceof TechResearchMenu techResearchMenu) {
            Bukkit.getPluginManager().registerEvents(techResearchMenu, SasaEnhancedProgression.plugin);
            techResearchMenu.startAnimation();
        }
    }
}
