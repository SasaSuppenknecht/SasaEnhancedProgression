package sasa.progression.sasaEnhancedProgression.features;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;

public abstract class AbstractFeature implements Listener {

    @EventHandler
    public abstract void onTechnologyUnlock(TechnologyUnlockEvent event);

}
