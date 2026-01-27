package sasa.progression.sasaEnhancedProgression.io;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;

import java.io.File;
import java.io.IOException;

public class ConfigReader {

    private final String CONFIG = "config.yml";

    private final int difficulty;
    private final int playerCount;
    private final int timeout;

    public ConfigReader() {
        SasaEnhancedProgression.plugin.saveDefaultConfig();
        YamlConfiguration config = new YamlConfiguration();
        File configFile = new File(SasaEnhancedProgression.plugin.getDataFolder(), CONFIG);
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        // note: for more config variables a less redundant approached should be used
        int tmpDifficulty = config.getInt("difficulty", 2) - 1;
        if (tmpDifficulty < 1 || tmpDifficulty > 3) {
            SasaEnhancedProgression.plugin.getLogger().config("Difficulty outside of bounds. Setting difficulty to 2.");
            tmpDifficulty = 1;
        }
        difficulty = tmpDifficulty;

        int tmpPlayerCount = config.getInt("playercount", 2);
        if (tmpPlayerCount < 0) {
            SasaEnhancedProgression.plugin.getLogger().config("Playercount outside of bounds. Setting playercount to 2.");
            tmpPlayerCount = 2;
        } else if (tmpPlayerCount == 0) {
            tmpPlayerCount = Bukkit.getServer().getMaxPlayers();
        }
        playerCount = tmpPlayerCount;

        int tmpTimeout = config.getInt("timeout", 180);
        if (tmpTimeout < 0 || tmpTimeout > 3600) {
            SasaEnhancedProgression.plugin.getLogger().config("Timeout outside of bounds. Setting timeout to 180.");
            tmpTimeout = 180;
        }
        timeout = tmpTimeout;
    }


    public int getDifficulty() {
        return difficulty;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public int getTimeout() {
        return timeout;
    }
}
