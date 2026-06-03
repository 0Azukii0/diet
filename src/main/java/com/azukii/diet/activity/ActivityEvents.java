package com.azukii.diet.activity;

import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.mixin.PlayerActivitiesAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.max;

public class ActivityEvents {
    public static float exhaustionReductionShortSheen(Player player, ActivitiesCategories source) {
        return exhaustionReductionLongSheen(player, source, 7);
    }

    public static float exhaustionReductionLongSheen(Player player, ActivitiesCategories source) {
        return exhaustionReductionLongSheen(player, source, 15); // used to be 20
        //TODO healing makes the sheen keep going for ages for some reason, and natural generation is not counted at all
    }

    public static float exhaustionReductionLongSheen(Player player, ActivitiesCategories source, int cooldown) {
        ((PlayerActivitiesAccess) player).mindful_eating$setHurtOrHeal(source == ActivitiesCategories.HURT || source == ActivitiesCategories.HEAL);

        if (!MEConfig.COMMON.proportionalDiet.get()) {
            for (FoodCategories group : FoodCategories.VALUES) {
                for (String configGroup : MEConfig.COMMON.foodGroupExhaustion[source.ordinal()].get().split("/")) {
                    if (group.getName().equals(configGroup)) {
                        ((PlayerActivitiesAccess) player).mindful_eating$setSheenCooldown(cooldown);
                        return -MEConfig.COMMON.exhaustionReduction.get().floatValue();
                    }
                }
            }

            return 0.0F;
        } else {
            AtomicReference<Float> percentage = new AtomicReference<>(0.0F);
            DietComponents.DIET_TRACKER.maybeGet(player).ifPresent(tracker -> {
                for (String configGroup : MEConfig.COMMON.foodGroupExhaustion[source.ordinal()].get().split("/"))
                    percentage.set(tracker.getValue(configGroup));
            });
            if (percentage.get() > 0.0F)
                ((PlayerActivitiesAccess) player).mindful_eating$setSheenCooldown(cooldown);
            return max(-percentage.get(), 1.0F);
        }
    }
}
