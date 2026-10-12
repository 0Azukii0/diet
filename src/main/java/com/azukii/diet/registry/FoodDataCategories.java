package com.azukii.diet.registry;

import com.azukii.diet.client.data.ModItemTagsProvider;
import com.mojang.serialization.Codec;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;

public enum FoodDataCategories implements StringRepresentable {
    FRUIT("fruit", "tooltip.diet.category.fruit", Items.APPLE, 0xFFD11C52, ModItemTagsProvider.FRUITS),
    GRAIN("grain", "tooltip.diet.category.grain", Items.WHEAT, 0xFFD1A11C, ModItemTagsProvider.GRAINS),
    PROTEIN("protein", "tooltip.diet.category.protein", Items.COOKED_BEEF, 0xFFD1771C, ModItemTagsProvider.PROTEINS),
    VEGETABLE("vegetable", "tooltip.diet.category.vegetable", Items.CARROT, 0xFF30D11C, ModItemTagsProvider.VEGETABLES),
    SUGAR("sugar", "tooltip.diet.category.sugar", Items.SUGAR, 0xFFFC8DE6, ModItemTagsProvider.SUGARS);

    public static final FoodDataCategories[] VALUES = values();
    public static final int COUNT = VALUES.length;

    private final String serializedName;
    private final String translationKey;
    private final Item item;
    private final int color;
    private final TagKey<Item> tag;

    FoodDataCategories(String serializedName, String translationKey, Item item, int color, TagKey<Item> tag) {
        this.serializedName = serializedName;
        this.translationKey = translationKey;
        this.item = item;
        this.color = color;
        this.tag = tag;
    }

    public static FoodDataCategories[] fromItemStack(ItemStack stack) {
        return Arrays.stream(VALUES).filter(category -> stack.is(category.tag)).toArray(FoodDataCategories[]::new);
    }

    @Override
    public @NonNull String getSerializedName() {
        return serializedName;
    }

    public Component getDisplayName() {
        return Component.translatable(translationKey).withColor(this.getColor());
    }

    public Item getItem() {
        return item;
    }

    public int getColor() {
        return color;
    }

    public enum Activity implements StringRepresentable {
        SWIM("swim", FRUIT, "tooltip.diet.activity.swim"),
        DROWN("drown", FRUIT, "tooltip.diet.activity.drown"),
        WALK("walk", GRAIN, "tooltip.diet.activity.walk"),
        MINE("mine", GRAIN, "tooltip.diet.activity.mine"),
        ATTACK("attack", PROTEIN, "tooltip.diet.activity.attack"),
        HURT("hurt", PROTEIN, "tooltip.diet.activity.hurt"),
        FALL("fall", PROTEIN, "tooltip.diet.activity.fall"),
        FIRE("fire", PROTEIN, "tooltip.diet.activity.fire"),
        SPRINT("sprint", VEGETABLE, "tooltip.diet.activity.sprint"),
        JUMP("jump", VEGETABLE, "tooltip.diet.activity.jump"),
        CLIMB("climb", VEGETABLE, "tooltip.diet.activity.climb"),
        HEAL("heal", SUGAR, "tooltip.diet.activity.heal"),
        EFFECT("effect", SUGAR, "tooltip.diet.activity.effect"),
        STARVE("starve", SUGAR, "tooltip.diet.activity.starve");

        public static final Activity[] VALUES = values();
        public static final Codec<Activity> CODEC = StringRepresentable.fromEnum(Activity::values);

        private final String serializedName;
        private final FoodDataCategories relatedCategory;
        private final String translationKey;

        Activity(String serializedName, FoodDataCategories relatedCategory, String translationKey) {
            this.serializedName = serializedName;
            this.relatedCategory = relatedCategory;
            this.translationKey = translationKey;
        }

        @Override
        public @NonNull String getSerializedName() {
            return serializedName;
        }

        public FoodDataCategories getRelatedCategory() {
            return relatedCategory;
        }

        public Component getDisplayName() {
            return Component.translatable(translationKey);
        }
    }
}