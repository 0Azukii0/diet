package com.azukii.diet.data;

import com.azukii.diet.profile.FoodProfile;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class ModFoodData implements ValueIOSerializable {
    public static final float DEFAULT_START_PERCENT = 0.8f;

    private final float[] values = new float[FoodCategories.COUNT];
    private long lastDecayTimeMs = System.currentTimeMillis();
    private boolean dirty = false;
    private boolean initialized = false;

    public ModFoodData() {
        preloadDefaults();
    }

    public float get(FoodCategories category) {
        return values[category.ordinal()];
    }

    public float[] copyValues() {
        float[] copy = new float[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        return copy;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public long getLastDecayTimeMs() {
        return lastDecayTimeMs;
    }

    /**
     * Adds the nutritional values from a {@link FoodProfile} to this data,
     * capped at the current {@link FoodRegistry#getMaxValue()}.
     */
    public void add(FoodProfile profile) {
        float max = FoodRegistry.getMaxValue();
        for (FoodCategories category : FoodCategories.VALUES) {
            float delta = profile.get(category);
            if (delta <= 0.0f) continue;
            int idx = category.ordinal();
            float newValue = Math.min(values[idx] + delta, max);
            if (newValue != values[idx]) {
                values[idx] = newValue;
                markDirty();
            }
        }
    }

    /**
     * Decays all categories by their configured rate over {@code deltaSeconds}.
     * Rates are read from {@link FoodRegistry#getConfig()} so they always reflect
     * the current datapack without needing to pass them in.
     *
     * @return {@code true} if any value changed.
     */
    public boolean applyDecay(float deltaSeconds) {
        if (deltaSeconds <= 0.0f) return false;

        boolean changed = false;
        for (FoodCategories category : FoodCategories.VALUES) {
            int idx = category.ordinal();
            float value = values[idx];
            if (value <= 0.0f) continue;

            float decayPerSecond = FoodRegistry.getConfig().decayRate(category);
            if (decayPerSecond <= 0.0f) continue;

            float newValue = Math.max(0.0f, value - decayPerSecond * deltaSeconds);
            if (newValue != value) {
                values[idx] = newValue;
                changed = true;
            }
        }
        if (changed) markDirty();
        return changed;
    }

    /**
     * Initializes all bars to {@code percent} × maxValue if not already initialized.
     * Called on first login to avoid players starting with empty bars.
     */
    public void ensureInitialized(float percent) {
        if (initialized) return;
        preload(percent);
        initialized = true;
        dirty = true;
    }

    public void setAll(float[] newValues) {
        if (newValues.length != values.length) {
            throw new IllegalArgumentException("Expected " + values.length + " values, got " + newValues.length);
        }
        boolean changed = false;
        for (int i = 0; i < values.length; i++) {
            float clamped = Math.max(0.0f, newValues[i]);
            if (values[i] != clamped) {
                values[i] = clamped;
                changed = true;
            }
        }
        if (changed) markDirty();
    }

    public void setLastDecayTimeMs(long timestamp) {
        this.lastDecayTimeMs = timestamp;
    }

    public void markDirty() {
        this.dirty = true;
        this.initialized = true;
    }

    public boolean consumeDirty() {
        if (dirty) {
            dirty = false;
            return true;
        }
        return false;
    }

    @Override
    public void serialize(ValueOutput output) {
        for (FoodCategories category : FoodCategories.VALUES) {
            output.putFloat(category.name().toLowerCase(), values[category.ordinal()]);
        }
        output.putLong("lastDecayTimeMs", lastDecayTimeMs);
        output.putBoolean("initialized", initialized);
    }

    @Override
    public void deserialize(ValueInput input) {
        for (FoodCategories category : FoodCategories.VALUES) {
            values[category.ordinal()] = input.getFloatOr(category.name().toLowerCase(), 0f);
        }
        // If the key is absent (fresh save), use current time to avoid instant decay burst.
        lastDecayTimeMs = input.keySet().contains("lastDecayTimeMs")
                ? input.getLongOr("lastDecayTimeMs", 0L)
                : System.currentTimeMillis();
        initialized = input.getBooleanOr("initialized", false);
    }

    /**
     * Called from the constructor — FoodRegistry may not be loaded yet, uses the default constant.
     */
    private void preloadDefaults() {
        float max = FoodRegistry.getMaxValue();
        float target = max * DEFAULT_START_PERCENT;
        for (FoodCategories category : FoodCategories.VALUES) {
            values[category.ordinal()] = target;
        }
        this.initialized = false;
        this.dirty = false;
    }

    /**
     * Called from {@link #ensureInitialized} once the registry is available.
     */
    private void preload(float percent) {
        float clampedPercent = Math.clamp(percent, 0.0f, 1.0f);
        float max = FoodRegistry.getMaxValue();
        float target = max * clampedPercent;
        for (FoodCategories category : FoodCategories.VALUES) {
            values[category.ordinal()] = target;
        }
    }
}