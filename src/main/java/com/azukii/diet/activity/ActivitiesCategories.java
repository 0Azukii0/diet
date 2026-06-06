package com.azukii.diet.activity;

import com.azukii.diet.data.FoodCategories;

public enum ActivitiesCategories {
    SWIM(FoodCategories.FRUIT),
    SPRINT(FoodCategories.VEGETABLE),
    JUMP(FoodCategories.VEGETABLE),
    MINE(FoodCategories.GRAIN),
    ATTACK(FoodCategories.PROTEIN),
    HURT(FoodCategories.PROTEIN),
    HEAL(FoodCategories.SUGAR),
    EFFECT(FoodCategories.SUGAR);

    private final FoodCategories relatedCategory;

    ActivitiesCategories(FoodCategories relatedCategory) {
        this.relatedCategory = relatedCategory;
    }

    public FoodCategories getRelatedCategory() {
        return relatedCategory;
    }
}
