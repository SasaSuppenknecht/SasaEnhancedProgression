package sasa.progression.sasaEnhancedProgression.techinterface;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.view.MerchantView;
import sasa.progression.sasaEnhancedProgression.techtree.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
class TechInventory {

    private final Technology technology;
    private final MerchantView research;

    TechInventory(Technology technology, Player player) {
        this.technology = technology;

        Merchant merchant = Bukkit.createMerchant();
        int parts = technology.getParts();
        List<MaterialRequirement> requirements = technology.getRequirements();
        List<MerchantRecipe> merchantRecipeList = new ArrayList<>();
        for (MaterialRequirement requirement : requirements) {
            Material material = requirement.getMaterial();
            int partSize = Math.ceilDiv(requirement.getNeeded(), parts);
            MerchantRecipe merchantRecipe = new MerchantRecipe(ItemStack.of(material, requirement.getNeeded()), partSize);
            merchantRecipe.addIngredient(ItemStack.of(material));
            merchantRecipeList.add(merchantRecipe);
        }
        merchant.setRecipes(merchantRecipeList);

        String technologyName = PlainTextComponentSerializer.plainText().serialize(technology.getConnectedAdvancement().displayName());
        technologyName = technologyName.substring(1, technologyName.length() - 1); // remove [ and ]
        research = MenuType.MERCHANT.builder()
                .title(Component.text(technologyName))
                .merchant(merchant)
                .build(player);
    }

    MerchantView getView() {
        return research;
    }

    Technology getTechnology() {
        return technology;
    }
}
