package com.azukii.diet.mixin;

import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.food.FoodCategories;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.core.food.ModFoodData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {
    @ModifyConstant(
            method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V",
            constant = @Constant(floatValue = 4.0F)
    )
    private float diet$modifyExhaustionThreshold(float original, ServerPlayer player) {
        int thirstLevel = player.getData(ModAttachments.THIRST_DATA).getThirstLevel();
        return (float) thirstLevel;
    }

    /**
     * We enforce the saturation cap here
     * cap = 20 × average(barLevel%) across all 5 diet categories.
     */
    @Inject(at = @At("TAIL"), method = "tick(Lnet/minecraft/server/level/ServerPlayer;)V")
    private void afterTick(ServerPlayer player, CallbackInfo ci) {
        FoodData self = (FoodData)(Object) this;
        float cap = diet$computeSaturationCap(player);
        if (self.getSaturationLevel() > cap) {
            self.setSaturation(cap);
        }
    }

    @Unique
    private float diet$computeSaturationCap(Player player) {
        ModFoodData data = player.getData(ModAttachments.FOOD_DATA);
        int sum = 0;
        for (FoodCategories cat : FoodCategories.VALUES) {
            sum += data.getLevel(cat);
        }
        return (float) sum;
    }
}
