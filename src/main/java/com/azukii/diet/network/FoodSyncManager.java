package com.azukii.diet.network;

import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.data.ModFoodData;
import com.azukii.diet.profile.FoodProfile;
import com.azukii.diet.profile.FoodProfileCache;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

/**
 * Handles diet gain from food consumption and periodic decay.
 */
public class FoodSyncManager {
    private static final float START_PERCENT = 0.8f;

    public static void syncDietProfilesToClient(ServerPlayer player) {
        // Get all cached profiles from server
        FoodProfileCache serverCache = FoodRegistry.getPersistentCache();
        if (serverCache != null) {
            Map<Identifier, FoodProfile> profiles = serverCache.getAllProfiles();
            if (!profiles.isEmpty()) {
                PacketDistributor.sendToPlayer(player, new FoodProfileSyncPacket(profiles));
            }
        }
    }

    public static void syncIfNeeded(ServerPlayer player, ModFoodData data, boolean force) {
        boolean dirty = force || data.consumeDirty();
        if (!dirty) {
            return;
        }

        float[] values = data.copyValues();
        PacketDistributor.sendToPlayer(player, new FoodSyncPacket(values));
    }

    public static void initializeIfNeeded(ServerPlayer player, ModFoodData data) {
        if (data.isInitialized()) {
            return;
        }
        data.ensureInitialized(FoodRegistry.getMaxValues(), START_PERCENT);
        data.setLastDecayTimeMs(System.currentTimeMillis());
        syncIfNeeded(player, data, true);
    }
}
