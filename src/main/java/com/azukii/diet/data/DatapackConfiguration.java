package com.azukii.diet.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the data-driven configuration for the diet system loaded from datapacks.
 */
public record DatapackConfiguration(
        int intervalSeconds,
        float maxValues,
        float tagValueMultiplier,
        Map<FoodCategories, Float> decayRates,
        Map<Identifier, Map<FoodCategories, Float>> items
) {
    private static final Logger LOGGER = LoggerFactory.getLogger(DatapackConfiguration.class);

    public static final int   DEFAULT_INTERVAL_SECONDS     = 120;
    public static final float DEFAULT_MAX_VALUES            = 100.0f;
    public static final float DEFAULT_TAG_VALUE_MULTIPLIER  = 1.0f;

    /** Applied when a decay_rates entry is missing for a category. */
    public static final float DEFAULT_DECAY_RATE = 0.20f;

    public static final Map<FoodCategories, Float> DEFAULT_DECAY_RATES = Map.of(
            FoodCategories.GRAIN,     0.15f,
            FoodCategories.PROTEIN,   0.20f,
            FoodCategories.VEGETABLE, 0.25f,
            FoodCategories.FRUIT,     0.25f,
            FoodCategories.SUGAR,     0.30f
    );
    /** Returned when no JSON file is found; the mod works out of the box. */
    public static DatapackConfiguration defaults() {
        return new DatapackConfiguration(
                DEFAULT_INTERVAL_SECONDS,
                DEFAULT_MAX_VALUES,
                DEFAULT_TAG_VALUE_MULTIPLIER,
                DEFAULT_DECAY_RATES,
                Collections.emptyMap()
        );
    }

    /**
     * Parses the config from a {@link JsonObject}.
     * Unknown keys are silently ignored so future versions don't break old datapacks.
     * Invalid values log a warning and fall back to their default.
     */
    public static DatapackConfiguration fromJson(JsonObject json) {
        int intervalSeconds = readInt(json, "interval_seconds", DEFAULT_INTERVAL_SECONDS);
        float maxValues     = readFloat(json, "max_values",             DEFAULT_MAX_VALUES);
        float tagMult       = readFloat(json, "tag_value_multiplier",   DEFAULT_TAG_VALUE_MULTIPLIER);

        Map<FoodCategories, Float> decayRates = parseDecayRates(
                json.has("decay_rates") ? json.getAsJsonObject("decay_rates") : null
        );

        Map<Identifier, Map<FoodCategories, Float>> items = parseItems(
                json.has("items") ? json.getAsJsonObject("items") : null
        );

        return new DatapackConfiguration(intervalSeconds, maxValues, tagMult, decayRates, items);
    }

    private static Map<FoodCategories, Float> parseDecayRates(JsonObject obj) {
        // Start from defaults so missing entries don't leave a category without a rate.
        EnumMap<FoodCategories, Float> result = new EnumMap<>(DEFAULT_DECAY_RATES);
        if (obj == null) return Collections.unmodifiableMap(result);

        for (Map.Entry<String, JsonElement> entry : obj.entrySet()) {
            FoodCategories category = FoodCategories.byName(entry.getKey());
            if (category == null) {
                LOGGER.warn("[Diet] Unknown category '{}' in decay_rates — skipped.", entry.getKey());
                continue;
            }
            try {
                float rate = entry.getValue().getAsFloat();
                if (rate < 0f || rate > 1f) {
                    LOGGER.warn("[Diet] decay_rates.{} = {} is outside [0,1] — clamped.", entry.getKey(), rate);
                    rate = Math.clamp(rate, 0f, 1f);
                }
                result.put(category, rate);
            } catch (Exception e) {
                LOGGER.warn("[Diet] Could not parse decay_rates.{} — using default {}.",
                        entry.getKey(), DEFAULT_DECAY_RATE);
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private static Map<Identifier, Map<FoodCategories, Float>> parseItems(JsonObject obj) {
        if (obj == null) return Collections.emptyMap();

        Map<Identifier, Map<FoodCategories, Float>> result = new HashMap<>();

        for (Map.Entry<String, JsonElement> itemEntry : obj.entrySet()) {
            Identifier id = Identifier.tryParse(itemEntry.getKey());
            if (id == null) {
                LOGGER.warn("[Diet] Invalid item id '{}' in items — skipped.", itemEntry.getKey());
                continue;
            }
            if (!itemEntry.getValue().isJsonObject()) {
                LOGGER.warn("[Diet] items.{} must be a JSON object — skipped.", itemEntry.getKey());
                continue;
            }

            EnumMap<FoodCategories, Float> profile = new EnumMap<>(FoodCategories.class);
            for (Map.Entry<String, JsonElement> catEntry : itemEntry.getValue().getAsJsonObject().entrySet()) {
                FoodCategories category = FoodCategories.byName(catEntry.getKey());
                if (category == null) {
                    LOGGER.warn("[Diet] Unknown category '{}' for item {} — skipped.",
                            catEntry.getKey(), itemEntry.getKey());
                    continue;
                }
                try {
                    float value = catEntry.getValue().getAsFloat();
                    if (value < 0f) {
                        LOGGER.warn("[Diet] Negative value for {}.{} — set to 0.", itemEntry.getKey(), catEntry.getKey());
                        value = 0f;
                    }
                    profile.put(category, value);
                } catch (Exception e) {
                    LOGGER.warn("[Diet] Could not parse value for {}.{} — skipped.",
                            itemEntry.getKey(), catEntry.getKey());
                }
            }

            if (!profile.isEmpty()) {
                result.put(id, Collections.unmodifiableMap(profile));
            }
        }
        return Collections.unmodifiableMap(result);
    }

    /** Returns the decay rate for a category, falling back to {@link #DEFAULT_DECAY_RATE}. */
    public float decayRate(FoodCategories category) {
        return decayRates.getOrDefault(category, DEFAULT_DECAY_RATE);
    }

    /**
     * Returns the explicit per-category values for an item if defined in the "items" section,
     * or {@code null} if the item should fall back to tag-based resolution.
     */
    public Map<FoodCategories, Float> explicitProfile(Identifier itemId) {
        return items.get(itemId);
    }

    private static int readInt(JsonObject json, String key, int fallback) {
        if (!json.has(key)) return fallback;
        try {
            int v = json.get(key).getAsInt();
            if (v <= 0) {
                LOGGER.warn("[Diet] '{}' must be > 0 — using default {}.", key, fallback);
                return fallback;
            }
            return v;
        } catch (Exception e) {
            LOGGER.warn("[Diet] Could not parse '{}' — using default {}.", key, fallback);
            return fallback;
        }
    }

    private static float readFloat(JsonObject json, String key, float fallback) {
        if (!json.has(key)) return fallback;
        try {
            float v = json.get(key).getAsFloat();
            if (v <= 0f) {
                LOGGER.warn("[Diet] '{}' must be > 0 — using default {}.", key, fallback);
                return fallback;
            }
            return v;
        } catch (Exception e) {
            LOGGER.warn("[Diet] Could not parse '{}' — using default {}.", key, fallback);
            return fallback;
        }
    }
}