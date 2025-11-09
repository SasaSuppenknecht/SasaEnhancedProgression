package sasa.progression.sasaEnhancedProgression.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class TechnologyCommand {

    public static LiteralCommandNode<CommandSourceStack> createCommand() {
        return Commands.literal("tech")
                .requires(sender -> sender instanceof Player)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    Player player = (Player) sender;
                    Inventory inventory = Bukkit.createInventory(player, 9);
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
