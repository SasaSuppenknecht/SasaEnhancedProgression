package sasa.progression.sasaEnhancedProgression.techinterface;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.events.TechnologyTimeoutEvent;

import java.util.HashMap;
import java.util.UUID;

class TechTimeout {

    private final long TIMEOUT;
    private final HashMap<UUID, TechResearchMenu> activeResearch = new HashMap<>();


    public TechTimeout() {
        long TICKS_PER_SECOND = 20;
        TIMEOUT = TICKS_PER_SECOND * SasaEnhancedProgression.configReader.getTimeout();
    }

    TechResearchMenu getActiveResearchOfPlayer(Player player) {
        return activeResearch.get(player.getUniqueId());
    }

    void setActiveResearchOfPlayer(Player player, TechResearchMenu research) {
        // todo this approach forgets about the timer on server restart
        if (activeResearch.containsKey(player.getUniqueId()))
            return;

        UUID playerUUID = player.getUniqueId();
        activeResearch.put(playerUUID, research);
        Bukkit.getScheduler().runTaskLater(SasaEnhancedProgression.plugin, () -> {
            TechResearchMenu techResearchMenu = activeResearch.remove(playerUUID);
            new TechnologyTimeoutEvent(player, techResearchMenu.getTechnology()).callEvent();
            player.sendMessage("You may contribute to a different research again.");
        }, TIMEOUT);

    }

    boolean playerHasActiveResearch(Player player) {
        return activeResearch.containsKey(player.getUniqueId());
    }
}
