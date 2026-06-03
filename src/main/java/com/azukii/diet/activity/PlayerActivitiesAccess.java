package com.azukii.diet.activity;

import net.minecraft.resources.Identifier;

public interface PlayerActivitiesAccess {
    Identifier diet$getLastFood();
    void diet$setLastFood(Identifier lastFood);

    int diet$getSheenCooldown();
    void diet$setSheenCooldown(int cooldown);

    boolean diet$getHurtOrHeal();
    void diet$setHurtOrHeal(boolean hurtOrHeal);
}
