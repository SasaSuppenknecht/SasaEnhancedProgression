package sasa.progression.sasaEnhancedProgression.features;

import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.tag.Tag;
import io.papermc.paper.registry.tag.TagKey;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.BlockType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;
import sasa.progression.sasaEnhancedProgression.events.TechnologyUnlockEvent;

public class FarmingInteractionHandler extends AbstractFeature {

    private final Tag<BlockType> CROPS;
    private boolean isFarmingEnabled = false;

    public FarmingInteractionHandler() {
        CROPS = Registry.BLOCK.getTag(TagKey.create(RegistryKey.BLOCK, NamespacedKey.minecraft("crops")));
    }

    @EventHandler
    public void onSoilCreation(BlockPlaceEvent event) {
        if (!isFarmingEnabled) {
            BlockType placedBlockType = event.getBlock().getType().asBlockType();
            if (placedBlockType == BlockType.FARMLAND ||
                    CROPS.contains(TypedKey.create(RegistryKey.BLOCK, placedBlockType.getKey()))) {
                event.setCancelled(true);
                event.getPlayer().sendMessage("You don't know how this works yet.");
            }
        }

    }

    @Override
    @EventHandler
    public void onTechnologyUnlock(TechnologyUnlockEvent event) {
        if (event.getAdvancementKey().getKey().endsWith("farming")) {
            isFarmingEnabled = true;
        }
    }
}
