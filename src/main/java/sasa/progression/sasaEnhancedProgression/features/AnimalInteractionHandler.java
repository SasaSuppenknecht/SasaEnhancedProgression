package sasa.progression.sasaEnhancedProgression.features;

import org.bukkit.Material;
import org.bukkit.entity.Animals;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;

public class AnimalInteractionHandler extends AbstractFeature {

    private boolean tamingEnabled = false;
    private boolean breedingEnabled = false;

    @EventHandler
    public void onAnimalInteraction(PlayerInteractEntityEvent event) {
        if (event.getRightClicked() instanceof Animals) {
            ItemStack itemStack = event.getPlayer().getInventory().getItemInMainHand();
            if (itemStack.getType() == Material.AIR) {
                event.setCancelled(!tamingEnabled);
            } else {
                event.setCancelled(!breedingEnabled);
            }
        }
    }

    @Override
    @EventHandler
    public void onTechnologyUnlock(TechnologyUnlockEvent event) {
        String key = event.getAdvancementKey().getKey();
        if (key.endsWith("taming")) {
            tamingEnabled = true;
        } else if (key.endsWith("breeding")) {
            breedingEnabled = true;
        }
    }
}
