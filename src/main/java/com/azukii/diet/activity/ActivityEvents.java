package com.azukii.diet.activity;

import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.data.FoodRegistry;
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
        ItemStack stack = new ItemStack(item);
        FoodProfile profile = FoodRegistry.getProfile(stack);

        for (FoodCategories category : FoodCategories.VALUES) {
            float val = profile.get(category);
            if (val != 0.0F) {
                data.setSheenCooldown(cooldown);
                return 0.75F;
            }
        }
        return 0.0F;
    }
}
