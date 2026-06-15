package com.azukii.diet.event;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.component.ModComponents;
import com.azukii.diet.component.ThirstComponent;
import com.azukii.diet.core.DecayData;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.core.food.ModFoodData;
import com.azukii.diet.core.thirst.ThirstData;
import com.azukii.diet.data.DatapackConfiguration;
import com.azukii.diet.data.FoodDataLoader;
import com.azukii.diet.data.ModItemTagsProvider;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.AddServerReloadListenersEvent;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.resource.ListenerKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@EventBusSubscriber(modid = DietMod.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        FoodRegistry.rebuildCache();
    }



    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        event.modify(Items.POTION, (components, context, item) -> components.set(ModComponents.THIRST.get(), new ThirstComponent(1)));
        event.modify(Items.SUGAR, (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            components.set(DataComponents.FOOD, new FoodProperties(1,1.0f, true));
        });
        event.modify(Items.WHEAT, (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            components.set(DataComponents.FOOD, new FoodProperties(2,1.0f, false));
        });
        event.modify(Items.MILK_BUCKET, (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            components.set(DataComponents.FOOD, new FoodProperties(5,2.0f, false));
        });
        event.modify(Items.SEA_PICKLE, (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            components.set(DataComponents.FOOD, new FoodProperties(1,1.0f, false));
        });
        event.modify(Items.COCOA_BEANS, (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            components.set(DataComponents.FOOD, new FoodProperties(1,3.0f, false));
        });

        //TODO : tags not working
        event.modifyMatching((item, components) -> BuiltInRegistries.ITEM.wrapAsHolder(item).is(Tags.Items.SEEDS), (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            components.set(DataComponents.FOOD, new FoodProperties(1,1.0f, false));
        });
        event.modifyMatching((item, components) -> BuiltInRegistries.ITEM.wrapAsHolder(item).is(Tags.Items.EGGS), (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.POISONOUS_POTATO);
            components.set(DataComponents.FOOD, Foods.POISONOUS_POTATO);
        });
        event.modifyMatching((item, components) -> BuiltInRegistries.ITEM.wrapAsHolder(item).is(Tags.Items.MUSHROOMS), (components, context, item) -> {
            components.set(DataComponents.CONSUMABLE, Consumables.DEFAULT_FOOD);
            components.set(DataComponents.FOOD, new FoodProperties(2,1.0f, false));
        });
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
    public static void onDrinkConsumed(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof ServerPlayer serverPlayer) || entity.level().isClientSide()) return;

        ItemStack stack = event.getItem();
        if (stack.isEmpty() || stack.get(DataComponents.POTION_CONTENTS) == null) return;

        ThirstData thirstData = serverPlayer.getData(ModAttachments.THIRST_DATA);
        thirstData.addThirst(DatapackConfiguration.DEFAULT_MAX_VALUES * 0.25f);
        thirstData.setLastDecayTimeMs(System.currentTimeMillis());
        serverPlayer.setData(ModAttachments.THIRST_DATA, thirstData);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;

        ModFoodData foodData = serverPlayer.getData(ModAttachments.FOOD_DATA);
        if (foodData.isNotInitialized()) foodData.ensureInitialized(ModFoodData.DEFAULT_START_PERCENT);
        foodData.setLastDecayTimeMs(System.currentTimeMillis());
        serverPlayer.setData(ModAttachments.FOOD_DATA, foodData);

        ThirstData thirstData = serverPlayer.getData(ModAttachments.THIRST_DATA);
        if (thirstData.isNotInitialized()) thirstData.ensureInitialized(ThirstData.DEFAULT_START_PERCENT);
        thirstData.setLastDecayTimeMs(System.currentTimeMillis());
        serverPlayer.setData(ModAttachments.THIRST_DATA, thirstData);
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
        ModFoodData foodData = serverPlayer.getData(ModAttachments.FOOD_DATA);
        ThirstData thirstData = serverPlayer.getData(ModAttachments.THIRST_DATA);
        tickDecay(foodData, ModAttachments.FOOD_DATA.get(), serverPlayer);
        tickDecay(thirstData, ModAttachments.THIRST_DATA.get(), serverPlayer);
    }

    private static <T extends DecayData> void tickDecay(T data, AttachmentType<T> type, ServerPlayer serverPlayer) {
        if (data.isNotInitialized()) {
            data.ensureInitialized(DecayData.DEFAULT_START_PERCENT);
            data.setLastDecayTimeMs(System.currentTimeMillis());
            serverPlayer.setData(type, data);
            return;
        }

        if (applyDecay(data, FoodRegistry.getConfig().intervalSeconds())) {
            serverPlayer.setData(type, data);
        }
    }

    private static <T extends DecayData> boolean applyDecay(T data, int intervalSeconds) {
        long decayIntervalMs = intervalSeconds * 1000L;
        long now = System.currentTimeMillis();
        long timeSinceDecay = now - data.getLastDecayTimeMs();
        if (timeSinceDecay < decayIntervalMs) return false;

        float intervalUnits = (timeSinceDecay / 1000.0f) / Math.max(1.0f, intervalSeconds);
        boolean changed = data.applyDecay(intervalUnits);
        data.setLastDecayTimeMs(now);
        return changed;
    }
}
