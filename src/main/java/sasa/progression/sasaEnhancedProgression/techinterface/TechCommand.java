package sasa.progression.sasaEnhancedProgression.techinterface;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import sasa.progression.sasaEnhancedProgression.techtree.TechProgress;

public class TechCommand {

    private static TechMenu techMenu = null;

    public static LiteralCommandNode<CommandSourceStack> createCommand(TechProgress techProgress) {
        techMenu = new TechMenu(techProgress);
        return Commands.literal("tech")
                .requires(source -> source.getSender() instanceof Player)
                .executes(ctx -> {
                    CommandSender sender = ctx.getSource().getSender();
                    techMenu.openSelectionMenuForPlayer((Player) sender);
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}
