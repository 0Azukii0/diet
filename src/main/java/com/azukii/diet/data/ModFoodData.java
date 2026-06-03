package com.azukii.diet.data;

import com.azukii.diet.profile.FoodProfile;
import net.minecraft.nbt.CompoundTag;
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
        preloadDefaults(DEFAULT_START_PERCENT);
    }

    public float get(FoodCategories category) {
        return values[category.ordinal()];
    }

    public void add(FoodProfile profile, FoodProfile maxValues) {
        for (FoodCategories category : FoodCategories.VALUES) {
            float delta = profile.get(category);
            if (delta <= 0.0f) continue;
            int idx = category.ordinal();
            float max = maxValues.get(category);
            float newValue = clamp(values[idx] + delta, max);
            if (newValue != values[idx]) {
                values[idx] = newValue;
                markDirty();
            }
        }
    }

    public boolean applyDecay(FoodProfile decayRates, float deltaSeconds) {
        if (deltaSeconds <= 0.0f) {
            return false;
        }

        boolean changed = false;
        for (FoodCategories category : FoodCategories.VALUES) {
            int idx = category.ordinal();
            float value = values[idx];
            if (value <= 0.0f) continue;

            float decayPerSecond = decayRates.get(category);
            if (decayPerSecond <= 0.0f) continue;

            float newValue = Math.max(0.0f, value - (decayPerSecond * deltaSeconds));
            if (newValue != value) {
                changed = true;
                values[idx] = newValue;
            }
        }
        if (changed) {
            markDirty();
        }
        return changed;
    }

    public void ensureInitialized(FoodProfile maxValues, float percent) {
        if (initialized) {
            return;
        }
        preload(maxValues, percent);
        initialized = true;
        dirty = true;
    }

    public float[] copyValues() {
        float[] copy = new float[values.length];
        System.arraycopy(values, 0, copy, 0, values.length);
        return copy;
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
        if (changed) {
            markDirty();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    public long getLastDecayTimeMs() {
        return lastDecayTimeMs;
    }

    public void setLastDecayTimeMs(long timestamp) {
        this.lastDecayTimeMs = timestamp;
    }

    private float clamp(float value, float max) {
        if (max <= 0.0f) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(value, max));
    }

    public void markDirty() {
        this.dirty = true;
        this.initialized = true;
    }

    private void preloadDefaults(float percent) {
        preload(FoodRegistry.getMaxValues(), percent);
        this.initialized = false;
        this.dirty = false;
    }

    private void preload(FoodProfile maxValues, float percent) {
        float clampedPercent = Math.clamp(percent, 0.0f, 1.0f);
        for (FoodCategories category : FoodCategories.VALUES) {
            int idx = category.ordinal();
            float max = maxValues.get(category);
            values[idx] = clamp(max * clampedPercent, max);
        }
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
        CompoundTag tag = new CompoundTag();
        for (FoodCategories category : FoodCategories.VALUES) {
            tag.putFloat(category.name().toLowerCase(), values[category.ordinal()]);
        }
        tag.putLong("lastDecayTimeMs", lastDecayTimeMs);
        tag.putBoolean("initialized", initialized);
    }

    @Override
    public void deserialize(ValueInput input) {
        for (FoodCategories category : FoodCategories.VALUES) {
            String key = category.name().toLowerCase();
            if (input.keySet().contains(key)) {
                values[category.ordinal()] = input.getFloatOr(key, 0);
            }
        }
        lastDecayTimeMs = input.keySet().contains("lastDecayTimeMs")
                ? input.getLongOr("lastDecayTimeMs", 0) : System.currentTimeMillis();
        initialized = input.getBooleanOr("initialized", false);
    }
}
