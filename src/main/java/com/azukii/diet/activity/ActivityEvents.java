package com.azukii.diet.activity;

import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.data.ModFoodData;
import com.azukii.diet.data.PlayerActivityData;
import com.azukii.diet.profile.FoodProfile;
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
        PlayerActivityData data = player.getData(ModAttachments.PLAYER_ACTIVITY);
        data.setHurtOrHeal(source == ActivitiesCategories.HURT || source == ActivitiesCategories.HEAL);

        Holder.Reference<Item> item = BuiltInRegistries.ITEM.get(data.getLastFood()).orElse(Items.COOKED_BEEF.builtInRegistryHolder());
        FoodProfile profile = FoodRegistry.getProfile(new ItemStack(item));

        float ratio = computeRatio(profile, source, player);
        if (ratio > 0.0f) {
            data.setSheenCooldown(cooldown);
        }
        return ratio;
    }

    private static float computeRatio(FoodProfile profile, ActivitiesCategories source, Player player) {
        float total = 0.0f;
        for (FoodCategories category : FoodCategories.VALUES) {
            total += profile.get(category);
        }
        if (total <= 0.0f) return 0.0f;

        float relevant = profile.get(source.getRelatedCategory());
        float baseRatio = Math.min(relevant / total, 0.75f);

        ModFoodData data = player.getData(ModAttachments.FOOD_DATA);
        float maxVal = FoodRegistry.getMaxValue();
        float barLevel = maxVal > 0f ? Math.clamp(data.get(source.getRelatedCategory()) / maxVal, 0f, 1f) : 0f;

        return baseRatio * barLevel;
    }
}
