package com.azukii.diet.core.activity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import org.jspecify.annotations.NonNull;

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

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerActivityData> STREAM_CODEC =
            new StreamCodec<>() {
                @Override
                public @NonNull PlayerActivityData  decode(RegistryFriendlyByteBuf buf) {
                    PlayerActivityData data = new PlayerActivityData();
                    data.setLastFood(buf.readIdentifier());
                    data.setSheenCooldown(buf.readVarInt());
                    data.setHurtOrHeal(buf.readBoolean());
                    return data;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, PlayerActivityData data) {
                    buf.writeIdentifier(data.getLastFood());
                    buf.writeVarInt(data.getSheenCooldown());
                    buf.writeBoolean(data.getHurtOrHeal());
                }
            };
}
