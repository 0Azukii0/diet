package com.azukii.diet.event;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.FoodDataLoader;
import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.data.ModFoodData;
import com.azukii.diet.data.PlayerActivityData;
import com.azukii.diet.network.FoodSyncManager;
import com.azukii.diet.network.FoodSyncPacket;
import com.azukii.diet.network.PlayerActivitySyncPacket;
import com.azukii.diet.profile.FoodProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.neoforged.neoforge.resource.ListenerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@EventBusSubscriber(modid = DietMod.MODID)
public class ModEvents {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModEvents.class);

    @SubscribeEvent
    public static void onAddReloadListener(AddServerReloadListenersEvent event) {
        event.addRetainedListener(ListenerKey.create(Identifier.fromNamespaceAndPath(DietMod.MODID, "diet_config")), new FoodDataLoader());
        event.addRetainedListener(ListenerKey.create(Identifier.fromNamespaceAndPath(DietMod.MODID, "diet_foods")),
                (sharedState, executor, preparationBarrier, executor1) -> preparationBarrier.wait(null).thenRun(FoodRegistry::rebuildCache));
        LOGGER.info("Diet data loader registered");
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");

        registrar.playToClient(
                FoodSyncPacket.TYPE,
                FoodSyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() ->
                        FoodSyncPacket.handle(packet, context.player())
                )
        );

        registrar.playToClient(
                PlayerActivitySyncPacket.TYPE,
                PlayerActivitySyncPacket.STREAM_CODEC,
                (packet, context) -> context.enqueueWork(() ->
                        PlayerActivitySyncPacket.handle(packet, context.player())
                )
        );

        DietMod.LOGGER.info("Diet network packets registered");
    }

    @SubscribeEvent
    public static void onFoodConsumed(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Player player) || entity.level().isClientSide()) {
            return;
        }

        ItemStack stack = event.getItem();
        LOGGER.debug("[DIET] Item use finished: {}", stack.getItem());

        if (stack.isEmpty()) {
            return;
        }

        FoodProfile profile;
        if (stack.get(DataComponents.FOOD) == null) {
            return;
        }
        profile = FoodRegistry.getProfile(stack);

        if (profile == null || profile.isEmpty()) {
            return;
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;
        ModFoodData data = serverPlayer.getData(ModAttachments.FOOD_DATA);
        FoodSyncManager.initializeIfNeeded(serverPlayer, data);
        data.add(profile);
        data.setLastDecayTimeMs(System.currentTimeMillis());
        FoodSyncManager.syncIfNeeded(serverPlayer, data, true);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ModFoodData data = player.getData(ModAttachments.FOOD_DATA);
        FoodSyncManager.initializeIfNeeded(serverPlayer, data);
        //FoodEffectsManager.apply(serverPlayer, data);

        long decayIntervalMs = FoodRegistry.getConfig().intervalSeconds() * 1000L;
        long now = System.currentTimeMillis();
        long lastDecay = data.getLastDecayTimeMs();
        long timeSinceDecay = now - lastDecay;

        if (timeSinceDecay < decayIntervalMs) {
            return;
        }

        float elapsedSeconds = timeSinceDecay / 1000.0f;
        float intervalSeconds = Math.max(1.0f, decayIntervalMs / 1000.0f);
        float intervalUnits = elapsedSeconds / intervalSeconds;
        boolean changed = data.applyDecay(intervalUnits);
        data.setLastDecayTimeMs(now);

        if (changed) {
            FoodSyncManager.syncIfNeeded(serverPlayer, data, false);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        ModFoodData data = serverPlayer.getData(ModAttachments.FOOD_DATA);
        FoodSyncManager.initializeIfNeeded(serverPlayer, data);

        // Reset decay timer to current time to prevent decay during offline time
        data.setLastDecayTimeMs(System.currentTimeMillis());

        //FoodEffectsManager.apply(serverPlayer, data);
        FoodSyncManager.syncIfNeeded(serverPlayer, data, true);

        // Sync for last food eaten HUD
        PlayerActivityData activityData = serverPlayer.getData(ModAttachments.PLAYER_ACTIVITY);
        PacketDistributor.sendToPlayer(serverPlayer, new PlayerActivitySyncPacket(activityData.getLastFood()));
    }
}
