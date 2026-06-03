package com.azukii.diet.mixin;
import com.azukii.diet.activity.ActivitiesCategories;
import com.azukii.diet.activity.ActivityEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(at = @At("TAIL"), method = "heal")
    protected void heal(float heal, CallbackInfo ci) {
        if((((LivingEntity) (Object) this)) instanceof Player player) {
            float ratio = ActivityEvents.exhaustionReductionLongSheen(player, ActivitiesCategories.HEAL);
            player.causeFoodExhaustion(6.0F * heal * ratio);
        }
    }

    @Inject(at = @At("TAIL"), method = "jumpFromGround")
    protected void jumpFromGround(CallbackInfo ci) {
        if((((LivingEntity) (Object) this)) instanceof Player player) {
            float ratio = ActivityEvents.exhaustionReductionLongSheen(player, ActivitiesCategories.JUMP);
            if (player.isSprinting()) {
                player.causeFoodExhaustion(0.2F * ratio);
            } else {
                player.causeFoodExhaustion(0.05F * ratio);
            }
        }
    }
}
