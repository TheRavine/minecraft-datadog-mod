package io.coded.minecraft_datadog.mixin;

import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityIndex;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerEntityManager.class)
public interface ServerEntityManagerAccessor<T extends EntityLike> {
    @Accessor("index")
    EntityIndex<T> getEntityIndex();
}

