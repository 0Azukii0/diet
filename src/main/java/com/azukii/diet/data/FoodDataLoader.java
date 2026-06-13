package com.azukii.diet.data;

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
import java.util.Locale;
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
        LOGGER.info("[Diet] Loading diet configuration...");
        FoodRegistry.reset();
        generateDefaultConfig();

        if (data.isEmpty()) {
            LOGGER.warn("[Diet] No diet_config data found — using defaults.");
            FoodRegistry.reset(DatapackConfiguration.defaults());
            return;
        }

        DatapackConfiguration selectedConfig = null;
        Identifier selectedId = null;

        for (Map.Entry<Identifier, JsonElement> entry : data.entrySet()) {
            JsonElement element = entry.getValue();
            if (!element.isJsonObject()) {
                LOGGER.error("[Diet] Config file {} is not a JSON object — skipped.", entry.getKey());
                continue;
            }
            try {
                DatapackConfiguration config = DatapackConfiguration.fromJson(element.getAsJsonObject());
                selectedConfig = config;
                selectedId = entry.getKey();
            } catch (Exception ex) {
                LOGGER.error("[Diet] Unexpected error loading config {} — skipped: {}", entry.getKey(), ex.getMessage(), ex);
            }
        }

        if (selectedConfig != null) {
            FoodRegistry.reset(selectedConfig);
            LOGGER.info("[Diet] Loaded diet config from {}.", selectedId);
        } else {
            LOGGER.warn("[Diet] All diet config files failed to load — falling back to defaults.");
            FoodRegistry.reset(DatapackConfiguration.defaults());
        }
    }

    /**
     * Writes an example config to the Minecraft config folder on first run.
     * This file is NOT loaded by the mod — it's a reference template for datapack authors.
     * The real config lives in data/<namespace>/diet_config/<name>.json.
     */
    private void generateDefaultConfig() {
        try {
            Path configFile = FMLPaths.CONFIGDIR.get().resolve("diet_example.json");
            if (Files.exists(configFile)) return;

            Files.createDirectories(configFile.getParent());
            Files.writeString(configFile, buildExampleJson());
            LOGGER.info("[Diet] Wrote example config to {}.", configFile.toAbsolutePath());
            LOGGER.info("[Diet] To customise, create a datapack at data/<namespace>/diet_config/<name>.json");
        } catch (Exception e) {
            LOGGER.warn("[Diet] Could not write example config: {}", e.getMessage());
        }
    }

    private String buildExampleJson() {
        DatapackConfiguration configuration = DatapackConfiguration.defaults();
        return String.format(Locale.ROOT, """
            {
              "_comment": "Diet mod — example config. Copy to data/<namespace>/diet_config/<n>.json in a datapack.",

              "interval_seconds":    %d,
              "max_values":          %.1f,
              "tag_value_multiplier": %.1f,

              "decay_rates": {
                "grain":     %.2f,
                "protein":   %.2f,
                "vegetable": %.2f,
                "fruit":     %.2f,
                "sugar":     %.2f
              },

              "_items_comment": "Only needed for items with multiple categories or tag overrides.",
              "items": {
                "minecraft:apple":        { "fruit": 4.0, "sugar": 1.0 },
                "minecraft:carrot":       { "vegetable": 3.0, "sugar": 0.5 },
                "minecraft:cake":         { "grain": 2.0, "sugar": 3.0 },
                "minecraft:honey_bottle": { "sugar": 3.0 },
                "minecraft:golden_apple": { "fruit": 4.0, "sugar": 2.0 }
              }
            }
            """,
                configuration.intervalSeconds(),
                configuration.maxValues(),
                configuration.tagValueMultiplier(),
                configuration.decayRate(FoodCategories.GRAIN),
                configuration.decayRate(FoodCategories.PROTEIN),
                configuration.decayRate(FoodCategories.VEGETABLE),
                configuration.decayRate(FoodCategories.FRUIT),
                configuration.decayRate(FoodCategories.SUGAR)
        );
    }
}