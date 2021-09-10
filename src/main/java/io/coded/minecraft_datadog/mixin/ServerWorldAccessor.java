package io.coded.minecraft_datadog.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.BlockEntityTickInvoker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(ServerWorld.class)
public interface ServerWorldAccessor {
    @Accessor("entityManager")
    ServerEntityManager<Entity> getEntityManager();
}
