package com.azukii.diet.mixin;


import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.activity.PlayerActivityData;
import com.azukii.diet.core.food.FoodCategories;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.core.food.ModFoodData;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {
    @Inject(at = @At("HEAD"), method = "finishUsingItem")
    protected void finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        if (itemStack.getComponents().get(DataComponents.FOOD) != null && entity instanceof Player player) {
            PlayerActivityData activityData = player.getData(ModAttachments.PLAYER_ACTIVITY);
            ModFoodData foodData = player.getData(ModAttachments.FOOD_DATA);
            Identifier currentFood = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
            FoodItemProfile profile = FoodRegistry.getProfile(itemStack);
            for (FoodCategories category : FoodCategories.VALUES) {
                float val = profile.get(category);
                if (val != 0.0F) {
                    activityData.setLastFood(currentFood);
                    foodData.addLevel(category);
                    return;
                }
            }
        }
    }
}
