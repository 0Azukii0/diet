package com.azukii.diet.mixin;

import net.minecraft.resources.Identifier;

public interface PlayerActivitiesAccess {
    Identifier mindful_eating$getLastFood();
    void mindful_eating$setLastFood(Identifier lastFood);

    int mindful_eating$getSheenCooldown();
    void mindful_eating$setSheenCooldown(int cooldown);

    boolean mindful_eating$getHurtOrHeal();
    void mindful_eating$setHurtOrHeal(boolean hurtOrHeal);
}
