package com.azukii.diet.registry;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.*;
import net.minecraft.world.level.Level;

import java.util.List;

public class ModConsumables {
    private static final float LEISURELY_CONSUME = 2.5F;
    public static final Consumable LEISURELY_FOOD = defaultFood(LEISURELY_CONSUME).build();
    public static final Consumable LEISURELY_DRINK = defaultDrink(LEISURELY_CONSUME).build();
    public static final Consumable ENCHANTED_GOLDEN_APPLE = defaultFood(LEISURELY_CONSUME, new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.REGENERATION, 400, 1), new MobEffectInstance(MobEffects.RESISTANCE, 6000), new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000), new MobEffectInstance(MobEffects.ABSORPTION, 2400, 3)))).build();
    private static final float UNHURRIED_CONSUME = 2.0F;
    public static final Consumable UNHURRIED_FOOD = defaultFood(UNHURRIED_CONSUME).build();
    public static final Consumable UNHURRIED_DRINK = defaultDrink(UNHURRIED_CONSUME).build();
    public static final Consumable HONEY_BOTTLE = defaultDrink(UNHURRIED_CONSUME, new RemoveStatusEffectsConsumeEffect(MobEffects.POISON)).sound(SoundEvents.HONEY_DRINK).build();
    public static final Consumable OMINOUS_BOTTLE = defaultDrink(UNHURRIED_CONSUME).soundAfterConsume(SoundEvents.OMINOUS_BOTTLE_DISPOSE).build();
    public static final Consumable GOLDEN_APPLE = defaultFood(UNHURRIED_CONSUME, new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.REGENERATION, 100, 1), new MobEffectInstance(MobEffects.ABSORPTION, 2400)))).build();
    public static final Consumable MILK = defaultFood(UNHURRIED_CONSUME, ClearAllStatusEffectsConsumeEffect.INSTANCE).build();
    public static final Consumable CHORUS_FRUIT = defaultFood(UNHURRIED_CONSUME, new TeleportRandomlyConsumeEffect()).build();
    private static final float NORMAL_CONSUME = 1.6F;
    public static final Consumable NORMAL_FOOD = defaultFood(NORMAL_CONSUME).build();
    public static final Consumable NORMAL_DRINK = defaultDrink(NORMAL_CONSUME).build();
    public static final Consumable RAW_MEAT = defaultFood(NORMAL_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.3F)).build();
    public static final Consumable RAW_FISH = defaultFood(NORMAL_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 400), 0.1F)).build();
    public static final Consumable POISONOUS_POTATO = defaultFood(NORMAL_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 100), 0.6F)).build();
    public static final Consumable PUFFERFISH = defaultFood(NORMAL_CONSUME, new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.POISON, 1200, 1), new MobEffectInstance(MobEffects.HUNGER, 300, 2), new MobEffectInstance(MobEffects.NAUSEA, 300)))).build();
    private static final float HASTY_CONSUME = 1.2F;
    public static final Consumable HASTY_FOOD = defaultFood(HASTY_CONSUME).build();
    public static final Consumable HASTY_DRINK = defaultDrink(HASTY_CONSUME).build();
    public static final Consumable ROTTEN_FLESH = defaultFood(HASTY_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)).build();
    public static final Consumable SPIDER_EYE = defaultFood(HASTY_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 100))).build();
    public static final Consumable GLOW_BERRIES = defaultFood(HASTY_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.GLOWING, 4000))).build();
    public static final Consumable GLISTERING_MELON_SLICE = defaultFood(HASTY_CONSUME, new ConsumeEffect() {
        @Override
        public Type<? extends ConsumeEffect> getType() {
            return Type.APPLY_EFFECTS;
        }

        @Override
        public boolean apply(Level level, ItemStack itemStack, LivingEntity livingEntity) {
            if (livingEntity.getHealth() < 20.0F) livingEntity.heal(1.0F);
            return true;
        }
    }).build();
    public static final Consumable EGG = defaultFood(HASTY_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.HUNGER, 600), 0.8F)).build();
    public static final Consumable GOLDEN_CARROT = defaultFood(HASTY_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 600))).build();
    private static final float RUSHED_CONSUME = 0.8F;
    public static final Consumable RUSHED_FOOD = defaultFood(RUSHED_CONSUME).build();
    public static final Consumable SUGAR = defaultFood(RUSHED_CONSUME, new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.SPEED, 100), 0.8F)).build();
    public static final Consumable RUSHED_DRINK = defaultDrink(RUSHED_CONSUME).build();

    public static Consumable.Builder defaultFood(float consumeSeconds, ConsumeEffect consumeEffects) {
        return Consumable.builder().consumeSeconds(consumeSeconds).animation(ItemUseAnimation.EAT).sound(SoundEvents.GENERIC_EAT).onConsume(consumeEffects).hasConsumeParticles(true);
    }

    public static Consumable.Builder defaultFood(float consumeSeconds) {
        return Consumable.builder().consumeSeconds(consumeSeconds).animation(ItemUseAnimation.EAT).sound(SoundEvents.GENERIC_EAT).hasConsumeParticles(true);
    }

    public static Consumable.Builder defaultDrink(float consumeSeconds, ConsumeEffect consumeEffects) {
        return Consumable.builder().consumeSeconds(consumeSeconds).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK).onConsume(consumeEffects).hasConsumeParticles(false);
    }

    public static Consumable.Builder defaultDrink(float consumeSeconds) {
        return Consumable.builder().consumeSeconds(consumeSeconds).animation(ItemUseAnimation.DRINK).sound(SoundEvents.GENERIC_DRINK).hasConsumeParticles(false);
    }
}
