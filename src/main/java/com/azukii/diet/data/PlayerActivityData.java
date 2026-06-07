package com.azukii.diet.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;

public class PlayerActivityData implements ValueIOSerializable {
    private Identifier lastFood = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
    private int sheenCooldown = 0;
    private boolean hurtOrHeal = false;

    public Identifier getLastFood() {
        Identifier lastfood = this.lastFood;
        if (lastfood == null || !BuiltInRegistries.ITEM.containsKey(lastfood)) {
            lastfood = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
            setLastFood(lastfood);
        }
        return lastfood;
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
        output.putBoolean("HurtOrHeal", getHurtOrHeal());
        output.putString("LastFoodEaten", getLastFood().toString());
        output.putInt("SheenCooldown", getSheenCooldown());
    }

    @Override
    public void deserialize(ValueInput input) {
        Identifier lastFood = Identifier.tryParse(input.getStringOr("LastFoodEaten", ""));
        if (lastFood == null) {
            lastFood = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
        }

        setLastFood(lastFood);
        setHurtOrHeal(input.getBooleanOr("HurtOrHeal", false));
        setSheenCooldown(input.getIntOr("SheenCooldown", 0));
    }
}
