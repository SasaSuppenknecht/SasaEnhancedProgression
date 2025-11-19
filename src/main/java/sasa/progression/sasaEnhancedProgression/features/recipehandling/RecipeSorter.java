package sasa.progression.sasaEnhancedProgression.features.recipehandling;

import org.bukkit.Bukkit;
import org.bukkit.Keyed;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.*;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.io.DatapackRewardExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RecipeSorter {

    private final DatapackRewardExtractor datapackRewardExtractor = new DatapackRewardExtractor();

    private final HashMap<NamespacedKey, Recipe> nonCraftingRecipes = new HashMap<>();


    public RecipeSorter() {
        for (@NotNull Iterator<Recipe> it = Bukkit.recipeIterator(); it.hasNext(); ) {
            Recipe recipe = it.next();

            if (recipe instanceof CookingRecipe<?> || recipe instanceof StonecuttingRecipe || recipe instanceof SmithingRecipe) {
                NamespacedKey recipeKey = ((Keyed) recipe).getKey();
                nonCraftingRecipes.put(recipeKey, recipe);
                Bukkit.removeRecipe(recipeKey);
            }
        }
    }


    public List<Recipe> getRecipesForAdvancement(NamespacedKey advancement) {
        ArrayList<Recipe> recipes = new ArrayList<>();
        if (!datapackRewardExtractor.advancementHasRecipeKeys(advancement)) {
            return recipes;
        }
        for (NamespacedKey recipeKey : datapackRewardExtractor.getRecipeKeys(advancement)) {
            Recipe recipe = nonCraftingRecipes.get(recipeKey);
            if (recipe != null) {
                recipes.add(recipe);
            }
        }
        return recipes;
    }
}
