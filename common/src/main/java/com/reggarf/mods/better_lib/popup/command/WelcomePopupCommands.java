package com.reggarf.mods.better_lib.popup.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.reggarf.mods.better_lib.popup.api.WelcomePopupPlugins;
import com.reggarf.mods.better_lib.popup.manager.WelcomePopupManager;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class WelcomePopupCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // Register /betterlib welcome, /betterlib popup, /betterlib report
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("betterlib")
                .then(Commands.literal("welcome")
                        .executes(ctx -> executeOpen(ctx.getSource(), null))
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .executes(ctx -> executeOpen(ctx.getSource(), StringArgumentType.getString(ctx, "modid")))))
                .then(Commands.literal("popup")
                        .executes(ctx -> executeOpen(ctx.getSource(), null))
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .executes(ctx -> executeOpen(ctx.getSource(), StringArgumentType.getString(ctx, "modid")))))
                .then(Commands.literal("report")
                        .executes(ctx -> executeOpen(ctx.getSource(), null))
                        .then(Commands.argument("modid", StringArgumentType.string())
                                .executes(ctx -> executeOpen(ctx.getSource(), StringArgumentType.getString(ctx, "modid")))));

        dispatcher.register(root);

        // Register convenient top-level aliases /welcome and /report
        dispatcher.register(Commands.literal("welcome")
                .executes(ctx -> executeOpen(ctx.getSource(), null))
                .then(Commands.argument("modid", StringArgumentType.string())
                        .executes(ctx -> executeOpen(ctx.getSource(), StringArgumentType.getString(ctx, "modid")))));

        dispatcher.register(Commands.literal("report")
                .executes(ctx -> executeOpen(ctx.getSource(), null))
                .then(Commands.argument("modid", StringArgumentType.string())
                        .executes(ctx -> executeOpen(ctx.getSource(), StringArgumentType.getString(ctx, "modid")))));
    }

    private static int executeOpen(CommandSourceStack source, String targetModId) {
        if (!WelcomePopupPlugins.hasAny()) {
            source.sendFailure(Component.literal("No mods have registered a welcome popup!")
                    .withStyle(ChatFormatting.RED));
            return 0;
        }

        try {
            // Open screen on the client
            WelcomePopupManager.openScreenForMod(targetModId, null);
            source.sendSuccess(() -> Component.literal("Opening welcome and support menu...")
                    .withStyle(ChatFormatting.GREEN), false);
            return Command.SINGLE_SUCCESS;
        } catch (Throwable t) {
            source.sendFailure(Component.literal("Failed to open welcome screen: " + t.getMessage())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
    }
}
