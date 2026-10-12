package com.azukii.diet.registry;

import com.azukii.diet.DietMod;
import com.azukii.diet.client.ClientboundSetFoodSaturationPacket;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = DietMod.MODID)
public class ModEvents {
    @SubscribeEvent
    public static void setPlayerActivity(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        FoodData foodData = player.getFoodData();
        if (foodData instanceof FoodDataMixinAccess access) {
            if (player.isSwimming()) access.diet$setCurrentActivity(FoodDataCategories.Activity.SWIM);
            if (player.isSprinting()) access.diet$setCurrentActivity(FoodDataCategories.Activity.SPRINT);
            if (player.isJumping()) access.diet$setCurrentActivity(FoodDataCategories.Activity.JUMP);
            if (player.hasEffect(MobEffects.HUNGER)) access.diet$setCurrentActivity(FoodDataCategories.Activity.EFFECT);
            if (player.walkAnimation.isMoving()) access.diet$setCurrentActivity(FoodDataCategories.Activity.WALK);
            if (player.onClimbable() && player.getDeltaMovement().y != 0)
                access.diet$setCurrentActivity(FoodDataCategories.Activity.CLIMB);
        }
    }

    @SubscribeEvent
    public static void setHurtingActivity(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player)) return;
        FoodData foodData = player.getFoodData();
        if (foodData instanceof FoodDataMixinAccess access) {
            if (event.getSource().is(DamageTypes.FALL)) {
                access.diet$setCurrentActivity(FoodDataCategories.Activity.FALL);
            } else if (event.getSource().is(DamageTypes.DROWN)) {
                access.diet$setCurrentActivity(FoodDataCategories.Activity.DROWN);
            } else if (event.getSource().is(DamageTypes.ON_FIRE) && event.getSource().is(DamageTypes.IN_FIRE)) {
                access.diet$setCurrentActivity(FoodDataCategories.Activity.FIRE);
            } else access.diet$setCurrentActivity(FoodDataCategories.Activity.HURT);
        }
    }

    @SubscribeEvent
    public static void setMiningActivity(BreakBlockEvent event) {
        Player player = event.getPlayer();
        FoodData foodData = player.getFoodData();
        if (foodData instanceof FoodDataMixinAccess access)
            access.diet$setCurrentActivity(FoodDataCategories.Activity.MINE);
    }

    @SubscribeEvent
    public static void setAttackingActivity(AttackEntityEvent event) {
        Player player = event.getEntity();
        FoodData foodData = player.getFoodData();
        if (foodData instanceof FoodDataMixinAccess access)
            access.diet$setCurrentActivity(FoodDataCategories.Activity.ATTACK);
    }

    @SubscribeEvent
    public static void setHealingActivity(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        FoodData foodData = player.getFoodData();
        if (foodData instanceof FoodDataMixinAccess access)
            access.diet$setCurrentActivity(FoodDataCategories.Activity.HEAL);
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.getFoodData() instanceof FoodDataMixinAccess access) access.diet$syncToClient(player);
    }

    @SubscribeEvent
    public static void onEat(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!(player.getFoodData() instanceof FoodDataMixinAccess access)) return;
        if (event.getItem().has(DataComponents.FOOD)) access.diet$setLastFoodEatenServer(event.getItem());
    }

    @SubscribeEvent
    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ClientboundSetFoodSaturationPacket.TYPE,
                ClientboundSetFoodSaturationPacket.STREAM_CODEC
        );
    }

    @SubscribeEvent
    public static void modifyComponents(ModifyDefaultComponentsEvent event) {
        modifyConsumableComponent(event, Items.APPLE, ModConsumables.HASTY_FOOD, ModFoods.APPLE);
        modifyConsumableComponent(event, Items.GOLDEN_APPLE, ModConsumables.GOLDEN_APPLE, ModFoods.GOLDEN_APPLE);
        modifyConsumableComponent(event, Items.ENCHANTED_GOLDEN_APPLE, ModConsumables.ENCHANTED_GOLDEN_APPLE, ModFoods.ENCHANTED_GOLDEN_APPLE);
        modifyConsumableComponent(event, Items.MELON_SLICE, ModConsumables.HASTY_FOOD, ModFoods.MELON_SLICE);
        modifyConsumableComponent(event, Items.GLISTERING_MELON_SLICE, ModConsumables.GLISTERING_MELON_SLICE, ModFoods.GLISTERING_MELON_SLICE);
        modifyConsumableComponent(event, Items.CHORUS_FRUIT, ModConsumables.CHORUS_FRUIT, ModFoods.CHORUS_FRUIT);
        modifyConsumableComponent(event, Items.SWEET_BERRIES, ModConsumables.HASTY_FOOD, ModFoods.SWEET_BERRIES);
        modifyConsumableComponent(event, Items.GLOW_BERRIES, ModConsumables.GLOW_BERRIES, ModFoods.GLOW_BERRIES);
        modifyConsumableComponent(event, Items.WHEAT, ModConsumables.HASTY_FOOD, ModFoods.WHEAT);
        modifyConsumableComponent(event, Items.BREAD, ModConsumables.UNHURRIED_FOOD, ModFoods.BREAD);
        modifyConsumableComponent(event, Items.COOKIE, ModConsumables.HASTY_FOOD, ModFoods.COOKIE);
        modifyConsumableComponent(event, Items.PUMPKIN_PIE, ModConsumables.LEISURELY_FOOD, ModFoods.PUMPKIN_PIE);
        modifyConsumableComponent(event, Items.BEEF, ModConsumables.RAW_MEAT, ModFoods.BEEF);
        modifyConsumableComponent(event, Items.CHICKEN, ModConsumables.RAW_MEAT, ModFoods.CHICKEN);
        modifyConsumableComponent(event, Items.MUTTON, ModConsumables.RAW_MEAT, ModFoods.MUTTON);
        modifyConsumableComponent(event, Items.PORKCHOP, ModConsumables.RAW_MEAT, ModFoods.PORKCHOP);
        modifyConsumableComponent(event, Items.RABBIT, ModConsumables.RAW_MEAT, ModFoods.RABBIT);
        modifyConsumableComponent(event, Items.COOKED_BEEF, ModConsumables.NORMAL_FOOD, ModFoods.COOKED_BEEF);
        modifyConsumableComponent(event, Items.COOKED_CHICKEN, ModConsumables.NORMAL_FOOD, ModFoods.COOKED_CHICKEN);
        modifyConsumableComponent(event, Items.COOKED_MUTTON, ModConsumables.NORMAL_FOOD, ModFoods.COOKED_MUTTON);
        modifyConsumableComponent(event, Items.COOKED_PORKCHOP, ModConsumables.NORMAL_FOOD, ModFoods.COOKED_PORKCHOP);
        modifyConsumableComponent(event, Items.COOKED_RABBIT, ModConsumables.NORMAL_FOOD, ModFoods.COOKED_RABBIT);
        modifyConsumableComponent(event, Items.PUFFERFISH, ModConsumables.PUFFERFISH, ModFoods.PUFFERFISH);
        modifyConsumableComponent(event, Items.TROPICAL_FISH, ModConsumables.HASTY_FOOD, ModFoods.TROPICAL_FISH);
        modifyConsumableComponent(event, Items.COD, ModConsumables.RAW_FISH, ModFoods.COD);
        modifyConsumableComponent(event, Items.SALMON, ModConsumables.RAW_FISH, ModFoods.SALMON);
        modifyConsumableComponent(event, Items.COOKED_COD, ModConsumables.NORMAL_FOOD, ModFoods.COOKED_COD);
        modifyConsumableComponent(event, Items.COOKED_SALMON, ModConsumables.NORMAL_FOOD, ModFoods.COOKED_SALMON);
        modifyConsumableComponent(event, Items.RABBIT_STEW, ModConsumables.LEISURELY_FOOD, ModFoods.RABBIT_STEW);
        modifyConsumableComponent(event, Items.MILK_BUCKET, ModConsumables.MILK, ModFoods.MILK);
        modifyConsumableComponent(event, Items.ROTTEN_FLESH, ModConsumables.ROTTEN_FLESH, ModFoods.ROTTEN_FLESH);
        modifyConsumableComponent(event, Items.SPIDER_EYE, ModConsumables.SPIDER_EYE, ModFoods.SPIDER_EYE);
        modifyConsumableComponent(event, Items.FERMENTED_SPIDER_EYE, ModConsumables.UNHURRIED_FOOD, ModFoods.FERMENTED_SPIDER_EYE);
        modifyConsumableComponent(event, Items.EGG, ModConsumables.EGG, ModFoods.EGG);
        modifyConsumableComponent(event, Items.POTATO, ModConsumables.HASTY_FOOD, ModFoods.POTATO);
        modifyConsumableComponent(event, Items.POISONOUS_POTATO, ModConsumables.POISONOUS_POTATO, ModFoods.POISONOUS_POTATO);
        modifyConsumableComponent(event, Items.BAKED_POTATO, ModConsumables.NORMAL_FOOD, ModFoods.BAKED_POTATO);
        modifyConsumableComponent(event, Items.BEETROOT, ModConsumables.HASTY_FOOD, ModFoods.BEETROOT);
        modifyConsumableComponent(event, Items.BEETROOT_SOUP, ModConsumables.LEISURELY_DRINK, ModFoods.BEETROOT_SOUP);
        modifyConsumableComponent(event, Items.CARROT, ModConsumables.HASTY_FOOD, ModFoods.CARROT);
        modifyConsumableComponent(event, Items.GOLDEN_CARROT, ModConsumables.GOLDEN_CARROT, ModFoods.GOLDEN_CARROT);
        modifyConsumableComponent(event, Items.DRIED_KELP, ModConsumables.HASTY_FOOD, ModFoods.DRIED_KELP);
        modifyConsumableComponent(event, Items.BROWN_MUSHROOM, ModConsumables.RUSHED_FOOD, ModFoods.BROWN_MUSHROOM);
        modifyConsumableComponent(event, Items.RED_MUSHROOM, ModConsumables.RUSHED_FOOD, ModFoods.RED_MUSHROOM);
        modifyConsumableComponent(event, Items.MUSHROOM_STEW, ModConsumables.LEISURELY_DRINK, ModFoods.MUSHROOM_STEW);
        modifyConsumableComponent(event, Items.SEA_PICKLE, ModConsumables.RUSHED_FOOD, ModFoods.SEA_PICKLE);
        modifyConsumableComponent(event, Items.SUSPICIOUS_STEW, ModConsumables.LEISURELY_DRINK, ModFoods.SUSPICIOUS_STEW);
        modifyConsumableComponent(event, Items.COCOA_BEANS, ModConsumables.RUSHED_FOOD, ModFoods.COCOA_BEANS);
        modifyConsumableComponent(event, Items.HONEY_BOTTLE, ModConsumables.HONEY_BOTTLE, ModFoods.HONEY_BOTTLE);
        modifyConsumableComponent(event, Items.SUGAR, ModConsumables.SUGAR, ModFoods.SUGAR);
        event.modify(Items.OMINOUS_BOTTLE, (components, _, _) -> components.set(DataComponents.CONSUMABLE, ModConsumables.OMINOUS_BOTTLE));
    }

    private static void modifyConsumableComponent(ModifyDefaultComponentsEvent event, Item item, Consumable consumable, FoodProperties foodProperties) {
        event.modify(item, (components, _, _) -> {
            components.set(DataComponents.CONSUMABLE, consumable);
            components.set(DataComponents.FOOD, foodProperties);
        });
    }
}
