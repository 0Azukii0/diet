package com.azukii.diet.client;

import com.azukii.diet.DietMod;
import com.azukii.diet.client.gui.screen.FoodStatsRenderState;
import com.azukii.diet.client.gui.screen.FoodStatsScreen;
import com.azukii.diet.registry.FoodDataCategories;
import com.azukii.diet.registry.FoodDataMixinAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.event.RegisterClientPayloadHandlersEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

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
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        boolean holdingFood = (!mainHand.isEmpty() && mainHand.get(DataComponents.FOOD) != null)
                || (!offHand.isEmpty() && offHand.get(DataComponents.FOOD) != null);
        if (holdingFood) LastEatenFoodIcon.hudTimer = 2f;

        FoodData foodData = player.getFoodData();
        if (foodData instanceof FoodDataMixinAccess access) {
            ItemStack currentLastFood = access.diet$getLastFoodEaten();
            if (currentLastFood != null && LastEatenFoodIcon.lastFoodDisplayed != null && !ItemStack.isSameItemSameComponents(currentLastFood, LastEatenFoodIcon.lastFoodDisplayed)) {
                if (LastEatenFoodIcon.lastFoodDisplayed != null) {
                    LastEatenFoodIcon.hudTimer = 3f;
                }
                LastEatenFoodIcon.lastFoodDisplayed = currentLastFood;
            }

            if (LastEatenFoodIcon.hudTimer > 0f) {
                LastEatenFoodIcon.hudTimer = Math.max(0f, LastEatenFoodIcon.hudTimer - (1f / 20f));
            }
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiLayerEvent.Post event) {
        if (event.getName() != VanillaGuiLayers.FOOD_LEVEL) return;
        if (LastEatenFoodIcon.hudTimer <= 0f) return;
        if (LastEatenFoodIcon.lastFoodDisplayed == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Player player = mc.player;
        if (player.getAbilities().instabuild) return;

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = screenWidth / 2 - 8;
        int y = screenHeight - 52;

        GuiGraphicsExtractor graphics = event.getGuiGraphics();
        float fadeDuration = 1.0f;
        float scale = Math.min(1f, LastEatenFoodIcon.hudTimer / fadeDuration);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 8, y + 8);
        graphics.pose().scale(scale, scale);
        graphics.pose().translate(-8, -8);
        graphics.item(LastEatenFoodIcon.lastFoodDisplayed, 0, 0);
        graphics.pose().popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onGatherTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> tooltips = event.getToolTip();
        if (event.getEntity() == null) return;
        if (stack.has(DataComponents.FOOD)) {
            FoodDataCategories[] categories = FoodDataCategories.fromItemStack(stack);
            for (FoodDataCategories category : categories) tooltips.add(category.getDisplayName());
        }
    }

    @SubscribeEvent
    static void registerClientPayloads(RegisterClientPayloadHandlersEvent event) {
        event.register(
                ClientboundSetFoodSaturationPacket.TYPE,
                ClientboundSetFoodSaturationPacket::handle
        );
    }

    public static class LastEatenFoodIcon {
        private static float hudTimer = 0f;
        private static ItemStack lastFoodDisplayed = null;
    }
}
