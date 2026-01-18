package sasa.progression.sasaEnhancedProgression.features;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.world.PortalCreateEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;

public class PortalCreationHandler extends AbstractFeature {

    private boolean isNetherEnabled = false;

    @EventHandler
    public void onPortalCreateEvent(PortalCreateEvent event) {
        if (!isNetherEnabled && event.getReason() == PortalCreateEvent.CreateReason.FIRE) {
            event.setCancelled(true);
            if (event.getEntity() instanceof Player player) {
                player.sendMessage("You don't know how this works yet.");
            }
        }
    }

    @Override
    @EventHandler
    public void onTechnologyUnlock(TechnologyUnlockEvent event) {
        if (event.getAdvancementKey().getKey().endsWith("nether")) {
            isNetherEnabled = true;
        }
    }
}
