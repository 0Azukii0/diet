package com.azukii.diet.mixin;

import com.azukii.diet.client.ClientboundSetFoodSaturationPacket;
import com.azukii.diet.registry.FoodDataCategories;
import com.azukii.diet.registry.FoodDataMixinAccess;
import com.mojang.serialization.Codec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(FoodData.class)
public abstract class FoodDataMixin implements FoodDataMixinAccess {
    @Unique
    private static final Codec<float[]> FLOAT_ARRAY_CODEC = Codec.FLOAT.listOf().xmap(
            list -> {
                float[] array = new float[list.size()];
                for (int i = 0; i < array.length; i++) {
                    array[i] = list.get(i);
                }
                return array;
            },
            array -> {
                List<Float> list = new ArrayList<>(array.length);
                for (float value : array) {
                    list.add(value);
                }
                return list;
            }
    );
    @Unique
    private float[] diet$categoriesSaturationLevels;
    @Unique
    private FoodDataCategories.Activity diet$currentActivity = null;
    @Unique
    private int diet$currentActivityCooldown = 0;
    @Unique
    private ItemStack diet$lastFoodEaten = Items.COOKED_BEEF.getDefaultInstance();
    @Unique
    private boolean diet$dirty = false;
    @Shadow
    private int foodLevel = 20;
    @Shadow
    private float saturationLevel = 5.0F;
    @Shadow
    private float exhaustionLevel;
    @Shadow
    private int tickTimer;

    @Shadow
    public abstract void addExhaustion(float amount);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void diet$init(CallbackInfo ci) {
        this.diet$categoriesSaturationLevels = new float[FoodDataCategories.COUNT];
    }

    /**
     * @author Azukii
     * @reason Saturation removed by tick is calculated by a percentage of all categories and player activities
     */
    @Overwrite
    public void tick(ServerPlayer player) {
        ServerLevel level = player.level();
        Difficulty difficulty = level.getDifficulty();

        float values = 0.0F;
        for (FoodDataCategories category : FoodDataCategories.VALUES) {
            if (this.diet$currentActivity != null && this.diet$currentActivity.getRelatedCategory().equals(category)) {
                values += this.diet$getCategorySaturationLevels(category) * 1.5f;
            } else values += this.diet$getCategorySaturationLevels(category);
        }
        float saturationRemoved = Mth.clamp(2.0f - (values / 50), 0.0f, 2.0f);

        if (this.exhaustionLevel > 4.0F) {
            this.exhaustionLevel -= 4.0F;
            for (FoodDataCategories category : FoodDataCategories.VALUES) {
                this.diet$setCategorySaturationLevels(category, Math.max(this.diet$getCategorySaturationLevels(category) - saturationRemoved, 0.0F));
            }
            if (this.saturationLevel > 0.0F) {
                this.saturationLevel = Math.max(this.saturationLevel - saturationRemoved, 0.0F);
            } else if (difficulty != Difficulty.PEACEFUL) {
                this.foodLevel = Math.max(this.foodLevel - 1, 0);
            }
        }

        boolean naturalRegen = level.getGameRules().get(GameRules.NATURAL_HEALTH_REGENERATION);
        if (naturalRegen && this.saturationLevel > 0.0F && player.isHurt() && this.foodLevel >= 20) {
            ++this.tickTimer;
            if (this.tickTimer >= 10) {
                float saturationSpent = Math.min(this.saturationLevel, 6.0F);
                player.heal(saturationSpent / 6.0F);
                this.addExhaustion(saturationSpent);
                this.tickTimer = 0;
            }
        } else if (naturalRegen && this.foodLevel >= 18 && player.isHurt()) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                player.heal(1.0F);
                this.addExhaustion(6.0F);
                this.tickTimer = 0;
            }
        } else if (this.foodLevel <= 0) {
            ++this.tickTimer;
            if (this.tickTimer >= 80) {
                if (player.getHealth() > 10.0F || difficulty == Difficulty.HARD || player.getHealth() > 1.0F && difficulty == Difficulty.NORMAL) {
                    player.hurtServer(level, player.damageSources().starve(), 1.0F);
                    this.diet$setCurrentActivity(FoodDataCategories.Activity.STARVE);
                }
                this.tickTimer = 0;
            }
        } else {
            this.tickTimer = 0;
        }

        if (this.diet$currentActivityCooldown > 0) {
            this.diet$currentActivityCooldown--;
        }

        if (this.diet$dirty) {
            this.diet$syncToClient(player);
        }
    }

    @Unique
    public float diet$getCategorySaturationLevels(FoodDataCategories category) {
        return this.diet$categoriesSaturationLevels[category.ordinal()];
    }

    @Unique
    public void diet$setCategorySaturationLevels(FoodDataCategories category, float saturation) {
        this.diet$categoriesSaturationLevels[category.ordinal()] = saturation;
        this.diet$dirty = true;
    }

    @Unique
    public void diet$setAllCategorySaturationLevels(float[] values) {
        System.arraycopy(values, 0, this.diet$categoriesSaturationLevels, 0, FoodDataCategories.COUNT);
    }

    @Unique
    public void diet$setCurrentActivity(FoodDataCategories.Activity currentActivity) {
        if (this.diet$currentActivity != currentActivity && this.diet$currentActivityCooldown <= 0) {
            this.diet$currentActivityCooldown = 100;
            this.diet$currentActivity = currentActivity;
        }
    }

    @Unique
    public ItemStack diet$getLastFoodEaten() {
        return this.diet$lastFoodEaten;
    }

    @Unique
    public void diet$setLastFoodEaten(ItemStack stack) {
        this.diet$lastFoodEaten = stack;
    }

    @Unique
    public void diet$setLastFoodEatenServer(ItemStack stack) {
        this.diet$lastFoodEaten = stack;
        this.diet$dirty = true;
    }

    @Unique
    public void diet$syncToClient(ServerPlayer player) {
        PacketDistributor.sendToPlayer(player, new ClientboundSetFoodSaturationPacket(this.diet$categoriesSaturationLevels, this.diet$lastFoodEaten));
        this.diet$dirty = false;
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueInput;)V")
    public void readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        this.diet$currentActivity = input.read("currentActivity", FoodDataCategories.Activity.CODEC).orElse(null);
        input.read("categoriesSaturation", FLOAT_ARRAY_CODEC).ifPresent(loaded -> {
            int length = Math.min(loaded.length, FoodDataCategories.COUNT);
            System.arraycopy(loaded, 0, this.diet$categoriesSaturationLevels, 0, length);
        });
        this.diet$currentActivityCooldown = input.getIntOr("currentActivityCooldown", 0);
        this.diet$lastFoodEaten = input.read("lastFoodEaten", ItemStack.CODEC).orElse(Items.COOKED_BEEF.getDefaultInstance());
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData(Lnet/minecraft/world/level/storage/ValueOutput;)V")
    public void addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        output.store("categoriesSaturation", FLOAT_ARRAY_CODEC, this.diet$categoriesSaturationLevels);
        if (this.diet$currentActivity != null) {
            output.store("currentActivity", FoodDataCategories.Activity.CODEC, this.diet$currentActivity);
        }
        output.putInt("currentActivityCooldown", this.diet$currentActivityCooldown);
        output.store("lastFoodEaten", ItemStack.CODEC, this.diet$lastFoodEaten);
    }
}
