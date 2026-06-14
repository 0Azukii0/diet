package com.azukii.diet.mixin.gui;

import com.azukii.diet.gui.screen.FoodStatsRenderState;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RecipeBookComponent.class)
public class RecipeBookComponentMixin {
    @Inject(method = "toggleVisibility", at = @At("HEAD"))
    private void blockWhenFoodStatsOpen(CallbackInfo ci) {
        if (FoodStatsRenderState.isOpen()) {
            FoodStatsRenderState.close();
        }
    }
}
