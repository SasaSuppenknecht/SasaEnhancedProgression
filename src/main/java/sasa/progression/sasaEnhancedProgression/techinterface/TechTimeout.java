package sasa.progression.sasaEnhancedProgression.techinterface;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.events.TechnologyTimeoutEvent;

import java.util.HashMap;
import java.util.UUID;

class TechTimeout {

    private final int TIMEOUT;
    private final HashMap<UUID, PlayerTimeoutInformation> activeResearch = new HashMap<>();


    public TechTimeout() {
        int TICKS_PER_SECOND = 20;
        TIMEOUT = TICKS_PER_SECOND * SasaEnhancedProgression.configReader.getTimeout();
        System.out.printf("Timeout: %d%n", TIMEOUT);
    }

    TechResearchMenu getActiveResearch(Player player) {
        return activeResearch.get(player.getUniqueId()).techResearchMenu;
    }

    void setActiveResearch(Player player, TechResearchMenu research) {
        // note: this approach forgets about the timer on server restart
        if (activeResearch.containsKey(player.getUniqueId()))
            return;

        // by using the unique ID the player can re-log into the game and the timer will still track that player
        UUID playerUUID = player.getUniqueId();
        int tick = Bukkit.getCurrentTick();

        activeResearch.put(playerUUID, new PlayerTimeoutInformation(research, tick + TIMEOUT));
        Bukkit.getScheduler().runTaskLater(SasaEnhancedProgression.plugin, () -> {
            PlayerTimeoutInformation techResearchMenu = activeResearch.remove(playerUUID);
            new TechnologyTimeoutEvent(player, techResearchMenu.techResearchMenu.getTechnology()).callEvent();
            player.sendMessage("You may contribute to a different research again.");
        }, TIMEOUT);

    }

    boolean hasActiveResearch(Player player) {
        return activeResearch.containsKey(player.getUniqueId());
    }

    int getRemainingTimeoutInSeconds(Player player) {
        if (!activeResearch.containsKey(player.getUniqueId())) {
            return 0;
        } else {
            PlayerTimeoutInformation playerTimeoutInformation = activeResearch.get(player.getUniqueId());
            return (playerTimeoutInformation.timeoutEndTick - Bukkit.getCurrentTick()) / 20;
        }
    }

    private record PlayerTimeoutInformation(TechResearchMenu techResearchMenu, int timeoutEndTick) {}
}
