package sasa.progression.sasaEnhancedProgression.io;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.misc.DatapackSetup;
import sasa.progression.sasaEnhancedProgression.techtree.TechnologyRequirementBundle;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TechnologyConfigReader {

    private final String TECH_CONFIG = "techconfig.yml";

    private final YamlConfiguration techconfig;

    private final int difficulty;
    private final int playerCount;

    public TechnologyConfigReader() {
        SasaEnhancedProgression.plugin.saveResource(TECH_CONFIG, false);
        File techConfigFile = new File(SasaEnhancedProgression.plugin.getDataFolder(), TECH_CONFIG);
        techconfig = new YamlConfiguration();
        try {
            techconfig.load(techConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            throw new RuntimeException(e);
        }

        difficulty = SasaEnhancedProgression.configReader.getDifficulty();
        playerCount = SasaEnhancedProgression.configReader.getPlayerCount();
    }


    public TechnologyRequirementBundle getTechnologyRequirements(@NotNull NamespacedKey namespacedKey) {
        assert namespacedKey.getNamespace().equals(DatapackSetup.DATAPACK_NAMESPACE);
        String key = namespacedKey.getKey().replace("/", ".");

        String yamlPath = key + ".requirements";
        ConfigurationSection section = techconfig.getConfigurationSection(yamlPath);
        if (section == null) {
            return null;
        }

        int parts = techconfig.getInt(key + ".parts");
        TechnologyRequirementBundle technologyRequirement = new TechnologyRequirementBundle(parts);
        for (String sectionKey : section.getKeys(false)) {
            List<Integer> values = (List<Integer>) section.getConfigurationSection(sectionKey).getList("amount");
            assert values != null;
            int value = values.get(difficulty);
            boolean scaling = section.getBoolean("scaling", true);
            if (!scaling) {
                value *= playerCount / 2;
            }
            if (value == 0) continue;

            technologyRequirement.requirements.put(sectionKey, value);
        }
        return technologyRequirement;
    }



}
