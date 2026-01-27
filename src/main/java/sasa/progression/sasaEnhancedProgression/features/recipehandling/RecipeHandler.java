package sasa.progression.sasaEnhancedProgression.features.recipehandling;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.Recipe;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;
import sasa.progression.sasaEnhancedProgression.features.AbstractFeature;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RecipeHandler extends AbstractFeature {

    private final RecipeSorter recipeSorter;
    private final Set<NamespacedKey> completedTechnologies = new HashSet<>();

    public RecipeHandler() {
        this.recipeSorter = new RecipeSorter();
    }


    @EventHandler
    public void onTechnologyUnlock(TechnologyUnlockEvent event) {
        NamespacedKey advancementKey = event.getAdvancementKey();
        List<Recipe> recipes = recipeSorter.getRecipesForAdvancement(advancementKey);

        for (Recipe recipe : recipes) {
            Bukkit.addRecipe(recipe, true); // todo this can crash when a recipe is added more than once
        }
        for (Player player : Bukkit.getOnlinePlayers()) {
            discoverRecipes(player, advancementKey);
        }
        completedTechnologies.add(advancementKey);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        for (NamespacedKey advancementKey : completedTechnologies) {
            discoverRecipes(event.getPlayer(), advancementKey);
        }
    }

    private void discoverRecipes(Player player, NamespacedKey advancementKey) {
        List<Recipe> recipes = recipeSorter.getRecipesForAdvancement(advancementKey);
        List<NamespacedKey> recipeKeys = recipes.stream()
                .map(recipe -> ((Keyed) recipe).getKey())
                .toList();
        player.discoverRecipes(recipeKeys);
    }
}
