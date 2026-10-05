package com.example.unlimitedxp;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unlimited XP
 *
 * Every server tick, tops every online player's XP level back up to a
 * configurable target (default 999) whenever it drops below that.
 *
 * Commands (require operator / permission level 2):
 *   /unlimitedxp toggle        - turn the effect on or off
 *   /unlimitedxp status        - show current state and target level
 *   /unlimitedxp level <value> - change the target XP level
 */
public class UnlimitedXpMod implements ModInitializer {
    public static final String MOD_ID = "unlimited-xp";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static volatile boolean enabled = true;
    private static volatile int targetLevel = 999;

    @Override
    public void onInitialize() {
        LOGGER.info("Unlimited XP initialized (target level: {})", targetLevel);

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            if (!enabled) {
                return;
            }
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                if (player.experienceLevel < targetLevel) {
                    player.giveExperienceLevels(targetLevel - player.experienceLevel);
                }
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(Commands.literal("unlimitedxp")
                .requires(source -> true)
                .then(Commands.literal("toggle").executes(ctx -> {
                    enabled = !enabled;
                    boolean state = enabled;
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("Unlimited XP is now " + (state ? "ON" : "OFF")),
                        true
                    );
                    return 1;
                }))
                .then(Commands.literal("status").executes(ctx -> {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal(
                            "Unlimited XP: " + (enabled ? "ON" : "OFF") + ", target level: " + targetLevel
                        ),
                        false
                    );
                    return 1;
                }))
                .then(Commands.literal("level")
                    .then(Commands.argument("value", IntegerArgumentType.integer(0))
                        .executes(ctx -> {
                            targetLevel = IntegerArgumentType.getInteger(ctx, "value");
                            int level = targetLevel;
                            ctx.getSource().sendSuccess(
                                () -> Component.literal("Unlimited XP target level set to " + level),
                                true
                            );
                            return 1;
                        })))
            )
        );
    }
}
