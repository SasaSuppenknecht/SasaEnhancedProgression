package sasa.progression.sasaEnhancedProgression.features;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerFishEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;

public class FishingInteractionHandler extends AbstractFeature {

    private boolean isFishingEnabled = false;

    @EventHandler
    public void onFishingEvent(PlayerFishEvent event) {
        PlayerFishEvent.State state = event.getState();
        if (!isFishingEnabled && state == PlayerFishEvent.State.CAUGHT_FISH) {
            event.setCancelled(true);
            event.getPlayer().sendMessage("You don't know how this works yet.");
        }
    }

    @Override
    @EventHandler
    public void onTechnologyUnlock(TechnologyUnlockEvent event) {
        if (event.getAdvancementKey().getKey().endsWith("fishing")) {
            isFishingEnabled = true;
        }
    }
}
