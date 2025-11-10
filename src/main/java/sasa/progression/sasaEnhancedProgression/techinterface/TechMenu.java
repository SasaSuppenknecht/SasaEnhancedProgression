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
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;

import java.util.*;

public class TechMenu implements Listener {


    private final TechProgress techProgress;
    private final List<Inventory> activeSelectionInventories = Collections.synchronizedList(new ArrayList<>());

    TechMenu(TechProgress techProgress) {
        this.techProgress = techProgress;
        SasaEnhancedProgression.plugin.getServer().getPluginManager().registerEvents(this, SasaEnhancedProgression.plugin);
    }


    void openSelectionMenuForPlayer(Player player) {
        Inventory inventory = Bukkit.createInventory(player, 9 * 3, Component.text("Technology Selection"));
        for (Technology technology : techProgress.getOpenTech()) {
            Advancement advancement = technology.getConnectedAdvancement();
            ItemStack icon = Objects.requireNonNull(advancement.getDisplay()).icon();

            ItemMeta itemMeta = icon.getItemMeta();
            String title = PlainTextComponentSerializer.plainText().serialize(advancement.getDisplay().title());
            itemMeta.displayName(Component.text(title));
            ArrayList<Component> requirementsText = new ArrayList<>();
            for (Technology.MaterialRequirement requirement : technology.getRequirements()) {
                Component text = Component.translatable(requirement.getMaterial().translationKey())
                        .append(Component.text(":"))
                        .appendSpace()
                        .append(Component.text("%d / %d".formatted(requirement.getGiven(), requirement.getNeeded())));
                requirementsText.add(text);
            }
            itemMeta.lore(requirementsText);
            icon.setItemMeta(itemMeta);

            inventory.addItem(icon);
        }
        activeSelectionInventories.add(inventory);
        player.openInventory(inventory);
    }

    void openProgressMenuForPlayer(Technology technology, Player player) {

    }

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (activeSelectionInventories.contains(event.getClickedInventory()) || event.isShiftClick()) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onInventoryCloseEvent(InventoryCloseEvent event) {
        activeSelectionInventories.remove(event.getInventory());
    }

}
