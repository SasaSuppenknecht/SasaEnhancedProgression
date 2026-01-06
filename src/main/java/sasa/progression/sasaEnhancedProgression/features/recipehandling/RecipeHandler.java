package sasa.progression.sasaEnhancedProgression.features.recipehandling;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Recipe;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;

import java.util.List;

public class RecipeHandler implements Listener {

    private final RecipeSorter recipeSorter;


    public RecipeHandler() {
        this.recipeSorter = new RecipeSorter();
    }


    @EventHandler
    public void onAdvancementUnlock(TechnologyUnlockEvent event) {
        NamespacedKey advancementKey = event.getAdvancementKey();
        List<Recipe> recipes = recipeSorter.getRecipesForAdvancement(advancementKey);
        if (recipes.isEmpty())
            return;

        for (Recipe recipe : recipes) {
            Bukkit.addRecipe(recipe, true);
            // todo new players joining will not get this update
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.discoverRecipe(((Keyed) recipe).getKey());
            }
        }
    }
}
