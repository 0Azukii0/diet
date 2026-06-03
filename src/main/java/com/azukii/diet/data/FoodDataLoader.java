package com.azukii.diet.data;

import com.azukii.diet.system.FoodSystemSettings;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.fml.loading.FMLPaths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class FoodDataLoader extends SimpleJsonResourceReloadListener<JsonElement> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoodDataLoader.class);
    private static final Codec<JsonElement> JSON_ELEMENT_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(),
            json -> new Dynamic<>(JsonOps.INSTANCE, json)
    );
    private static final String DIRECTORY = "diet_config";


    public FoodDataLoader() {
        super(JSON_ELEMENT_CODEC, FileToIdConverter.json(DIRECTORY));
    }

    @Override
    protected void apply(Map<Identifier, JsonElement> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        LOGGER.info("Loading diet configuration...");
        LOGGER.debug("Found {} diet config files", data.size());
        FoodRegistry.reset();

        // Always generate default config as a reference for users
        generateDefaultConfig();

        if (data.isEmpty()) {
            LOGGER.warn("No diet_config data found; using defaults.");
            FoodRegistry.loadConfig(DatapackConfiguration.DEFAULT);
            return;
        }

        DatapackConfiguration selectedConfig = null;
        Identifier selectedId = null;

        for (Map.Entry<Identifier, JsonElement> entry : data.entrySet()) {
            try {
                DatapackConfiguration packConfig = DatapackConfiguration.CODEC
                        .parse(JsonOps.INSTANCE, entry.getValue())
                        .resultOrPartial(error -> LOGGER.error("Failed to parse diet config {}: {}", entry.getKey(), error))
                        .orElse(null);

                if (packConfig != null) {
                    selectedConfig = packConfig;
                    selectedId = entry.getKey();
                }
            } catch (Exception ex) {
                LOGGER.error("Error loading diet config {}: {}", entry.getKey(), ex.getMessage(), ex);
            }
        }

        if (selectedConfig != null) {
            FoodRegistry.loadConfig(selectedConfig);
            LOGGER.info("Loaded diet config from {}", selectedId);
        } else {
            LOGGER.warn("All diet configs failed to load; falling back to defaults.");
            FoodRegistry.loadConfig(DatapackConfiguration.DEFAULT);
        }
    }

    /**
     * Generates a default config file in the config directory if one doesn't exist.
     * This provides users with a template they can customize.
     */
    private void generateDefaultConfig() {
        LOGGER.info("[DIET CONFIG] Attempting to generate default config file...");

        try {
            // Try FMLPaths first, fall back to working directory
            Path configDir = FMLPaths.CONFIGDIR.get();
            Path configFile = configDir.resolve("example_diet_config.json");

            LOGGER.info("[DIET CONFIG] Config directory: {}", configDir.toAbsolutePath());
            LOGGER.info("[DIET CONFIG] Config file path: {}", configFile.toAbsolutePath());

            if (Files.exists(configFile)) {
                LOGGER.info("[DIET CONFIG] Default config already exists at {}", configFile);
                return;
            }

            Files.createDirectories(configDir);
            LOGGER.info("[DIET CONFIG] Created config directory");

            // Manually build JSON to ensure all default values are included for reference
            String jsonContent = buildDefaultConfigJson();
            Files.writeString(configFile, jsonContent);
            LOGGER.info("[DIET CONFIG] Generated default diet config at: {}", configFile.toAbsolutePath());
            LOGGER.info("[DIET CONFIG] To use custom settings, create a datapack with diet_config JSON files.");
        } catch (Exception e) {
            LOGGER.error("[DIET CONFIG] Failed to generate default config file: {}", e.getMessage(), e);
        }
    }

    /**
     * Builds the default config JSON manually to ensure all values are included.
     * The codec's optionalFieldOf skips default values, so we build it manually.
     */
    private String buildDefaultConfigJson() {
        DatapackConfiguration config = DatapackConfiguration.DEFAULT;
        FoodSystemSettings settings = config.settings();

        return """
                {
                  "_comment": "This is a reference config file. To customize, create a datapack with diet_config JSON files.",
                  "_datapack_location": "data/<namespace>/diet_config/<name>.json",
                  "items": {
                    "minecraft:bread": {
                      "grain": 3.0,
                      "protein": 0.0,
                      "vegetable": 0.0,
                      "fruit": 0.0,
                      "sugar": 0.0
                    },
                    "minecraft:cooked_beef": {
                      "grain": 0.0,
                      "protein": 8.0,
                      "vegetable": 0.0,
                      "fruit": 0.0,
                      "sugar": 0.0
                    },
                    "minecraft:apple": {
                      "grain": 0.0,
                      "protein": 0.0,
                      "vegetable": 0.0,
                      "fruit": 4.0,
                      "sugar": 1.0
                    },
                    "minecraft:carrot": {
                      "grain": 0.0,
                      "protein": 0.0,
                      "vegetable": 3.0,
                      "fruit": 0.0,
                      "sugar": 0.5
                    }
                  },
                  "decay_rates": {
                    "grain": %s,
                    "protein": %s,
                    "vegetable": %s,
                    "fruit": %s,
                    "sugar": %s
                  },
                  "max_values": {
                    "grain": %s,
                    "protein": %s,
                    "vegetable": %s,
                    "fruit": %s,
                    "sugar": %s
                  },
                  "settings": {
                    "decay_interval_seconds": %s,
                    "heuristics": {
                      "saturation_scale": %s,
                      "fast_food_saturation_threshold": %s,
                      "grain_nutrition_multiplier": %s,
                      "fast_food_grain_bonus": %s,
                      "protein_meat_multiplier": %s,
                      "protein_base_multiplier": %s,
                      "vegetable_hint_multiplier": %s,
                      "vegetable_base_multiplier": %s,
                      "fruit_hint_multiplier": %s,
                      "fruit_base_multiplier": %s,
                      "sugar_base_multiplier": %s,
                      "fast_sugar_flat_bonus": %s,
                      "fast_sugar_saturation_multiplier": %s
                    }
                  }
                }
                """.formatted(
                // decay_rates
                config.decayRates().get(FoodCategories.GRAIN),
                config.decayRates().get(FoodCategories.PROTEIN),
                config.decayRates().get(FoodCategories.VEGETABLE),
                config.decayRates().get(FoodCategories.FRUIT),
                config.decayRates().get(FoodCategories.SUGAR),
                // max_values
                config.maxValues().get(FoodCategories.GRAIN),
                config.maxValues().get(FoodCategories.PROTEIN),
                config.maxValues().get(FoodCategories.VEGETABLE),
                config.maxValues().get(FoodCategories.FRUIT),
                config.maxValues().get(FoodCategories.SUGAR),
                // settings
                settings.decayIntervalSeconds(),
                settings.saturationScale(),
                settings.fastFoodSaturationThreshold(),
                settings.grainNutritionMultiplier(),
                settings.fastFoodGrainBonus(),
                settings.proteinMeatMultiplier(),
                settings.proteinBaseMultiplier(),
                settings.vegetableHintMultiplier(),
                settings.vegetableBaseMultiplier(),
                settings.fruitHintMultiplier(),
                settings.fruitBaseMultiplier(),
                settings.sugarBaseMultiplier(),
                settings.fastSugarFlatBonus(),
                settings.fastSugarSaturationMultiplier()
        );
    }
}
