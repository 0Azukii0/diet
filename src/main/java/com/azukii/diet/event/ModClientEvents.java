package com.azukii.diet.event;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.component.ModComponents;
import com.azukii.diet.core.activity.PlayerActivityData;
import com.azukii.diet.core.food.FoodCategories;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.gui.screen.FoodStatsRenderState;
import com.azukii.diet.gui.screen.FoodStatsScreen;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.*;

@EventBusSubscriber(modid = DietMod.MODID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Background event) {
        if (!(event.getScreen() instanceof InventoryScreen screen)) return;
        if (!FoodStatsRenderState.isOpen()) return;

        FoodStatsScreen.render(event.getGuiGraphics(), screen, event.getMouseX(), event.getMouseY(), event.getPartialTick());
    }

    @SubscribeEvent
    public static void onScreenClose(ScreenEvent.Closing event) {
        if (event.getScreen() instanceof InventoryScreen) {
            FoodStatsRenderState.close();
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        Player player = mc.player;

        // Food item in hand
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean holdingFood = (!mainHand.isEmpty() && mainHand.get(DataComponents.FOOD) != null) || (!offHand.isEmpty() && offHand.get(DataComponents.FOOD) != null);

        if (holdingFood) LastEatenFoodIcon.hudTimer = 3f;

        // Last food has changed
        Identifier currentLastFood = player.getData(ModAttachments.PLAYER_ACTIVITY).getLastFood();
        if (!currentLastFood.equals(LastEatenFoodIcon.lastFoodDisplayed)) {
            boolean isFirstInit = LastEatenFoodIcon.lastFoodDisplayed == null;
            LastEatenFoodIcon.lastFoodDisplayed = currentLastFood;
            if (!isFirstInit) {
                LastEatenFoodIcon.hudTimer = 3f;
            }
        }

        if (LastEatenFoodIcon.hudTimer > 0f) {
            LastEatenFoodIcon.hudTimer = Math.max(0f, LastEatenFoodIcon.hudTimer - (1f / 20f));
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiLayerEvent.Post event) {
        if (event.getName() != VanillaGuiLayers.FOOD_LEVEL) return;
        if (LastEatenFoodIcon.hudTimer <= 0f) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;
        if (player.getAbilities().instabuild) return;
        PlayerActivityData data = player.getData(ModAttachments.PLAYER_ACTIVITY);
        Identifier lastFood = data.getLastFood();

        Holder.Reference<Item> itemHolder = BuiltInRegistries.ITEM.get(lastFood).orElse(Items.COOKED_BEEF.builtInRegistryHolder());
        ItemStack stack = new ItemStack(itemHolder);
        if (stack.isEmpty()) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        boolean showingAir = player.getAirSupply() < player.getMaxAirSupply();
        int baseY = screenHeight - 55;
        if (showingAir) {
            baseY -= 10;
        }

        int x = screenWidth / 2 + 75;
        int y = baseY;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        graphics.pose().pushMatrix();
        graphics.pose().translate(x, y);
        graphics.item(stack, 0, 0);
        graphics.itemDecorations(mc.font, stack, 0, 0);
        graphics.pose().popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGatherTooltip(ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        List<Component> toolTips = event.getToolTip();
        if (itemStack.has(ModComponents.THIRST)) toolTips.add(Component.translatable("tooltip.diet.thirst.level_added", Objects.requireNonNull(itemStack.get(ModComponents.THIRST)).value()));
        if (itemStack.has(DataComponents.FOOD)) {
            FoodItemProfile profile = FoodRegistry.getProfile(itemStack);
            for (FoodCategories category : FoodCategories.VALUES) {
                float value = profile.get(category);
                if (value <= 0.0f) continue;
                toolTips.add(Component.empty().append(Component.translatable(category.getName()).withColor(category.getColor()))
                        .append(Component.translatable("tooltip.diet.category.level_added").withColor(0xFFFFFF)));
            }
        }
    }

    public static class LastEatenFoodIcon {
        private static float hudTimer = 0f;
        private static Identifier lastFoodDisplayed = null;
    }
}
