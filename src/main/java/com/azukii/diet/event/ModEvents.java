package com.azukii.diet.event;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.core.food.ModFoodData;
import com.azukii.diet.data.FoodDataLoader;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.resource.ListenerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@EventBusSubscriber(modid = DietMod.MODID)
public class ModEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModEvents.class);

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        FoodRegistry.rebuildCache();
    }

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addRetainedListener(
                ListenerKey.create(Identifier.fromNamespaceAndPath(DietMod.MODID, "diet_config")),
                new FoodDataLoader()
        );
        event.addRetainedListener(
                ListenerKey.create(Identifier.fromNamespaceAndPath(DietMod.MODID, "diet_foods")),
                (sharedState, executor, preparationBarrier, executor1) ->
                        preparationBarrier.wait(null).thenRun(FoodRegistry::rebuildCache)
        );
    }

    @SubscribeEvent
    public static void onFoodConsumed(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer) || entity.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        if (stack.isEmpty() || stack.get(DataComponents.FOOD) == null) return;

        FoodItemProfile profile = FoodRegistry.getProfile(stack);
        if (profile == null || profile.isEmpty()) return;

        ModFoodData data = serverPlayer.getData(ModAttachments.FOOD_DATA);
        data.add(profile);
        data.setLastDecayTimeMs(System.currentTimeMillis());
        serverPlayer.setData(ModAttachments.FOOD_DATA, data);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ModFoodData data = serverPlayer.getData(ModAttachments.FOOD_DATA);

        if (!data.isInitialized()) {
            data.ensureInitialized(ModFoodData.DEFAULT_START_PERCENT);
            data.setLastDecayTimeMs(System.currentTimeMillis());
            serverPlayer.setData(ModAttachments.FOOD_DATA, data);
            return;
        }

        long decayIntervalMs = FoodRegistry.getConfig().intervalSeconds() * 1000L;
        long now = System.currentTimeMillis();
        long timeSinceDecay = now - data.getLastDecayTimeMs();
        if (timeSinceDecay < decayIntervalMs) return;

        float intervalUnits = (timeSinceDecay / 1000.0f) / Math.max(1.0f, decayIntervalMs / 1000.0f);
        boolean changed = data.applyDecay(intervalUnits);
        data.setLastDecayTimeMs(now);

        if (changed) {
            serverPlayer.setData(ModAttachments.FOOD_DATA, data);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ModFoodData data = serverPlayer.getData(ModAttachments.FOOD_DATA);
        if (!data.isInitialized()) {
            data.ensureInitialized(ModFoodData.DEFAULT_START_PERCENT);
        }
        data.setLastDecayTimeMs(System.currentTimeMillis());
        serverPlayer.setData(ModAttachments.FOOD_DATA, data);
    }
}
