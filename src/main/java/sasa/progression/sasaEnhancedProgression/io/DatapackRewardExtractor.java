package sasa.progression.sasaEnhancedProgression.io;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import sasa.progression.sasaEnhancedProgression.DatapackSetup;
import sasa.progression.sasaEnhancedProgression.SasaEnhancedProgression;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class DatapackRewardExtractor {

    private final String INDEX_PATH = "advancement_index.txt";
    private final String ADVANCEMENT_ROOT = "techtreedatapack/data/techtree/advancement";
    private final ConcurrentHashMap<NamespacedKey, List<NamespacedKey>> advancementToRecipe = new ConcurrentHashMap<>();

    public DatapackRewardExtractor() {
        Plugin plugin = SasaEnhancedProgression.plugin;

        InputStream inputStream = plugin.getResource(INDEX_PATH);
        if (inputStream == null) {
            plugin.getLogger().severe("Advancement index could not be found.");
            Bukkit.getPluginManager().disablePlugin(plugin);
            return;
        }
        Path advancement_root = Path.of(ADVANCEMENT_ROOT).toAbsolutePath().normalize();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        bufferedReader.lines().parallel().forEach(s -> {
            if (!s.endsWith(".json")) return;
            InputStream in = plugin.getResource(s);
            assert in != null;

            JsonObject object = JsonParser.parseReader(new InputStreamReader(in)).getAsJsonObject();
            ArrayList<NamespacedKey> recipes = new ArrayList<>();
            if (object.has("rewards")) {
                JsonObject rewardsObject = object.getAsJsonObject("rewards");
                JsonArray recipeList = rewardsObject.getAsJsonArray("recipes");

                for (JsonElement element : recipeList) {
                    String entry = element.getAsString();
                    String[] parts = entry.split(":");
                    assert parts.length == 2;
                    recipes.add(new NamespacedKey(parts[0], parts[1]));
                }

                String key = advancement_root.relativize(Path.of(s).toAbsolutePath().normalize()).toString()
                        .replace(".json", "")
                        .replace("\\", "/");
                NamespacedKey advancement = new NamespacedKey(DatapackSetup.DATAPACK_NAMESPACE, key);

                advancementToRecipe.put(advancement, recipes);
            }
            try {
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            inputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean advancementHasRecipeKeys(NamespacedKey advancement) {
        return advancementToRecipe.containsKey(advancement);
    }

    public List<NamespacedKey> getRecipeKeys(NamespacedKey advancement) {
        return List.copyOf(advancementToRecipe.get(advancement));
    }


}
