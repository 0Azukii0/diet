package com.azukii.diet.event;

import com.azukii.diet.DietClientMod;
import com.azukii.diet.DietMod;
import com.azukii.diet.activity.ActivitiesCategories;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.data.PlayerActivityData;
import com.azukii.diet.gui.screen.FoodCategoriesScreen;
import com.azukii.diet.profile.ClientFoodProfileCache;
import com.azukii.diet.profile.FoodProfile;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
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

        boolean isFood = stack.getItem().components().get(DataComponents.FOOD) != null;
        if (!isFood) {
            return;
        }

        Identifier cacheKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Component> cached = TooltipHandler.CACHE.get(cacheKey);
        if (cached == null) {
            cached = TooltipHandler.buildFoodTooltipLines(stack);
            TooltipHandler.CACHE.put(cacheKey, cached);
        }

        if (cached.isEmpty()) {
            return;
        }

        if (Minecraft.getInstance().hasShiftDown()) {
            for (Component c : cached) {
                event.getTooltipElements().add(Either.left(c));
            }
        } else {
            event.getTooltipElements().add(Either.left(Component.translatable("tooltip.diet.category").withStyle(ChatFormatting.WHITE)));
        }
    }

    public static class TooltipHandler {
        public static final Map<Identifier, List<Component>> CACHE = new HashMap<>();
        private static float hudTimer = 0f;
        private static Identifier lastFoodDisplayed = null;

        public static List<Component> buildFoodTooltipLines(ItemStack stack) {
            // Try to get from client cache first (pre-calculated, no lag)
            ClientFoodProfileCache clientCache = DietClientMod.getClientDietCache();
            FoodProfile profile = null;

            if (clientCache != null) {
                Identifier itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
                profile = clientCache.getProfile(itemId);
            }

            // Fallback to FoodRegistry if not in client cache
            if (profile == null) {
                profile = FoodRegistry.getProfile(stack);
            }

            if (profile.isEmpty()) {
                return List.of();
            }

            List<Component> lines = new java.util.ArrayList<>();
            // Food nutrition
            for (FoodCategories category : FoodCategories.VALUES) {
                float value = profile.get(category);
                if (value <= 0.0f) continue;
                lines.add(createFoodCategoryLine(category.getName(), value, category.getColor()));
            }

            // Actions
            List<Component> actionLines = buildActionLines(profile);
            if (!actionLines.isEmpty()) {
                lines.add(Component.translatable("tooltip.diet.action").withStyle(ChatFormatting.GRAY));
                lines.addAll(actionLines);
            }
            return lines;
        }

        private static Component createFoodCategoryLine(String translationKey, float value, int color) {
            MutableComponent line = Component.literal("");
            line.append(Component.literal(" - ").withStyle(ChatFormatting.GRAY));
            line.append(Component.translatable(translationKey).append(": ").withStyle(Style.EMPTY.withColor(color)));
            line.append(Component.literal(String.format("+%.1f", value)).withStyle(ChatFormatting.WHITE));
            return line;
        }

        public static List<Component> buildActionLines(FoodProfile profile) {
            float total = 0.0f;
            for (FoodCategories category : FoodCategories.VALUES) {
                total += profile.get(category);
            }
            if (total <= 0.0f) return List.of();

            List<Component> lines = new java.util.ArrayList<>();
            for (ActivitiesCategories action : ActivitiesCategories.VALUES) {
                float relevant = profile.get(action.getRelatedCategory());
                if (relevant <= 0.0f) continue;

                float ratio = Math.min(relevant / total, 0.75f);
                int percent = Math.round(ratio * 100);

                lines.add(createActionLine(action, percent));
            }
            return lines;
        }

        private static Component createActionLine(ActivitiesCategories action, int percent) {
            MutableComponent line = Component.literal("");
            line.append(Component.literal(" - ").withStyle(ChatFormatting.GRAY));
            line.append(Component.translatable(action.getTranslationKey()).append(": ").withStyle(ChatFormatting.GRAY));
            line.append(Component.literal("-" + percent + "%").withStyle(ChatFormatting.GREEN));
            return line;
        }
    }
}
