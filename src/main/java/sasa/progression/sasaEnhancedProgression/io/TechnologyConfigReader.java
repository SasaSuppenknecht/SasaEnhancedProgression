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
import java.util.List;

public class TechnologyConfigReader {

    private final String CONFIG_NAME = "techconfig.yml";
    private final YamlConfiguration config;

    public TechnologyConfigReader() {
        config = loadConfig();
    }

    // todo place techconfig in folder if not already present
    private YamlConfiguration loadConfig() {
        File file = new File(SasaEnhancedProgression.plugin.getDataFolder(), CONFIG_NAME);
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(file);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }
        return config;
    }


    public HashMap<String, Integer> getTechnologyRequirements(@NotNull NamespacedKey namespacedKey) {
        assert namespacedKey.getNamespace().equals(DatapackSetup.DATAPACK_NAMESPACE);
        String key = namespacedKey.getKey();

        // todo read in parts
        // todo read in scaling
        // todo process difficulty
        int difficulty = 1;

        String yamlPath = key.replace("/", ".") + ".requirements";
        ConfigurationSection section = config.getConfigurationSection(yamlPath);
        if (section == null) {
            return null;
        }
        HashMap<String, Integer> technologyRequirementsMap = new HashMap<>();
        for (String sectionKey : section.getKeys(false)) {
            List<Integer> values = (List<Integer>) section.getConfigurationSection(sectionKey).getList("amount");
            assert values != null;
            int value = values.get(difficulty);
            if (value == 0) continue;
            technologyRequirementsMap.put(sectionKey, value);
        }
        return technologyRequirementsMap;
    }

}
