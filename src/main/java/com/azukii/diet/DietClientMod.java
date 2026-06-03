package com.azukii.diet;

import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.profile.ClientFoodProfileCache;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = DietMod.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = DietMod.MODID, value = Dist.CLIENT)
public class DietClientMod {
    private static ClientFoodProfileCache clientDietCache = null;

    public DietClientMod(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Register block entity renderers
        // Initialize client diet profile cache
        event.enqueueWork(DietClientMod::initializeClientDietCache);
        DietMod.LOGGER.info("Diet client setup complete");
    }

    private static void initializeClientDietCache() {
        try {
            clientDietCache = new ClientFoodProfileCache();
            clientDietCache.load();

            // Start background calculation if needed
            if (clientDietCache.needsRecalculation()) {
                DietMod.LOGGER.info("[DIET CLIENT] Starting background calculation of diet profiles...");
                clientDietCache.calculateAsync(FoodRegistry.getSettings());
            } else {
                DietMod.LOGGER.info("[DIET CLIENT] Using cached diet profiles");
            }
        } catch (Exception e) {
            DietMod.LOGGER.error("[DIET CLIENT] Failed to initialize client diet cache", e);
            clientDietCache = null;
        }
    }

    public static ClientFoodProfileCache getClientDietCache() {
        return clientDietCache;
    }
}
