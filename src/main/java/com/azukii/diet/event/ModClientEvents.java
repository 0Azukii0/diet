package com.azukii.diet.event;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.activity.PlayerActivityData;
import com.azukii.diet.core.food.FoodCategories;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.gui.screen.FoodCategoriesScreen;
import com.azukii.diet.item.FoodItemProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = DietMod.MODID, value = Dist.CLIENT)
public class ModClientEvents {
    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            event.addListener(new ImageButton(
                    screen.getLeftPos() + 134,
                    screen.height / 2 - 22,
                    20, 18,
                    new WidgetSprites(
                            Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_button"),
                            Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_button_highlighted")
                    ),
                    _ -> Minecraft.getInstance().setScreen(new FoodCategoriesScreen(screen)),
                    Component.empty()
            ));
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

        if (holdingFood) TooltipHandler.hudTimer = 3f;

        // Last food has changed
        Identifier currentLastFood = player.getData(ModAttachments.PLAYER_ACTIVITY).getLastFood();
        if (!currentLastFood.equals(TooltipHandler.lastFoodDisplayed)) {
            boolean isFirstInit = TooltipHandler.lastFoodDisplayed == null;
            TooltipHandler.lastFoodDisplayed = currentLastFood;
            if (!isFirstInit) {
                TooltipHandler.hudTimer = 3f;
            }
        }

        if (TooltipHandler.hudTimer > 0f) {
            TooltipHandler.hudTimer = Math.max(0f, TooltipHandler.hudTimer - (1f / 20f));
        }
    }

    @SubscribeEvent
    public static void onRenderHud(RenderGuiLayerEvent.Post event) {
        if (event.getName() != VanillaGuiLayers.FOOD_LEVEL) return;
        if (TooltipHandler.hudTimer <= 0f) return;

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

    @SubscribeEvent
    public static void onGatherTooltip(RenderTooltipEvent.GatherComponents event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        boolean isFood = stack.get(DataComponents.FOOD) != null;
        if (!isFood) {
            return;
        }

        TooltipHandler.CacheKey cacheKey = new TooltipHandler.CacheKey(BuiltInRegistries.ITEM.getKey(stack.getItem()), FoodRegistry.getConfig().hashCode());
        List<Component> cached = TooltipHandler.CACHE.get(cacheKey);
        if (cached == null) {
            cached = TooltipHandler.buildFoodTooltipLines(stack);
            TooltipHandler.CACHE.put(cacheKey, cached);
        }

        if (cached.isEmpty()) {
            return;
        }

        for (Component c : cached) {
            event.getTooltipElements().add(Either.left(c));
        }
    }

    public static class TooltipHandler {
        public static final Map<CacheKey, List<Component>> CACHE = new HashMap<>();
        private static float hudTimer = 0f;
        private static Identifier lastFoodDisplayed = null;

        public static List<Component> buildFoodTooltipLines(ItemStack stack) {
            FoodItemProfile profile = FoodRegistry.getProfile(stack);

            if (profile.isEmpty()) {
                return List.of();
            }

            List<Component> lines = new ArrayList<>();
            // Food categories
            for (FoodCategories category : FoodCategories.VALUES) {
                float value = profile.get(category);
                if (value <= 0.0f) continue;
                lines.add(createFoodCategoryLine(category.getName(), value, category.getColor()));
            }

            return lines;
        }

        private static Component createFoodCategoryLine(String translationKey, float value, int color) {
            MutableComponent line = Component.literal("");
            line.append(Component.translatable(translationKey).withStyle(Style.EMPTY.withColor(color)));
            return line;
        }

        public record CacheKey(Identifier id, int configHash) {}
    }
}
