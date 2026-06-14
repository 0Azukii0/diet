package com.azukii.diet.core;

public interface DecayData {
    float DEFAULT_START_PERCENT = 0.8f;
    boolean isNotInitialized();
    long getLastDecayTimeMs();
    void setLastDecayTimeMs(long timestamp);
    boolean applyDecay(float deltaSeconds);
    void ensureInitialized(float percent);
}
