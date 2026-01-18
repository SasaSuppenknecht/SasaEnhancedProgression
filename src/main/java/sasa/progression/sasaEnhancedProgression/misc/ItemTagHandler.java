package sasa.progression.sasaEnhancedProgression.misc;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class ItemTagHandler {

    public static NamespacedKey getItemTagKey(Tag<ItemType> tag) {
        return (NamespacedKey) tag.tagKey().key();
    }

    public static Tag<ItemType> getItemTag(NamespacedKey tagKey) {
        return Registry.ITEM.getTag(TagKey.create(RegistryKey.ITEM, tagKey));
    }

    public static ItemType getRandomItemFromItemTag(Tag<ItemType> tag) {
        return Registry.ITEM.get(tag.values().stream().findFirst().orElseThrow().key());
    }

    public static boolean isItemTypeInItemTag(Tag<ItemType> tag, ItemType itemType) {
        return tag.contains(TypedKey.create(RegistryKey.ITEM, itemType.getKey()));
    }

    public static Set<ItemType> getItemTypesInItemTag(Tag<ItemType> tag) {
        return tag.values().stream().map(Registry.ITEM::get).collect(Collectors.toSet());
    }

    public static String getTagName(Tag<ItemType> tag) {
        return tag.tagKey().toString().split("\\s|:")[1].replace("_", " ");
    }

}
