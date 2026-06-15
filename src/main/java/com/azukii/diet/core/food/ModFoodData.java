package com.azukii.diet.core.food;

import com.azukii.diet.core.DecayData;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public class ModFoodData implements ValueIOSerializable, DecayData {
    public static final int FOOD_CATEGORIES_MAX_LEVEL = 4;
    public static final int FOOD_CATEGORIES_MIN_LEVEL = 1;

    private final float[] values = new float[FoodCategories.COUNT];
    private final int[] levels = defaultLevels();
    private long lastDecayTimeMs = System.currentTimeMillis();
    private boolean initialized = false;

    private static int[] defaultLevels() {
        int[] l = new int[FoodCategories.COUNT];
        Arrays.fill(l, FOOD_CATEGORIES_MIN_LEVEL);
        return l;
    }

    public float getValue(FoodCategories category) {
        return values[category.ordinal()];
    }

    public int getLevel(FoodCategories category) {
        return levels[category.ordinal()];
    }

    public float[] copyValues() {
        return Arrays.copyOf(values, values.length);
    }

    public int[] copyLevels() {
        return Arrays.copyOf(levels, levels.length);
    }

    public long getLastDecayTimeMs() {
        return lastDecayTimeMs;
    }

    public boolean isNotInitialized() {
        return !initialized;
    }

    /**
     * Adds the nutritional values from a {@link FoodItemProfile},
     * capped at {@link FoodRegistry#getMaxValue()}.
     */
    public void add(FoodItemProfile profile) {
        float max = FoodRegistry.getMaxValue();
        for (FoodCategories category : FoodCategories.VALUES) {
            float delta = profile.get(category);
            if (delta <= 0.0f) continue;
            int idx = category.ordinal();
            float newValue = Math.min(values[idx] + delta, max);
            if (newValue != values[idx]) {
                values[idx] = newValue;
            }
        }
    }

    /**
     * Decays all categories by their configured rate over {@code deltaSeconds}.
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

                if (newValue == 0.0f && levels[idx] > FOOD_CATEGORIES_MIN_LEVEL) {
                    levels[idx]--;
                }
            }
        }
        return changed;
    }

    public void addLevel(FoodCategories category) {
        int idx = category.ordinal();
        if (levels[idx] < FOOD_CATEGORIES_MAX_LEVEL) {
            levels[idx]++;
        }
    }

    public void setLastDecayTimeMs(long timestamp) {
        this.lastDecayTimeMs = timestamp;
    }

    /**
     * Initializes all bars to {@code percent} × maxValue on first login.
     */
    public void ensureInitialized(float percent) {
        if (initialized) return;
        float target = FoodRegistry.getMaxValue() * Math.clamp(percent, 0.0f, 1.0f);
        Arrays.fill(values, target);
        Arrays.fill(levels, FOOD_CATEGORIES_MIN_LEVEL);
        initialized = true;
    }

    public void applyNetworkUpdate(float[] newValues, int[] newLevels) {
        for (int i = 0; i < values.length; i++) {
            values[i] = Math.max(0.0f, newValues[i]);
        }
        if (newLevels.length == levels.length) {
            for (int i = 0; i < levels.length; i++) {
                levels[i] = Math.max(FOOD_CATEGORIES_MIN_LEVEL, newLevels[i]);
            }
        }
    }

    @Override
    public void serialize(ValueOutput output) {
        for (FoodCategories category : FoodCategories.VALUES) {
            output.putFloat(category.name().toLowerCase(), values[category.ordinal()]);
        }
        output.putLong("lastDecayTimeMs", lastDecayTimeMs);
        output.putIntArray("levels", levels);
        output.putBoolean("initialized", initialized);
    }

    @Override
    public void deserialize(ValueInput input) {
        for (FoodCategories category : FoodCategories.VALUES) {
            values[category.ordinal()] = input.getFloatOr(category.name().toLowerCase(), 0f);
        }
        lastDecayTimeMs = input.keySet().contains("lastDecayTimeMs")
                ? input.getLongOr("lastDecayTimeMs", 0L)
                : System.currentTimeMillis();
        int[] saved = input.getIntArray("levels").orElse(defaultLevels());
        for (int i = 0; i < levels.length; i++) {
            levels[i] = Math.max(FOOD_CATEGORIES_MIN_LEVEL, i < saved.length ? saved[i] : FOOD_CATEGORIES_MIN_LEVEL);
        }
        initialized = input.getBooleanOr("initialized", false);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ModFoodData> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NonNull ModFoodData decode(RegistryFriendlyByteBuf buf) {
                    ModFoodData data = new ModFoodData();
                    int len = buf.readVarInt();
                    float[] values = new float[len];
                    for (int i = 0; i < len; i++) values[i] = buf.readFloat();

                    int levelsLen = buf.readVarInt();
                    int[] levels = new int[levelsLen];
                    for (int i = 0; i < levelsLen; i++) levels[i] = buf.readVarInt();

                    data.applyNetworkUpdate(values, levels);
                    return data;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ModFoodData data) {
                    float[] values = data.copyValues();
                    buf.writeVarInt(values.length);
                    for (float v : values) buf.writeFloat(v);

                    int[] levels = data.copyLevels();
                    buf.writeVarInt(levels.length);
                    for (int l : levels) buf.writeVarInt(l);
                }
            };
}