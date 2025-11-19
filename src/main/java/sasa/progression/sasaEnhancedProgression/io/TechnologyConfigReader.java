package sasa.progression.sasaEnhancedProgression.io;

import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.DatapackSetup;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public class TechnologyConfigReader {

    private final String CONFIG_NAME = "techconfig.yml";
    private final CompletableFuture<YamlConfiguration> config;

    public TechnologyConfigReader() {
        config = loadConfigAsync();
    }

    private CompletableFuture<YamlConfiguration> loadConfigAsync() {
        return CompletableFuture.supplyAsync(() -> {
            File file = new File(SasaEnhancedProgression.plugin.getDataFolder(), CONFIG_NAME);
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.load(file);
            } catch (IOException | InvalidConfigurationException e) {
                throw new RuntimeException(e);
            }
            return config;
        });
    }


    public HashMap<String, Integer> getTechnologyRequirements(@NotNull NamespacedKey namespacedKey) {
        assert namespacedKey.getNamespace().equals(DatapackSetup.DATAPACK_NAMESPACE);
        String key = namespacedKey.getKey();

        // todo read in parts
        // todo read in scaling

        String yamlPath = key.replace("/", ".") + ".requirements";
        ConfigurationSection section = config.join().getConfigurationSection(yamlPath);
        if (section == null) {
            return null;
        }
        HashMap<String, Integer> technologyRequirementsMap = new HashMap<>();
        for (String sectionKey : section.getKeys(false)) {
            int value = section.getInt(sectionKey);
            technologyRequirementsMap.put(sectionKey, value);
        }
        return technologyRequirementsMap;
    }

}
