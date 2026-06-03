package com.azukii.diet.mixin;

import com.azukii.diet.activity.ActivitiesCategories;
import com.azukii.diet.activity.ActivityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

import static java.lang.Math.max;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerActivitiesAccess {
    @Unique private static final EntityDataAccessor<String> LAST_FOOD_ID;
    @Unique private static final EntityDataAccessor<Integer> SHEEN_COOLDOWN_ID;
    @Unique private static final EntityDataAccessor<Boolean> HURT_OR_HEAL_ID;

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    static {
        LAST_FOOD_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.STRING);
        SHEEN_COOLDOWN_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.INT);
        HURT_OR_HEAL_ID = SynchedEntityData.defineId(Player.class, EntityDataSerializers.BOOLEAN);
    }

    public Identifier mindful_eating$getLastFood() {
        Identifier resourceLocation = Identifier.tryParse(this.entityData.get(LAST_FOOD_ID));
        if (resourceLocation == null || !BuiltInRegistries.ITEM.containsKey(resourceLocation)) {
            resourceLocation = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
            mindful_eating$setLastFood(resourceLocation);
        }
        return resourceLocation;
    }

    public void mindful_eating$setLastFood(Identifier lastFood) {
        this.entityData.set(LAST_FOOD_ID, lastFood.toString());
    }

    public int mindful_eating$getSheenCooldown() {
        return this.entityData.get(SHEEN_COOLDOWN_ID);
    }

    public void mindful_eating$setSheenCooldown(int cooldown) {
        this.entityData.set(SHEEN_COOLDOWN_ID, cooldown);
    }

    public boolean mindful_eating$getHurtOrHeal() {
        return this.entityData.get(HURT_OR_HEAL_ID);
    }

    public void mindful_eating$setHurtOrHeal(boolean hurtOrHeal) {
        this.entityData.set(HURT_OR_HEAL_ID, hurtOrHeal);
    }

    @Inject(at = @At("TAIL"), method = "tick")
    protected void tail(CallbackInfo ci) {
        Player player = (((Player) (Object) this));

        if (!player.getActiveEffects().isEmpty()) {
            for (MobEffectInstance effect : player.getActiveEffects()) {
                if (effect.getEffect() == MobEffects.HUNGER) {
                    player.causeFoodExhaustion(0.0025F * (float) (player.getEffect(MobEffects.HUNGER).getAmplifier() + 1) * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.EFFECT));
                    break;
                }
            }
        }

        float reduction = 0;

        double disX = player.getX() - player.xOld;
        double disZ = player.getZ() - player.zOld;

        if (player.level().isClientSide() ^ ((PlayerActivitiesAccess) player).mindful_eating$getHurtOrHeal()) {
            ((PlayerActivitiesAccess) player).mindful_eating$setSheenCooldown(max(0, ((PlayerActivitiesAccess) player).mindful_eating$getSheenCooldown() - 1));
        }

        if (player.getDeltaMovement().length() == 0.0 || disX == 0.0 && disZ == 0.0) {
            return;
        }

        int distance = Math.round(Mth.sqrt((float) disX * (float) disX + (float) disZ * (float) disZ) * 100.0F);

        if (player.isSwimming() || player.isEyeInFluid(FluidTags.WATER)) {
            reduction = 0.0001F * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.SWIM) * Math.round(Mth.sqrt((float) disX * (float) disX + (float) disZ * (float) disZ) * 100.0F);
        } else if (player.isInWater()) {
            reduction = 0.0001F * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.SWIM) * distance;
        } else if (player.onGround() && player.isSprinting()) {
            reduction = 0.001F * ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.SPRINT) * distance;
        }

        player.getFoodData().addExhaustion(reduction);
    }

    @Inject(at = @At("TAIL"), method = "attack")
    protected void attack(CallbackInfo ci) {
        Player player = (((Player) (Object) this));
        float ratio = ActivityEvents.exhaustionReductionLongSheen(player, ActivitiesCategories.ATTACK);
        player.causeFoodExhaustion(0.1F * ratio);
    }

    @Inject(at = @At("TAIL"), method = "hurtServer")
    protected void hurt(ServerLevel level, DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        Player player = (((Player) (Object) this));
        float ratio = ActivityEvents.exhaustionReductionLongSheen(player, ActivitiesCategories.HURT);
        player.causeFoodExhaustion(source.getFoodExhaustion() * ratio);
    }

    @Inject(at = @At("TAIL"), method = "defineSynchedData")
    protected void defineSynchedData(SynchedEntityData.Builder entityData, CallbackInfo ci) {
        entityData.define(LAST_FOOD_ID, BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF).toString());
        entityData.define(SHEEN_COOLDOWN_ID, 0);
        entityData.define(HURT_OR_HEAL_ID, false);
    }

    @Inject(at = @At("TAIL"), method = "addAdditionalSaveData")
    protected void addAdditionalSaveData(ValueOutput output, CallbackInfo ci) {
        output.putBoolean("HurtOrHeal", mindful_eating$getHurtOrHeal());
        output.putString("LastFoodEaten", mindful_eating$getLastFood().toString());
        output.putInt("SheenCooldown", mindful_eating$getSheenCooldown());
    }

    @Inject(at = @At("TAIL"), method = "readAdditionalSaveData")
    protected void readAdditionalSaveData(ValueInput input, CallbackInfo ci) {
        Identifier lastFood = Identifier.tryParse(input.getStringOr("LastFoodEaten", ""));
        if (lastFood == null) {
            lastFood = BuiltInRegistries.ITEM.getKey(Items.COOKED_BEEF);
        }

        mindful_eating$setLastFood(lastFood);
        mindful_eating$setHurtOrHeal(input.getBooleanOr("HurtOrHeal", false));
        mindful_eating$setSheenCooldown(input.getIntOr("SheenCooldown", 0));
    }
}
