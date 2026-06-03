package com.azukii.diet.system;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

/**
 * Tunable values that govern automatic diet behavior.
 */
public record FoodSystemSettings(
        float decayIntervalSeconds,
        HeuristicSettings heuristics
) {
    public FoodSystemSettings {
        heuristics = heuristics == null ? HeuristicSettings.DEFAULT : heuristics;
    }

    public static final FoodSystemSettings DEFAULT = new FoodSystemSettings(
            120.0f,
            HeuristicSettings.DEFAULT
    );

    public static final Codec<FoodSystemSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("decay_interval_seconds", DEFAULT.decayIntervalSeconds()).forGetter(FoodSystemSettings::decayIntervalSeconds),
            HeuristicSettings.HEURISTIC_CODEC.forGetter(FoodSystemSettings::heuristics)
    ).apply(instance, FoodSystemSettings::new));

    public long decayIntervalMillis() {
        float clamped = Math.max(0.05f, decayIntervalSeconds);
        return (long) (clamped * 1000.0f);
    }

    public float saturationScale() {
        return heuristics.saturationScale();
    }

    public float fastFoodSaturationThreshold() {
        return heuristics.fastFoodSaturationThreshold();
    }

    public float grainNutritionMultiplier() {
        return heuristics.grainNutritionMultiplier();
    }

    public float fastFoodGrainBonus() {
        return heuristics.fastFoodGrainBonus();
    }

    public float proteinMeatMultiplier() {
        return heuristics.proteinMeatMultiplier();
    }

    public float proteinBaseMultiplier() {
        return heuristics.proteinBaseMultiplier();
    }

    public float vegetableHintMultiplier() {
        return heuristics.vegetableHintMultiplier();
    }

    public float vegetableBaseMultiplier() {
        return heuristics.vegetableBaseMultiplier();
    }

    public float fruitHintMultiplier() {
        return heuristics.fruitHintMultiplier();
    }

    public float fruitBaseMultiplier() {
        return heuristics.fruitBaseMultiplier();
    }

    public float sugarBaseMultiplier() {
        return heuristics.sugarBaseMultiplier();
    }

    public float fastSugarFlatBonus() {
        return heuristics.fastSugarFlatBonus();
    }

    public float fastSugarSaturationMultiplier() {
        return heuristics.fastSugarSaturationMultiplier();
    }

    public record HeuristicSettings(
            float saturationScale,
            float fastFoodSaturationThreshold,
            float grainNutritionMultiplier,
            float fastFoodGrainBonus,
            float proteinMeatMultiplier,
            float proteinBaseMultiplier,
            float vegetableHintMultiplier,
            float vegetableBaseMultiplier,
            float fruitHintMultiplier,
            float fruitBaseMultiplier,
            float sugarBaseMultiplier,
            float fastSugarFlatBonus,
            float fastSugarSaturationMultiplier
    ) {
        private static final HeuristicSettings DEFAULT = new HeuristicSettings(
                2.0f,   // multiplier applied to vanilla saturation when deriving stats
                2.0f,   // saturation value that distinguishes “fast food” items (norma food became "fast" when saturation is < of this value)
                0.4f,   // grain gained per point of nutrition
                1.5f,   // flat grain bonus added for fast foods
                0.6f,   // protein gained per nutrition from meat
                0.2f,   // protein gained per nutrition from non-meat
                1.5f,   // vegetable gain multiplier when the item hints veggies
                0.3f,   // vegetable gain multiplier when there is no veggie hint
                0.4f,   // fruit gain multiplier when the item hints fruit
                0.2f,   // fruit gain multiplier when there is no fruit hint
                0.4f,   // sugar gained per point of saturation on normal foods
                2.0f,   // flat sugar injection for fast foods
                1.0f    // extra sugar from saturation contributed by fast foods
        );

        private static final MapCodec<HeuristicSettings> HEURISTIC_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.FLOAT.optionalFieldOf("saturation_scale", DEFAULT.saturationScale()).forGetter(HeuristicSettings::saturationScale),
                Codec.FLOAT.optionalFieldOf("fast_food_saturation_threshold", DEFAULT.fastFoodSaturationThreshold()).forGetter(HeuristicSettings::fastFoodSaturationThreshold),
                Codec.FLOAT.optionalFieldOf("grain_nutrition_multiplier", DEFAULT.grainNutritionMultiplier()).forGetter(HeuristicSettings::grainNutritionMultiplier),
                Codec.FLOAT.optionalFieldOf("fast_food_grain_bonus", DEFAULT.fastFoodGrainBonus()).forGetter(HeuristicSettings::fastFoodGrainBonus),
                Codec.FLOAT.optionalFieldOf("protein_meat_multiplier", DEFAULT.proteinMeatMultiplier()).forGetter(HeuristicSettings::proteinMeatMultiplier),
                Codec.FLOAT.optionalFieldOf("protein_base_multiplier", DEFAULT.proteinBaseMultiplier()).forGetter(HeuristicSettings::proteinBaseMultiplier),
                Codec.FLOAT.optionalFieldOf("vegetable_hint_multiplier", DEFAULT.vegetableHintMultiplier()).forGetter(HeuristicSettings::vegetableHintMultiplier),
                Codec.FLOAT.optionalFieldOf("vegetable_base_multiplier", DEFAULT.vegetableBaseMultiplier()).forGetter(HeuristicSettings::vegetableBaseMultiplier),
                Codec.FLOAT.optionalFieldOf("fruit_hint_multiplier", DEFAULT.fruitHintMultiplier()).forGetter(HeuristicSettings::fruitHintMultiplier),
                Codec.FLOAT.optionalFieldOf("fruit_base_multiplier", DEFAULT.fruitBaseMultiplier()).forGetter(HeuristicSettings::fruitBaseMultiplier),
                Codec.FLOAT.optionalFieldOf("sugar_base_multiplier", DEFAULT.sugarBaseMultiplier()).forGetter(HeuristicSettings::sugarBaseMultiplier),
                Codec.FLOAT.optionalFieldOf("fast_sugar_flat_bonus", DEFAULT.fastSugarFlatBonus()).forGetter(HeuristicSettings::fastSugarFlatBonus),
                Codec.FLOAT.optionalFieldOf("fast_sugar_saturation_multiplier", DEFAULT.fastSugarSaturationMultiplier()).forGetter(HeuristicSettings::fastSugarSaturationMultiplier)
        ).apply(instance, HeuristicSettings::new));
    }
}
