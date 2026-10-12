package com.azukii.diet.registry;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface FoodDataMixinAccess {
    void diet$setCategorySaturationLevels(FoodDataCategories category, float saturation);

    float diet$getCategorySaturationLevels(FoodDataCategories category);

    void diet$setAllCategorySaturationLevels(float[] values);

    void diet$setCurrentActivity(FoodDataCategories.Activity currentActivity);

    ItemStack diet$getLastFoodEaten();

    void diet$setLastFoodEaten(ItemStack stack);

    void diet$setLastFoodEatenServer(ItemStack stack);

    void diet$syncToClient(ServerPlayer player);
}
