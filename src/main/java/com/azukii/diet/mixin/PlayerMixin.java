package com.azukii.diet.mixin;

import com.azukii.diet.activity.ActivitiesCategories;
import com.azukii.diet.activity.ActivityEvents;
import com.azukii.diet.activity.PlayerActivitiesAccess;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.PlayerActivityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static java.lang.Math.max;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerActivitiesAccess {
    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    protected void tail(CallbackInfo ci) {
        Player player = (((Player) (Object) this));

        if (!player.getActiveEffects().isEmpty()) {
            for (MobEffectInstance effect : player.getActiveEffects()) {
                if (effect.getEffect() == MobEffects.HUNGER) {
                    player.causeFoodExhaustion(0.0025F * (float) (player.getEffect(MobEffects.HUNGER).getAmplifier() + 1) * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.EFFECT));
                    break;
                }
            }
        }

        float reduction = 0;

        double disX = player.getX() - player.xOld;
        double disZ = player.getZ() - player.zOld;

        if (player.level().isClientSide() ^ player.getData(ModAttachments.PLAYER_ACTIVITY).getHurtOrHeal()) {
            PlayerActivityData data = player.getData(ModAttachments.PLAYER_ACTIVITY);
            data.setSheenCooldown(data.getSheenCooldown() - 1);
        }

        if (player.getDeltaMovement().length() == 0.0 || disX == 0.0 && disZ == 0.0) {
            return;
        }

        int distance = Math.round(Mth.sqrt((float) disX * (float) disX + (float) disZ * (float) disZ) * 100.0F);

        if (player.isSwimming() || player.isEyeInFluid(FluidTags.WATER)) {
            reduction = 0.0001F * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.SWIM) * Math.round(Mth.sqrt((float) disX * (float) disX + (float) disZ * (float) disZ) * 100.0F);
        } else if (player.isInWater()) {
            reduction = 0.0001F * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.SWIM) * distance;
        } else if (player.onGround() && player.isSprinting()) {
            reduction = 0.001F * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.SPRINT) * distance;
        }

        player.getFoodData().addExhaustion(reduction);
    }

    @Inject(at = @At("TAIL"), method = "attack")
    protected void attack(CallbackInfo ci) {
        Player player = (((Player) (Object) this));
        float ratio = ActivityEvents.exhaustionReductionLongSheen(player, ActivitiesCategories.ATTACK);
        player.causeFoodExhaustion(0.1F * ratio);
    }

    @Inject(at = @At("TAIL"), method = "hurtServer")
    protected void hurt(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        Player player = (((Player) (Object) this));
        float ratio = ActivityEvents.exhaustionReductionLongSheen(player, ActivitiesCategories.HURT);
        player.causeFoodExhaustion(source.getFoodExhaustion() * ratio);
    }
}
