package com.azukii.diet.data;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

public enum FoodCategories {
    GRAIN("tooltip.diet.category.fruit", Items.APPLE,0xFFD11C52),
    PROTEIN("tooltip.diet.category.grain", Items.WHEAT,0xFFD1A11C),
    VEGETABLE("tooltip.diet.category.protein", Items.COOKED_BEEF,0xFFD1771C),
    FRUIT("tooltip.diet.category.vegetable", Items.CARROT,0xFF30D11C),
    SUGAR("tooltip.diet.category.sugar", Items.SUGAR,0xFFFC8DE6);

    public static final FoodCategories[] VALUES = values();
    public static final int COUNT = VALUES.length;
    private final String name;
    private final Item item;
    private final int color;

    FoodCategories(String name, Item item, int color) {
        this.name = name;
        this.item = item;
        this.color = color;
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