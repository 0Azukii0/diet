package com.azukii.diet.mixin;

import com.azukii.diet.registry.FoodDataCategories;
import com.azukii.diet.registry.FoodDataMixinAccess;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FoodProperties.class)
public class FoodPropertiesMixin {
    @Shadow
    @Final
    private float saturation;

    @Inject(at = @At("HEAD"), method = "onConsume(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/component/Consumable;)V")
    public void setSaturation(Level level, LivingEntity user, ItemStack stack, Consumable consumable, CallbackInfo ci) {
        if (!(user instanceof ServerPlayer player)) return;
        FoodData foodData = player.getFoodData();
        FoodDataCategories[] categories = FoodDataCategories.fromItemStack(stack);
        if (foodData instanceof FoodDataMixinAccess access) {
            for (FoodDataCategories category : categories) {
                float saturationLevel = access.diet$getCategorySaturationLevels(category);
                float newSaturationLevel = Mth.clamp(this.saturation + saturationLevel, 0.0F, foodData.getFoodLevel());
                access.diet$setCategorySaturationLevels(category, newSaturationLevel);
            }
        }
    }
}
