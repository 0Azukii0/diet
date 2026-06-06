package com.azukii.diet.data;

import com.azukii.diet.profile.FoodProfile;
import com.azukii.diet.profile.FoodProfileCache;
import com.azukii.diet.system.FoodSystemSettings;
import com.azukii.diet.system.RecipeNutrientAnalyzer;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Central registry for diet profiles and configuration, backed by datapacks.
 */
public final class FoodRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(FoodRegistry.class);

    private static volatile DatapackConfiguration config = DatapackConfiguration.DEFAULT;

    // Fast cache per item stack (identity). Auto-clears when stack GC'ed.
    private static final Cache<Identifier, FoodProfile> PROFILE_CACHE =
            CacheBuilder.newBuilder()
                    .maximumSize(512)
                    .expireAfterAccess(10, TimeUnit.MINUTES)
                    .build();

    // Persistent cache for pre-calculated profiles
    private static volatile FoodProfileCache persistentCache = null;

    // Server reference for recipe access
    private static volatile MinecraftServer currentServer = null;

    private FoodRegistry() {}

    public static void setServer(MinecraftServer server) {
        currentServer = server;
        RecipeNutrientAnalyzer.setServer(server);
        if (server != null) {
            PROFILE_CACHE.invalidateAll();
            RecipeNutrientAnalyzer.clearCache();

            // Initialize persistent cache
            initializePersistentCache(server);
        } else {
            // Server shutting down, save cache
            if (persistentCache != null) {
                persistentCache.save();
                persistentCache = null;
            }
        }
    }

    private static void initializePersistentCache(MinecraftServer server) {
        try {
            java.nio.file.Path worldDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            persistentCache = new FoodProfileCache(worldDir);
            persistentCache.load();

            // Start background calculation if needed
            if (persistentCache.needsRecalculation()) {
                LOGGER.info("[DIET] Starting background calculation of diet profiles...");
                persistentCache.calculateAsync(server, config.settings());
            } else {
                LOGGER.info("[DIET] Using cached diet profiles");
            }
        } catch (Exception e) {
            LOGGER.error("[DIET] Failed to initialize persistent cache", e);
            persistentCache = null;
        }
    }

    public static void loadConfig(DatapackConfiguration newConfig) {
        config = newConfig == null ? DatapackConfiguration.DEFAULT : newConfig;
        PROFILE_CACHE.invalidateAll();
        RecipeNutrientAnalyzer.clearCache();
        LOGGER.info("[DIET] Diet config loaded with {} explicit item entries", config.items().size());
        LOGGER.info("[DIET] Current settings - decay_interval: {}s, saturation_scale: {}",
                config.settings().decayIntervalSeconds(), config.settings().saturationScale());
        LOGGER.info("[DIET] Decay rates - grain: {}, protein: {}, vegetable: {}, fruit: {}, sugar: {}",
                config.decayRates().get(FoodCategories.GRAIN), config.decayRates().get(FoodCategories.PROTEIN),
                config.decayRates().get(FoodCategories.VEGETABLE), config.decayRates().get(FoodCategories.FRUIT),
                config.decayRates().get(FoodCategories.SUGAR));
    }

    public static void reset() {
        config = DatapackConfiguration.DEFAULT;
        PROFILE_CACHE.invalidateAll();
        if (persistentCache != null) {
            persistentCache.clear();
        }
    }

    public static FoodProfile getProfile(ItemStack stack) {
        if (stack.isEmpty()) {
            return FoodProfile.EMPTY;
        }
        Item item = stack.getItem();
        Identifier id = BuiltInRegistries.ITEM.getKey(item);

        // Check memory cache first (fastest)
        FoodProfile cached = PROFILE_CACHE.getIfPresent(id);
        if (cached != null) {
            return cached;
        }

        // Check explicit config entries (highest priority)
        FoodProfile profile = config.items().get(id);
        if (profile != null) {
            PROFILE_CACHE.put(id, profile);
            return profile;
        }

        // Check persistent cache (pre-calculated)
        if (persistentCache != null) {
            profile = persistentCache.getProfile(id);
            if (profile != null) {
                PROFILE_CACHE.put(id, profile);
                return profile;
            }
        }

        // Fallback: calculate on-demand (only if not in persistent cache)
        profile = deriveProfile(item);
        PROFILE_CACHE.put(id, profile);
        return profile;
    }

    public static FoodProfile getDecayRates() {
        return config.decayRates();
    }

    public static FoodProfileCache getPersistentCache() {
        return persistentCache;
    }

    public static FoodProfile getMaxValues() {
        return config.maxValues();
    }

    public static FoodSystemSettings getSettings() {
        return config.settings();
    }

    private static FoodProfile deriveProfile(Item item) {
        ItemStack sampleStack = item.getDefaultInstance();
        if (sampleStack.isEmpty()) {
            sampleStack = new ItemStack(item);
        }
        FoodProperties food = sampleStack.get(DataComponents.FOOD);
        if (food == null) {
            return FoodProfile.EMPTY;
        }

        FoodSystemSettings settings = config.settings();

        // Use new crafting-first analysis system
        // This will recursively analyze recipes and only use heuristics for base components
        return RecipeNutrientAnalyzer.analyze(item, settings);
    }

}