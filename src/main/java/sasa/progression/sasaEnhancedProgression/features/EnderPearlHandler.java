package sasa.progression.sasaEnhancedProgression.features;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerTeleportEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;

public class EnderPearlHandler extends AbstractFeature {

    private boolean isTeleportationEnabled = false;

    @EventHandler
    public void onPlayerTeleportation(PlayerTeleportEvent event) {
        if (!isTeleportationEnabled && event.getCause() == PlayerTeleportEvent.TeleportCause.ENDER_PEARL) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You don't know how this works yet.");
        }
    }

    @Override
    @EventHandler
    public void onTechnologyUnlock(TechnologyUnlockEvent event) {
        if (event.getAdvancementKey().getKey().endsWith("teleportation")) {
            isTeleportationEnabled = true;
        }
    }
}
