package com.azukii.diet.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class PlayerActivityData implements ValueIOSerializable {
    Identifier lastFood = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
    int sheenCooldown = 0;
    boolean hurtOrHeal = false;

    public Identifier diet$getLastFood() {
        Identifier lastfood = this.lastFood;
        if (lastfood == null || !BuiltInRegistries.ITEM.containsKey(lastfood)) {
            lastfood = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
            diet$setLastFood(lastfood);
        }
        return lastfood;
    }

    public void diet$setLastFood(Identifier lastFood) {
        this.lastFood = lastFood;
    }

    public int diet$getSheenCooldown() {
        return this.sheenCooldown;
    }

    public void diet$setSheenCooldown(int cooldown) {
        this.sheenCooldown = cooldown;
    }

    public boolean diet$getHurtOrHeal() {
        return this.hurtOrHeal;
    }

    public void diet$setHurtOrHeal(boolean hurtOrHeal) {
        this.hurtOrHeal = hurtOrHeal;
    }

    public Identifier getLastFood() {
        return this.lastFood;
    }

    public void setLastFood(Identifier lastFood) {
        this.lastFood = lastFood;
    }

    public int getSheenCooldown() {
        return this.sheenCooldown;
    }

    public void setSheenCooldown(int sheenCooldown) {
        this.sheenCooldown = sheenCooldown;
    }

    public boolean getHurtOrHeal() {
        return this.hurtOrHeal;
    }

    public void setHurtOrHeal(boolean hurtOrHeal) {
        this.hurtOrHeal = hurtOrHeal;
    }

    @Override
    public void serialize(ValueOutput output) {
        output.putBoolean("HurtOrHeal", diet$getHurtOrHeal());
        output.putString("LastFoodEaten", diet$getLastFood().toString());
        output.putInt("SheenCooldown", diet$getSheenCooldown());
    }

    @Override
    public void deserialize(ValueInput input) {
        Identifier lastFood = Identifier.tryParse(input.getStringOr("LastFoodEaten", ""));
        if (lastFood == null) {
            lastFood = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
        }

        diet$setLastFood(lastFood);
        diet$setHurtOrHeal(input.getBooleanOr("HurtOrHeal", false));
        diet$setSheenCooldown(input.getIntOr("SheenCooldown", 0));
    }
}
