package com.azukii.diet.mixin;

import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.data.ModFoodData;
import com.azukii.diet.profile.FoodProfile;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodData.class)
public class FoodDataMixin {
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
        FoodProfile maxValues = FoodRegistry.getMaxValues();
        float sum = 0f;
        for (FoodCategories cat : FoodCategories.VALUES) {
            float max = maxValues.get(cat);
            sum += max > 0f ? Math.clamp(data.get(cat) / max, 0f, 1f) : 0f;
        }
        return (sum / FoodCategories.COUNT) * 20f;
    }
}
