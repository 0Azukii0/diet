package com.azukii.diet.core.activity;

import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.core.food.ModFoodData;
import com.azukii.diet.core.thirst.ThirstData;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ActivityEvents {
    public static float exhaustionReductionShortSheen(Player player, ActivitiesCategories source) {
        return exhaustionReductionLongSheen(player, source, 7);
    }

    public static float exhaustionReductionLongSheen(Player player, ActivitiesCategories source) {
        return exhaustionReductionLongSheen(player, source, 15); // used to be 20
        //TODO healing makes the sheen keep going for ages for some reason, and natural generation is not counted at all
    }

    public static float exhaustionReductionLongSheen(Player player, ActivitiesCategories source, int cooldown) {
        PlayerActivityData playerActivityData = player.getData(ModAttachments.PLAYER_ACTIVITY);
        playerActivityData.setHurtOrHeal(source == ActivitiesCategories.HURT || source == ActivitiesCategories.HEAL);

        Holder.Reference<Item> item = BuiltInRegistries.ITEM.get(playerActivityData.getLastFood()).orElse(Items.COOKED_BEEF.builtInRegistryHolder());
        FoodItemProfile profile = FoodRegistry.getProfile(new ItemStack(item));

        float ratio = computeRatio(profile, source, player);
        if (ratio > 0.0f) {
            playerActivityData.setSheenCooldown(cooldown);
            player.setData(ModAttachments.PLAYER_ACTIVITY, playerActivityData);
        }
        return ratio;
    }

    private static float computeRatio(FoodItemProfile profile, ActivitiesCategories source, Player player) {
        float total = profile.total();
        if (total <= 0.0f) return 0.0f;

        float relevant = profile.get(source.getRelatedCategory());
        float baseRatio = Math.min(relevant / total, 0.75f);

        ModFoodData data = player.getData(ModAttachments.FOOD_DATA);
        float maxVal = FoodRegistry.getMaxValue();
        float barLevel = maxVal > 0f ? Math.clamp(data.getValues(source.getRelatedCategory()) / maxVal, 0f, 1f) : 0f;

        return baseRatio * barLevel;
    }

    public static float thirstExhaustionMultiplier(Player player) {
        ThirstData data = player.getData(ModAttachments.THIRST_DATA);
        int level = data.getThirstLevel();
        return 1.0f + (float)(ThirstData.THIRST_MAX_LEVEL - level) / ThirstData.THIRST_MAX_LEVEL;
    }
}
