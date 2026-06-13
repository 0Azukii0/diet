package com.azukii.diet.profile;

import com.azukii.diet.data.FoodCategories;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.StringJoiner;

/**
 * Immutable per-item diet contribution profile.
 */
public final class FoodProfile {
    public static final FoodProfile EMPTY = new FoodProfile(Collections.emptyMap());

    private final Map<FoodCategories, Float> values;

    private FoodProfile(Map<FoodCategories, Float> values) {
        this.values = values;
    }

    public static FoodProfile single(FoodCategories category, float value) {
        if (value <= 0f) return EMPTY;
        return new FoodProfile(Map.of(category, value));
    }

    public static FoodProfile of(Map<FoodCategories, Float> values) {
        if (values == null || values.isEmpty()) return EMPTY;

        EnumMap<FoodCategories, Float> clean = new EnumMap<>(FoodCategories.class);
        values.forEach((cat, v) -> {
            if (v > 0f) clean.put(cat, v);
        });

        return clean.isEmpty() ? EMPTY : new FoodProfile(Collections.unmodifiableMap(clean));
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
        return obj instanceof FoodProfile other && values.equals(other.values);
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }
}