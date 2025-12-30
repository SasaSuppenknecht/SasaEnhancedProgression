package sasa.progression.sasaEnhancedProgression.io;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class ProgressFileManager {

    private static final String PROGRESS = "progress.yaml";
    private static final String COMPLETED = "completed";
    private static final String OPEN = "open";

    public static void saveProgress(TechProgress techProgress) {
        if (techProgress == null) {
            Bukkit.getLogger().severe("TechProgress is null, aborting saveProgress");
            return;
        }

        YamlConfiguration config = new YamlConfiguration();

        List<String> completedList = techProgress.getCompletedTech()
                .stream()
                .map(Technology::getAdvancementKey)
                .filter(key -> !Bukkit.getAdvancement(key).getCriteria().contains("join"))
                .map(NamespacedKey::asString)
                .toList();
        config.set(COMPLETED, completedList);

        ConfigurationSection section = config.createSection(OPEN);
        for (Technology technology : techProgress.getOpenTech()) {
            List<AbstractMaterialRequirement> startedRequirements = technology.getRequirements()
                    .stream()
                    .filter(requirement -> requirement.getGiven() > 0)
                    .toList();

            if (!startedRequirements.isEmpty()) {
                ConfigurationSection techSection = section.createSection(technology.getAdvancementKey().asString());
                for (AbstractMaterialRequirement requirement : startedRequirements) {
                    Key key = switch (requirement) {
                        case MaterialRequirement mr -> mr.getItemType().getKey();
                        case MaterialTagRequirement tr -> tr.getTag().tagKey().key();
                        default -> throw new IllegalStateException("Unexpected value: " + requirement);
                    };

                    techSection.set(key.asString(), requirement.getGiven());
                }
            }
        }

        try {
            config.save(new File(SasaEnhancedProgression.plugin.getDataFolder(), PROGRESS));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not save to " + PROGRESS);
            Bukkit.getPluginManager().disablePlugin(SasaEnhancedProgression.plugin);
        }
    }

    public static TechProgress loadProgress() {
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.load(new File(SasaEnhancedProgression.plugin.getDataFolder(), PROGRESS));
        } catch (IOException e) {
            Bukkit.getLogger().severe("Could not open " + PROGRESS);
            Bukkit.getPluginManager().disablePlugin(SasaEnhancedProgression.plugin);
        } catch (InvalidConfigurationException e) {
            Bukkit.getLogger().severe("Invalid %s save file".formatted(PROGRESS));
            Bukkit.getPluginManager().disablePlugin(SasaEnhancedProgression.plugin);
        }

        TechProgress techProgress = new TechProgress();
        List<String> completedList = (List<String>) config.getList(COMPLETED);
        assert completedList != null;
        for (String key : completedList) {
            Technology technology = techProgress.getTechnologyFromKey(NamespacedKey.fromString(key));
            techProgress.unlockTechnology(technology);
        }

        ConfigurationSection open = config.getConfigurationSection(OPEN);
        assert open != null;
        for (String openResearchKey : open.getKeys(false)) {
            ConfigurationSection research = open.getConfigurationSection(openResearchKey);
            Technology technology = techProgress.getTechnologyFromKey(NamespacedKey.fromString(openResearchKey));
            assert research != null;
            for (String requirementKey : research.getKeys(false)) {
                int value = research.getInt(requirementKey);
                ItemType itemType;
                if (!requirementKey.startsWith("#")) {
                    itemType = Registry.ITEM.get(Key.key(requirementKey));
                    assert itemType != null : "Key " + requirementKey + " does not correspond to any itemtype";
                } else {
                    requirementKey = requirementKey.substring(1);
                    Tag<ItemType> tag = Registry.ITEM.getTag(TagKey.create(RegistryKey.ITEM, requirementKey));
                    itemType = Registry.ITEM.get(tag.values().stream().findFirst().orElseThrow().key());
                    assert itemType != null;
                }
                ItemStack itemStack = itemType.createItemStack(value);
                techProgress.progressTechnology(technology, itemStack);
            }
        }

        return techProgress;
    }

    public static boolean hasSavedProgress() {
        System.out.println(new File(PROGRESS).getPath());

        return new File(SasaEnhancedProgression.plugin.getDataFolder(), PROGRESS).exists();
    }
}
