package sasa.progression.sasaEnhancedProgression.techinterface;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;

import java.util.Objects;

public class TechnologyCommand {

    public static LiteralCommandNode<CommandSourceStack> createCommand(TechProgress techProgress) {
        return Commands.literal("tech")
                .requires(source -> source.getSender() instanceof Player player)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Player player = (Player) sender;
                    Inventory inventory = Bukkit.createInventory(player, 9 * 3);
                    for (Advancement advancement : techProgress.getOpenTech()) {
                        ItemStack icon = Objects.requireNonNull(advancement.getDisplay()).icon();
                        ItemMeta itemMeta = icon.getItemMeta();
                        String title = PlainTextComponentSerializer.plainText().serialize(advancement.getDisplay().title());
                        itemMeta.displayName(Component.text(title));
                        icon.setItemMeta(itemMeta);
                        inventory.addItem(icon);
                    }
                    player.openInventory(inventory);
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

//    @Override
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        if (sender instanceof Player player) {
//            Inventory inventory = Bukkit.createInventory(player, 9);
//            player.openInventory(inventory);
//        } else {
//            sender.sendMessage("Only players can run this command");
//        }
//        return true;
//    }
}
