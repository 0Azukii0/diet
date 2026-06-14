package com.azukii.diet.item;

import com.azukii.diet.core.food.FoodCategories;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Immutable per-item diet contribution profile.
 */
public final class FoodItemProfile {
    public static final FoodItemProfile EMPTY = new FoodItemProfile(Collections.emptyMap());

    private final Map<FoodCategories, Float> values;

    private FoodItemProfile(Map<FoodCategories, Float> values) {
        this.values = values;
    }

    public static FoodItemProfile single(FoodCategories category, float value) {
        if (value <= 0f) return EMPTY;
        return new FoodItemProfile(Map.of(category, value));
    }

    public static FoodItemProfile of(Map<FoodCategories, Float> values) {
        if (values == null || values.isEmpty()) return EMPTY;

        EnumMap<FoodCategories, Float> clean = new EnumMap<>(FoodCategories.class);
        values.forEach((cat, v) -> {
            if (v > 0f) clean.put(cat, v);
        });

        return clean.isEmpty() ? EMPTY : new FoodItemProfile(Collections.unmodifiableMap(clean));
    }

    public float get(FoodCategories category) {
        return values.getOrDefault(category, 0f);
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Map<FoodCategories, Float> values() {
        return values;
    }

    public float total() {
        float sum = 0f;
        for (float v : values.values()) sum += v;
        return sum;
    }

    public float ratioFor(FoodCategories category) {
        float t = total();
        return t == 0f ? 0f : get(category) / t;
    }

    public boolean hasCategory(FoodCategories category) {
        return values.containsKey(category);
    }

    @Override
    public String toString() {
        if (isEmpty()) return "FoodProfile{EMPTY}";
        StringJoiner sj = new StringJoiner(", ", "FoodProfile{", "}");
        values.forEach((cat, v) -> sj.add(cat.name().toLowerCase() + "=" + v));
        return sj.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FoodItemProfile other && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}