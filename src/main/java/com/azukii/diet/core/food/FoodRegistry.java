package com.azukii.diet.core.food;

import com.azukii.diet.data.DatapackConfiguration;
import com.azukii.diet.data.ModItemTagsProvider;
import com.azukii.diet.event.ModClientEvents;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Central registry for diet profiles and configuration, backed by datapacks.
 */
public final class FoodRegistry {
    private static DatapackConfiguration config;
    private static final Map<Identifier, FoodItemProfile> cache = new HashMap<>();

    public static void rebuildCache() {
        cache.clear();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (!BuiltInRegistries.ITEM.wrapAsHolder(item).is(ModItemTagsProvider.FOODS_CATEGORIES)) continue;

            FoodProperties food = item.components().get(DataComponents.FOOD);
            if (food == null) continue;

            Map<FoodCategories, Float> explicit = config.explicitProfile(item);
            FoodItemProfile profile = explicit != null ? FoodItemProfile.of(explicit) : computeFromTags(item, food.nutrition());

            if (!profile.isEmpty()) {
                cache.put(id, profile);
            }
        }
    }

    private static FoodItemProfile computeFromTags(Item item, int nutrition) {
        float value = nutrition * config.tagValueMultiplier();
        EnumMap<FoodCategories, Float> values = new EnumMap<>(FoodCategories.class);

        for (FoodCategories cat : FoodCategories.VALUES) {
            if (BuiltInRegistries.ITEM.wrapAsHolder(item).is(cat.getTag())) {
                values.put(cat, value);
            }
        }

        return FoodItemProfile.of(values);
    }

    public static FoodItemProfile getProfile(ItemStack stack) {
        return cache.getOrDefault(BuiltInRegistries.ITEM.getKey(stack.getItem()), FoodItemProfile.EMPTY);
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