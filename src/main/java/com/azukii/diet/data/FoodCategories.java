package com.azukii.diet.data;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum FoodCategories {
    FRUIT("tooltip.diet.category.fruit", Items.APPLE,0xFFD11C52),
    GRAIN("tooltip.diet.category.grain", Items.WHEAT,0xFFD1A11C),
    PROTEIN("tooltip.diet.category.protein", Items.COOKED_BEEF,0xFFD1771C),
    VEGETABLE("tooltip.diet.category.vegetable", Items.CARROT,0xFF30D11C),
    SUGAR("tooltip.diet.category.SUGAR", Items.SUGAR,0xFFFC8DE6);

    public static final FoodCategories[] VALUES = values();
    public static final int COUNT = VALUES.length;
    private final String name;
    private final Item item;
    private final int color;
    private final TagKey<Item> tag;

    FoodCategories(String name, Item item, int color) {
        this.name = name;
        this.item = item;
        this.color = color;
        this.tag = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("diet", name().toLowerCase()));
    }

    public static FoodCategories byName(String name) {
        for (FoodCategories c : VALUES) {
            if (c.name().equalsIgnoreCase(name)) return c;
        }
        return null;
    }

    public TagKey<Item> getTag() {
        return tag;
    }

    public String getName() {
        return name;
    }

    public Item getItem() {
        return item;
    }

    public int getColor() {
        return color;
    }
}