package com.azukii.diet.data;

import com.azukii.diet.profile.FoodProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for diet profiles and configuration, backed by datapacks.
 */
public final class FoodRegistry {
    private static DatapackConfiguration config;
    private static final Map<Identifier, FoodProfile> cache = new HashMap<>();

    public static void rebuildCache() {
        cache.clear();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!item.builtInRegistryHolder().is(ModItemTagsProvider.FOODS_CATEGORIES)) continue;

            FoodProperties food = item.components().get(DataComponents.FOOD);
            if (food == null) continue;

            Map<FoodCategories, Float> explicit = config.explicitProfile(id);
            FoodProfile profile = explicit != null ? FoodProfile.of(explicit) : computeFromTags(item, food.nutrition());

            if (!profile.isEmpty()) {
                cache.put(id, profile);
            }
        }
    }

    private static FoodProfile computeFromTags(Item item, int nutrition) {
        float value = nutrition * config.tagValueMultiplier();
        for (FoodCategories cat : FoodCategories.VALUES) {
            if (item.builtInRegistryHolder().is(cat.getTag())) {
                return FoodProfile.single(cat, value);
            }
        }
        return FoodProfile.EMPTY;
    }

    public static FoodProfile getProfile(ItemStack stack) {
        return cache.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), FoodProfile.EMPTY);
    }

    public static float getMaxValue() {
        return config != null ? config.maxValues() : DatapackConfiguration.DEFAULT_MAX_VALUES;
    }

    public static DatapackConfiguration getConfig() {
        return config;
    }

    public static void reset() {
        config = DatapackConfiguration.defaults();
        cache.clear();
    }

    public static void reset(DatapackConfiguration newConfig) {
        config = newConfig;
        cache.clear();
    }
}