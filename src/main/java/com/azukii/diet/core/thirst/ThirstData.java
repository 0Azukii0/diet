package com.azukii.diet.core.thirst;

import com.azukii.diet.core.DecayData;
import com.azukii.diet.data.DatapackConfiguration;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.NonNull;

public class ThirstData implements ValueIOSerializable, DecayData {
    public static final int THIRST_MAX_LEVEL = 4;

    private float thirstValue = 0.0f;
    private int thirstLevel = 0;
    private long lastDecayTimeMs = System.currentTimeMillis();
    private boolean initialized = false;

    public float getThirstValue() {
        return thirstValue;
    }

    public int getThirstLevel() {
        return thirstLevel;
    }

    public long getLastDecayTimeMs() {
        return lastDecayTimeMs;
    }

    public boolean isNotInitialized() {
        return !initialized;
    }

    public void addThirstLevel() {
        if (thirstLevel < THIRST_MAX_LEVEL) thirstLevel++;
    }

    public void addThirst(float amount) {
        float max = DatapackConfiguration.DEFAULT_MAX_VALUES;
        thirstValue = Math.min(thirstValue + amount, max);
    }

    /**
     * Decays thirst value by the configured rate over {@code deltaSeconds}.
     *
     * @return {@code true} if the value changed.
     */
    public boolean applyDecay(float deltaSeconds) {
        if (deltaSeconds <= 0.0f) return false;

        float value = thirstValue;
        if (value <= 0.0f) return false;

        float newValue = Math.max(0.0f, value - DatapackConfiguration.DEFAULT_DECAY_RATE * deltaSeconds);
        if (newValue == value) return false;

        thirstValue = newValue;

        if (newValue == 0.0f && thirstLevel > 1) {
            thirstLevel--;
        }

        return true;
    }

    public void ensureInitialized(float percent) {
        if (initialized) return;
        thirstValue = DatapackConfiguration.DEFAULT_MAX_VALUES * Math.clamp(percent, 0.0f, 1.0f);
        initialized = true;
    }

    public void setLastDecayTimeMs(long timestamp) {
        this.lastDecayTimeMs = timestamp;
    }

    public void applyNetworkUpdate(float thirstValue, int thirstLevel) {
        this.thirstValue = Math.max(0.0f, thirstValue);
        this.thirstLevel = thirstLevel;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putFloat("thirstValue", thirstValue);
        output.putInt("thirstLevel", thirstLevel);
        output.putLong("lastDecayTimeMs", lastDecayTimeMs);
        output.putBoolean("initialized", initialized);
    }

    @Override
    public void deserialize(ValueInput input) {
        thirstValue = input.getFloatOr("thirstValue", 0f);
        thirstLevel = input.getIntOr("thirstLevel", 0);
        lastDecayTimeMs = input.keySet().contains("lastDecayTimeMs")
                ? input.getLongOr("lastDecayTimeMs", 0L)
                : System.currentTimeMillis();
        initialized = input.getBooleanOr("initialized", false);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, ThirstData> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NonNull ThirstData decode(RegistryFriendlyByteBuf buf) {
                    ThirstData data = new ThirstData();
                    data.applyNetworkUpdate(buf.readFloat(), buf.readVarInt());
                    return data;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, ThirstData data) {
                    buf.writeFloat(data.thirstValue);
                    buf.writeVarInt(data.thirstLevel);
                }
            };
}
