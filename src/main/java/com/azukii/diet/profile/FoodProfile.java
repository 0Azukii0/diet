package com.azukii.diet.profile;

import com.azukii.diet.data.FoodCategories;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

import java.util.EnumMap;

/**
 * Immutable per-item diet contribution profile.
 */
public record FoodProfile(float[] values) {
    public static final FoodProfile EMPTY = new FoodProfile(new float[FoodCategories.COUNT]);

    public static final Codec<FoodProfile> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("grain", 0.0f).forGetter(profile -> profile.get(FoodCategories.GRAIN)),
            Codec.FLOAT.optionalFieldOf("protein", 0.0f).forGetter(profile -> profile.get(FoodCategories.PROTEIN)),
            Codec.FLOAT.optionalFieldOf("vegetable", 0.0f).forGetter(profile -> profile.get(FoodCategories.VEGETABLE)),
            Codec.FLOAT.optionalFieldOf("fruit", 0.0f).forGetter(profile -> profile.get(FoodCategories.FRUIT)),
            Codec.FLOAT.optionalFieldOf("sugar", 0.0f).forGetter(profile -> profile.get(FoodCategories.SUGAR))
    ).apply(instance, FoodProfile::of));

    public static FoodProfile of(float grain, float protein, float vegetable, float fruit, float sugar) {
        float[] array = new float[FoodCategories.COUNT];
        array[FoodCategories.GRAIN.ordinal()] = clamp(grain);
        array[FoodCategories.PROTEIN.ordinal()] = clamp(protein);
        array[FoodCategories.VEGETABLE.ordinal()] = clamp(vegetable);
        array[FoodCategories.FRUIT.ordinal()] = clamp(fruit);
        array[FoodCategories.SUGAR.ordinal()] = clamp(sugar);
        return new FoodProfile(array);
    }

    private static float clamp(float value) {
        return Mth.clamp(value, 0.0f, 1000.0f);
    }

    public float get(FoodCategories category) {
        return values[category.ordinal()];
    }

    public float[] copyValues() {
        float[] copy = new float[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        return copy;
    }

    public boolean isEmpty() {
        for (float value : values) {
            if (value > 0.0f) {
                return false;
            }
        }
        return true;
    }

    public static FoodProfile fromEnumMap(EnumMap<FoodCategories, Float> map) {
        float[] array = new float[FoodCategories.COUNT];
        for (FoodCategories category : FoodCategories.VALUES) {
            array[category.ordinal()] = clamp(map.getOrDefault(category, 0.0f));
        }
        return new FoodProfile(array);
    }
}