package com.azukii.diet.registry;

import net.minecraft.world.food.FoodProperties;

public class ModFoods {
    /// FRUITS
    public static final FoodProperties APPLE = register(4, 0.3F);
    public static final FoodProperties GOLDEN_APPLE = register(5, 1.2F, true);
    public static final FoodProperties ENCHANTED_GOLDEN_APPLE = register(5, 1.2F, true);
    public static final FoodProperties MELON_SLICE = register(2, 0.3F);
    public static final FoodProperties GLISTERING_MELON_SLICE = register(3, 0.6F, true);
    public static final FoodProperties CHORUS_FRUIT = register(4, 0.3F, true);
    public static final FoodProperties SWEET_BERRIES = register(2, 0.1F);
    public static final FoodProperties GLOW_BERRIES = register(2, 0.1F);
    /// GRAINS
    public static final FoodProperties WHEAT = register(2, 0.1F);
    public static final FoodProperties BREAD = register(5, 0.6F);
    public static final FoodProperties COOKIE = register(2, 0.1F);
    public static final FoodProperties PUMPKIN_PIE = register(6, 0.3F);
    /// PROTEINS
    public static final FoodProperties BEEF = register(3, 0.3F);
    public static final FoodProperties CHICKEN = register(2, 0.3F);
    public static final FoodProperties MUTTON = register(2, 0.3F);
    public static final FoodProperties PORKCHOP = register(3, 0.3F);
    public static final FoodProperties RABBIT = register(2, 0.3F);
    public static final FoodProperties COOKED_BEEF = register(8, 0.8F);
    public static final FoodProperties COOKED_CHICKEN = register(6, 0.6F);
    public static final FoodProperties COOKED_MUTTON = register(6, 0.8F);
    public static final FoodProperties COOKED_PORKCHOP = register(8, 0.8F);
    public static final FoodProperties COOKED_RABBIT = register(5, 0.6F);
    public static final FoodProperties PUFFERFISH = register(1, 0.1F);
    public static final FoodProperties TROPICAL_FISH = register(1, 0.1F);
    public static final FoodProperties COD = register(2, 0.1F);
    public static final FoodProperties SALMON = register(2, 0.1F);
    public static final FoodProperties COOKED_COD = register(5, 0.6F);
    public static final FoodProperties COOKED_SALMON = register(6, 0.8F);
    public static final FoodProperties RABBIT_STEW = register(10, 0.6F);
    public static final FoodProperties MILK = register(5, 2.0F);
    public static final FoodProperties ROTTEN_FLESH = register(4, 0.1F);
    public static final FoodProperties SPIDER_EYE = register(2, 0.4F);
    public static final FoodProperties FERMENTED_SPIDER_EYE = register(3, 0.8F);
    public static final FoodProperties EGG = register(2, 0.3F);
    /// VEGETABLES
    public static final FoodProperties POTATO = register(2, 0.3F);
    public static final FoodProperties POISONOUS_POTATO = register(3, 0.3F);
    public static final FoodProperties BAKED_POTATO = register(5, 0.6F);
    public static final FoodProperties BEETROOT = register(1, 0.6F);
    public static final FoodProperties BEETROOT_SOUP = register(6, 0.6F);
    public static final FoodProperties CARROT = register(3, 0.6F);
    public static final FoodProperties GOLDEN_CARROT = register(4, 1.2F, true); //TODO : add effect
    public static final FoodProperties DRIED_KELP = register(1, 0.3F);
    public static final FoodProperties BROWN_MUSHROOM = register(2, 0.1F);
    public static final FoodProperties RED_MUSHROOM = register(2, 0.1F);
    public static final FoodProperties MUSHROOM_STEW = register(6, 0.6F);
    public static final FoodProperties SEA_PICKLE = register(1, 1.0F);
    public static final FoodProperties SUSPICIOUS_STEW = register(6, 0.6F);
    /// SUGARS
    public static final FoodProperties COCOA_BEANS = register(1, 0.3F);
    public static final FoodProperties HONEY_BOTTLE = register(4, 0.2F);
    public static final FoodProperties SUGAR = register(1, 0.1F);

    private static FoodProperties register(int nutrition, float saturation) {
        return register(nutrition, saturation, false);
    }

    private static FoodProperties register(int nutrition, float saturation, boolean alwaysEdible) {
        FoodProperties.Builder builder = (new FoodProperties.Builder()).nutrition(nutrition).saturationModifier(saturation);
        if (alwaysEdible) builder.alwaysEdible();
        return builder.build();
    }
}
