package com.azukii.diet.effect;

import com.azukii.diet.DietMod;
import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.data.ModFoodData;
import com.azukii.diet.profile.FoodProfile;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class FoodEffectsManager {
    private static final float LOW_THRESHOLD = 0.20f;
    private static final float HIGH_THRESHOLD = 0.80f;
    private static final int EFFECT_DURATION = 220;

    private static final Identifier PROTEIN_DEBUFF_HEALTH = id("protein_debuff_health");
    private static final Identifier PROTEIN_BUFF_HEALTH = id("protein_buff_health");
    private static final Identifier FRUIT_DEBUFF_SPEED = id("fruit_debuff_speed");
    private static final Identifier FRUIT_BUFF_SPEED = id("fruit_buff_speed");
    private static final Identifier SUGAR_DEBUFF_ATTACK = id("sugar_debuff_attack");

    private FoodEffectsManager() {}

    public static void apply(ServerPlayer player, ModFoodData data) {
        FoodProfile maxValues = FoodRegistry.getMaxValues();
        if (maxValues == null) {
            return;
        }

        applyGrainEffects(player, data, maxValues);
        applyProteinEffects(player, data, maxValues);
        applyVegetableEffects(player, data, maxValues);
        applyFruitEffects(player, data, maxValues);
        applySugarEffects(player, data, maxValues);
    }

    private static void applyGrainEffects(ServerPlayer player, ModFoodData data, FoodProfile maxValues) {
        float ratio = ratio(data, maxValues, FoodCategories.GRAIN);
        boolean low = ratio < LOW_THRESHOLD;
        boolean high = ratio > HIGH_THRESHOLD;

        setMobEffect(player, MobEffects.SLOWNESS, 0, low);
        setMobEffect(player, MobEffects.MINING_FATIGUE, 0, low);
        setMobEffect(player, MobEffects.SPEED, 0, high);
    }

    private static void applyProteinEffects(ServerPlayer player, ModFoodData data, FoodProfile maxValues) {
        float ratio = ratio(data, maxValues, FoodCategories.PROTEIN);
        boolean low = ratio < LOW_THRESHOLD;
        boolean high = ratio > HIGH_THRESHOLD;

        setMobEffect(player, MobEffects.WEAKNESS, 0, low);
        setAttributeModifier(player, Attributes.MAX_HEALTH, PROTEIN_DEBUFF_HEALTH, "diet_protein_deficit", -4.0d, AttributeModifier.Operation.ADD_VALUE, low);
        setAttributeModifier(player, Attributes.MAX_HEALTH, PROTEIN_BUFF_HEALTH,"diet_protein_optimal", 2.0d, AttributeModifier.Operation.ADD_VALUE, high && !low);
    }

    private static void applyVegetableEffects(ServerPlayer player, ModFoodData data, FoodProfile maxValues) {
        float ratio = ratio(data, maxValues, FoodCategories.VEGETABLE);
        boolean low = ratio < LOW_THRESHOLD;
        boolean high = ratio > HIGH_THRESHOLD;

        setMobEffect(player, MobEffects.RESISTANCE, 0, high);
        if (low && player.hasEffect(MobEffects.RESISTANCE)) {
            player.removeEffect(MobEffects.RESISTANCE);
        }
        if (high && player.hasEffect(MobEffects.POISON)) {
            player.removeEffect(MobEffects.POISON);
        }
    }

    private static void applyFruitEffects(ServerPlayer player, ModFoodData data, FoodProfile maxValues) {
        float ratio = ratio(data, maxValues, FoodCategories.FRUIT);
        boolean low = ratio < LOW_THRESHOLD;
        boolean high = ratio > HIGH_THRESHOLD;

        setAttributeModifier(player, Attributes.MOVEMENT_SPEED, FRUIT_DEBUFF_SPEED,
                "diet_fruit_deficit", -0.10d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, low);
        setAttributeModifier(player, Attributes.MOVEMENT_SPEED, FRUIT_BUFF_SPEED,
                "diet_fruit_optimal", 0.10d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, high && !low);
    }

    private static void applySugarEffects(ServerPlayer player, ModFoodData data, FoodProfile maxValues) {
        float ratio = ratio(data, maxValues, FoodCategories.SUGAR);
        boolean low = ratio < LOW_THRESHOLD;
        boolean high = ratio > HIGH_THRESHOLD;

        setAttributeModifier(player, Attributes.ATTACK_SPEED, SUGAR_DEBUFF_ATTACK,
                "diet_sugar_deficit", -0.15d, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL, low);
        setMobEffect(player, MobEffects.HASTE   , 0, high && !low);
    }

    private static float ratio(ModFoodData data, FoodProfile maxValues, FoodCategories category) {
        float max = maxValues.get(category);
        if (max <= 0.0f) {
            return 0.0f;
        }
        float value = data.get(category);
        return Math.clamp(value / max, 0.0f, 1.0f);
    }

    private static void setMobEffect(Player player, Holder<MobEffect> effect, int amplifier, boolean enable) {
        if (enable) {
            player.addEffect(new MobEffectInstance(effect, EFFECT_DURATION, amplifier, true, false, true));
        } else if (player.hasEffect(effect)) {
            player.removeEffect(effect);
        }
    }

    private static void setAttributeModifier(Player player, Holder<Attribute> attribute, Identifier id, String name, double amount, AttributeModifier.Operation operation, boolean enable) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        AttributeModifier existing = instance.getModifier(id);
        if (!enable) {
            if (existing != null) {
                instance.removeModifier(existing);
            }
            return;
        }
        if (existing != null && (existing.amount() != amount || existing.operation() != operation)) {
            instance.removeModifier(existing);
            existing = null;
        }
        if (existing == null) {
            instance.addTransientModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(DietMod.MODID, "diet/" + path);
    }
}
