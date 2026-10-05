package com.example.deathrecover;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DeathRecoverMod implements ModInitializer {
    public static final String MOD_ID = "deathrecover";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static boolean enabled = true;
    private static final Map<UUID, SavedPlayerData> SAVED_DATA = new HashMap<>();

    private static class SavedPlayerData {
        final List<ItemStack> items = new ArrayList<>();
        final int experienceLevel;
        final float experienceProgress;
        final int totalExperience;
        final String deathDimension;

        SavedPlayerData(ServerPlayer player) {
            int size = player.getInventory().getContainerSize();
            for (int i = 0; i < size; i++) {
                items.add(player.getInventory().getItem(i).copy());
            }

            this.experienceLevel = player.experienceLevel;
            this.experienceProgress = player.experienceProgress;
            this.totalExperience = player.totalExperience;
            this.deathDimension = player.level().dimension().identifier().toString();
        }

        void restore(ServerPlayer player) {
            player.getInventory().clearContent();
            int size = player.getInventory().getContainerSize();

            for (int i = 0; i < size && i < items.size(); i++) {
                player.getInventory().setItem(i, items.get(i));
            }

            player.experienceLevel = this.experienceLevel;
            player.experienceProgress = this.experienceProgress;
            player.totalExperience = this.totalExperience;

            player.containerMenu.broadcastChanges();
        }
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Death Recover (Manual Mode) initialized!");

        // 1. Capture inventory before death and clear so nothing drops in lava/void
        ServerPlayerEvents.ALLOW_DEATH.register((player, damageSource, damageAmount) -> {
            if (enabled && player instanceof ServerPlayer serverPlayer) {
                SAVED_DATA.put(serverPlayer.getUUID(), new SavedPlayerData(serverPlayer));
                serverPlayer.getInventory().clearContent();
            }
            return true;
        });

        // 2. Alert player on respawn
        ServerPlayerEvents.COPY_FROM.register((oldPlayer, newPlayer, alive) -> {
            if (!enabled || alive) return;

            UUID uuid = oldPlayer.getUUID();
            if (SAVED_DATA.containsKey(uuid)) {
                newPlayer.sendSystemMessage(
                    Component.literal("§6[Item Recover]§r Your items are stored! Type §a/deathrecover get§r to claim them.")
                );
            }
        });

        // 3. Command registration: /deathrecover get
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("deathrecover")
                .requires(source -> true)
                .then(Commands.literal("get").executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayer();
                    if (player == null) {
                        ctx.getSource().sendFailure(Component.literal("Only players can run this command."));
                        return 0;
                    }

                    SavedPlayerData data = SAVED_DATA.remove(player.getUUID());
                    if (data == null) {
                        ctx.getSource().sendFailure(Component.literal("§c[Item Recover] No saved death items found to claim!"));
                        return 0;
                    }

                    data.restore(player);
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("§6[Item Recover]§a Restored all items & XP from " + data.deathDimension + "!"),
                        false
                    );
                    return 1;
                }))
                .then(Commands.literal("toggle").executes(ctx -> {
                    enabled = !enabled;
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("§6[Item Recover]§r Status: " + (enabled ? "§aON§r" : "§cOFF§r")),
                        false
                    );
                    return 1;
                }))
                .then(Commands.literal("status").executes(ctx -> {
                    ctx.getSource().sendSuccess(
                        () -> Component.literal("§6[Item Recover]§r Status: " + (enabled ? "§aON§r" : "§cOFF§r")),
                        false
                    );
                    return 1;
                }))
            );
        });
    }
}
