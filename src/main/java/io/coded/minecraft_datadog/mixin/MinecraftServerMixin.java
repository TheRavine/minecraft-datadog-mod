package io.coded.minecraft_datadog.mixin;

import io.coded.minecraft_datadog.StatsMod;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    private final static double NS_PER_MS = 1000000.0;

    @Shadow
    @Final
    public long[] lastTickLengths;

    @Shadow
    private int ticks;

    @Inject(at = @At("TAIL"), method = "tick")
    void tick(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        // Convert ns to ms to make it easier to reason about
        double currentTickLength = (double) this.lastTickLengths[this.ticks % this.lastTickLengths.length] / NS_PER_MS;

        StatsMod.reportTick(currentTickLength);
    }
}
