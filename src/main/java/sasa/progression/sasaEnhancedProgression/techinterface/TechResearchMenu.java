package sasa.progression.sasaEnhancedProgression.techinterface;

import io.papermc.paper.registry.tag.Tag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.view.MerchantView;
import org.jetbrains.annotations.NotNull;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.AbstractMaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialRequirement;
import sasa.progression.sasaEnhancedProgression.techtree.Technology;
import sasa.progression.sasaEnhancedProgression.techtree.requirements.MaterialTagRequirement;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
class TechResearchMenu {

    private final Technology technology;
    private final MerchantView researchView;

    TechResearchMenu(Technology technology, Player player) {
        this.technology = technology;

        Merchant merchant = Bukkit.createMerchant();
        int parts = technology.getParts();
        List<AbstractMaterialRequirement> requirements = technology.getRequirements();
        List<MerchantRecipe> merchantRecipeList = new ArrayList<>();
        for (AbstractMaterialRequirement requirement : requirements) {
            int partSize = Math.ceilDiv(requirement.getNeeded(), parts);
            MerchantRecipe merchantRecipe;
            switch (requirement) {
                case MaterialRequirement mr -> {
                    ItemType itemType = mr.getItemType();
                    merchantRecipe = new MerchantRecipe(itemType.createItemStack(mr.getNeeded()), partSize);
                    merchantRecipe.addIngredient(itemType.createItemStack());
                }
                case MaterialTagRequirement tr -> {
                    Tag<ItemType> tag = tr.getTag();
                    // Pick one random material from the materials behind tag
                    ItemType itemType = Registry.ITEM.get(tag.values().stream().findFirst().orElseThrow().key());
                    assert itemType != null;
                    ItemStack output = itemType.createItemStack(tr.getNeeded());
                    String tagName = tag.tagKey().key().value();
                    setItemStackName(output, tagName);
                    ItemStack input = itemType.createItemStack();
                    setItemStackName(input, tagName);

                    merchantRecipe = new MerchantRecipe(output, partSize);
                    merchantRecipe.addIngredient(input);
                }
                default -> throw new IllegalStateException("Unexpected value: " + requirement);
            }
            merchantRecipeList.add(merchantRecipe);
        }
        merchant.setRecipes(merchantRecipeList);

        String technologyName = PlainTextComponentSerializer.plainText().serialize(technology.getConnectedAdvancement().displayName());
        technologyName = technologyName.substring(1, technologyName.length() - 1); // remove [ and ]
        researchView = MenuType.MERCHANT.builder()
                .title(Component.text(technologyName))
                .merchant(merchant)
                .build(player);
    }

    MerchantView getView() {
        return researchView;
    }

    Technology getTechnology() {
        return technology;
    }

    private void setItemStackName(ItemStack item, String name) {
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.displayName(Component.text("ANY " + name));
        item.setItemMeta(itemMeta);
    }
}
