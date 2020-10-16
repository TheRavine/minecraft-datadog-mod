package io.coded.minecraft_datadog.mixin;

import io.coded.minecraft_datadog.StatsMod;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    private void report() {
        StatsMod.reportPlayers((PlayerManager) (Object) this);
    }

    @Inject(at = @At("HEAD"), method = "onPlayerConnect")
    public void onPlayerConnectHead(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        this.report();
    }

    @Inject(at = @At("TAIL"), method = "onPlayerConnect")
    public void onPlayerConnectTail(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        this.report();
    }

    @Inject(at = @At("HEAD"), method = "remove")
    public void removeHead(ServerPlayerEntity player, CallbackInfo ci) {
        this.report();
    }

    @Inject(at = @At("TAIL"), method = "remove")
    public void removeTail(ServerPlayerEntity player, CallbackInfo ci) {
        this.report();
    }
}
