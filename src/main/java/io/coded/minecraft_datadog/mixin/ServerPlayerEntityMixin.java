package io.coded.minecraft_datadog.mixin;

import com.mojang.authlib.GameProfile;
import io.coded.minecraft_datadog.StatsMod;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile profile) {
        super(world, pos, yaw, profile);
    }

    @Inject(at = @At("HEAD"), method = "onDeath")
    void onDeath(DamageSource damageSource, CallbackInfo ci) {
        Text rawDeathMessage = this.getDamageTracker().getDeathMessage();

        if (!(rawDeathMessage instanceof TranslatableText)) {
            LOGGER.warn("Death message wrong type. Expected TranslatableText.");
            return;
        }

        TranslatableText deathMessage = (TranslatableText) rawDeathMessage;
        Object[] args = deathMessage.getArgs();

        String deathActor = null;
        if (args.length > 1) {
            if (args[1] instanceof Text) {
                deathActor = ((Text) args[1]).getString().replace("[", "").replace("]", "");
            } else {
                deathActor = args[1].toString();
            }
        }

        String deathWeapon = null;
        if (args.length > 2) {
            if (args[2] instanceof Text) {
                deathWeapon = ((Text) args[2]).getString().replace("[", "").replace("]", "");
            } else {
                deathWeapon = args[2].toString();
            }
        }

        StatsMod.reportDeath(this.getUuid(), this.getGameProfile().getName(), deathMessage.getKey(), deathActor, deathWeapon);
    }
}
