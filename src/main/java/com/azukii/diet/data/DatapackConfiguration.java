package com.azukii.diet.data;

import com.azukii.diet.system.FoodSystemSettings;
import com.azukii.diet.profile.FoodProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.Identifier;

import java.util.Collections;
import java.util.Map;

/**
 * Represents the data-driven configuration for the diet system loaded from datapacks.
 */
public record DatapackConfiguration(Map<Identifier, FoodProfile> items, FoodProfile decayRates, FoodProfile maxValues, FoodSystemSettings settings) {
    private static final FoodProfile DEFAULT_DECAY = FoodProfile.of(
            0.15f, // grain
            0.2f,  // protein
            0.25f, // vegetable
            0.25f, // fruit
            0.3f // sugar
    );

    private static final FoodProfile DEFAULT_MAX = FoodProfile.of(
            100.0f, 100.0f, 100.0f, 100.0f, 100.0f
    );

    public static final DatapackConfiguration DEFAULT = new DatapackConfiguration(
            Collections.emptyMap(),
            DEFAULT_DECAY,
            DEFAULT_MAX,
            FoodSystemSettings.DEFAULT
    );

    public static final Codec<DatapackConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Identifier.CODEC, FoodProfile.CODEC)
                    .optionalFieldOf("items", Collections.emptyMap())
                    .forGetter(DatapackConfiguration::items),
            FoodProfile.CODEC.optionalFieldOf("decay_rates", DEFAULT_DECAY)
                    .forGetter(DatapackConfiguration::decayRates),
            FoodProfile.CODEC.optionalFieldOf("max_values", DEFAULT_MAX)
                    .forGetter(DatapackConfiguration::maxValues),
            FoodSystemSettings.CODEC.optionalFieldOf("settings", FoodSystemSettings.DEFAULT)
                    .forGetter(DatapackConfiguration::settings)
    ).apply(instance, DatapackConfiguration::new));
}