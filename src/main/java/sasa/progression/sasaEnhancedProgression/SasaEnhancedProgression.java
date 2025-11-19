package sasa.progression.sasaEnhancedProgression;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import sasa.progression.sasaEnhancedProgression.features.RecipeSorter;
import sasa.progression.sasaEnhancedProgression.io.TechnologyConfigReader;
import sasa.progression.sasaEnhancedProgression.techinterface.TechCommand;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;

public final class SasaEnhancedProgression extends JavaPlugin {

    public static JavaPlugin plugin;

    @Override
    public void onEnable() {
        plugin = this;
        TechnologyConfigReader technologyConfigReader = new TechnologyConfigReader();

        init_world_settings();
        this.saveConfig();

        TechProgress techProgress = new TechProgress(technologyConfigReader);
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(TechCommand.createCommand(techProgress), "Opens the tech tree progression interface");
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    private void init_world_settings() {
        for (World world : Bukkit.getWorlds()) {
            world.setGameRule(GameRule.DO_LIMITED_CRAFTING, true);
            world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        }
    }
}