package sasa.progression.sasaEnhancedProgression.techinterface;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.view.MerchantView;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;

import java.util.HashMap;
import java.util.UUID;

class TechTimeout {

    private final long TIMEOUT = 20 * 60 * 1; // 5 minutes = 300 seconds = 60 * 20 ticks
    private final HashMap<UUID, TechInventory> activeResearch = new HashMap<>();

    TechInventory getActiveResearchOfPlayer(Player player) {
        return activeResearch.get(player.getUniqueId());
    }

    void setActiveResearchOfPlayer(Player player, TechInventory research) {
        // todo this approach forgets about the timer on server restart
        if (activeResearch.containsKey(player.getUniqueId()))
            return;

        activeResearch.put(player.getUniqueId(), research);
        Bukkit.getScheduler().runTaskLater(SasaEnhancedProgression.plugin, () -> {
            activeResearch.remove(player.getUniqueId());
            player.sendMessage("You may contribute to a different research again.");
        }, TIMEOUT);
    }

    boolean playerHasActiveResearch(Player player) {
        return activeResearch.containsKey(player.getUniqueId());
    }

}
