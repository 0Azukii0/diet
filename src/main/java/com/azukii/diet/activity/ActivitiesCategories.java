package com.azukii.diet.activity;

import com.azukii.diet.data.FoodCategories;

public enum ActivitiesCategories {
    SWIM(FoodCategories.FRUIT,       "tooltip.diet.action.swim"),
    SPRINT(FoodCategories.VEGETABLE, "tooltip.diet.action.sprint"),
    JUMP(FoodCategories.VEGETABLE,   "tooltip.diet.action.jump"),
    MINE(FoodCategories.GRAIN,       "tooltip.diet.action.mine"),
    ATTACK(FoodCategories.PROTEIN,   "tooltip.diet.action.attack"),
    HURT(FoodCategories.PROTEIN,     "tooltip.diet.action.hurt"),
    HEAL(FoodCategories.SUGAR,       "tooltip.diet.action.heal"),
    EFFECT(FoodCategories.SUGAR,     "tooltip.diet.action.effect");

    public static final ActivitiesCategories[] VALUES = values();

    private final FoodCategories relatedCategory;
    private final String translationKey;

    ActivitiesCategories(FoodCategories relatedCategory, String translationKey) {
        this.relatedCategory = relatedCategory;
        this.translationKey = translationKey;
    }

    public FoodCategories getRelatedCategory() { return relatedCategory; }
    public String getTranslationKey() { return translationKey; }
}
