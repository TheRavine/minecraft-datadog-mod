package io.coded.minecraft_datadog.mixin;

import io.coded.minecraft_datadog.StatsMod;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLookup;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
    private final static double NS_PER_MS = 1000000.0;

    @Shadow
    @Final
    public long[] lastTickLengths;

    @Shadow
    private int ticks;

    @Shadow
    public abstract PlayerManager getPlayerManager();

    @Shadow
    public abstract Iterable<ServerWorld> getWorlds();

    @Inject(at = @At("TAIL"), method = "tick")
    void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        // Convert ns to ms to make it easier to reason about
        double currentTickLength = (double) this.lastTickLengths[this.ticks % this.lastTickLengths.length] / NS_PER_MS;

        StatsMod.reportTick(currentTickLength);

        if (this.ticks % 300 == 0) {
            PlayerManager playerManager = this.getPlayerManager();
            StatsMod.reportPlayers(playerManager.getCurrentPlayerCount(), playerManager.getMaxPlayerCount());
        }

         else if (this.ticks % 300 == 100) {
            for (ServerWorld world : this.getWorlds()) {
                String dimensionName = world.getRegistryKey().getValue().toString();
                StatsMod.reportChunks(
                        dimensionName,
                        world.getChunkManager().getLoadedChunkCount());
            }
        }

         else if (this.ticks % 300 == 200) {
            for (ServerWorld world : this.getWorlds()) {
                String dimensionName = world.getRegistryKey().getValue().toString();

                ServerWorldAccessor swa = ((ServerWorldAccessor) world);
                WorldAccessor wa = ((WorldAccessor) world);

                // Grab the EntityIndex so we can get the count
                ServerEntityManager<Entity> entityManager = swa.getEntityManager();
                EntityIndex<Entity> entityIndex = ((ServerEntityManagerAccessor) entityManager).getEntityIndex();

                StatsMod.reportEntities(
                        dimensionName,
                        entityIndex.size(),
                        wa.getBlockEntityTickers().size());
            }
        }
    }
}
