package sasa.progression.sasaEnhancedProgression.misc;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@SuppressWarnings("UnstableApiUsage")
public class ItemTagHandler {

    public static NamespacedKey getItemTagKey(Tag<ItemType> itemTag) {
        return (NamespacedKey) itemTag.tagKey().key();
    }

    public static Tag<ItemType> getItemTag(NamespacedKey itemTagKey) {
        return Registry.ITEM.getTag(TagKey.create(RegistryKey.ITEM, itemTagKey));
    }

    public static ItemType getRandomItemFromItemTag(Tag<ItemType> itemTag) {
        return Registry.ITEM.get(itemTag.values().stream().findFirst().orElseThrow().key());
    }

    public static boolean isItemInItemTag(Tag<ItemType> tag, ItemType itemType) {
        return tag.contains(TypedKey.create(RegistryKey.ITEM, itemType.getKey()));
    }

    public static boolean isItemInItemTag(Tag<ItemType> tag, ItemStack itemStack) {
        return isItemInItemTag(tag, Objects.requireNonNull(itemStack.getType().asItemType()));
    }

}
