package com.azukii.diet.event;

import com.azukii.diet.DietClientMod;
import com.azukii.diet.DietMod;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.gui.screen.FoodCategoriesScreen;
import com.azukii.diet.profile.ClientFoodProfileCache;
import com.azukii.diet.profile.FoodProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = DietMod.MODID, value = Dist.CLIENT)
public class ModClientEvents {
    private static final Identifier FOOD_DATA_BUTTON = Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_button");
    private static final Identifier FOOD_DATA_BUTTON_HIGHLIGHTED = Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_button_highlighted");

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof InventoryScreen screen) {
            event.addListener(new ImageButton(
                    screen.getLeftPos() + 134,
                    screen.height / 2 - 22,
                    20, 18,
                    new WidgetSprites(
                            FOOD_DATA_BUTTON,
                            FOOD_DATA_BUTTON_HIGHLIGHTED
                    ),
                    _ -> Minecraft.getInstance().setScreen(new FoodCategoriesScreen(screen)),
                    Component.empty()
            ));
        }
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }

        // Check if this is a food item
        boolean isFood = stack.getItem().components().get(DataComponents.FOOD) != null;
        if (!isFood) {
            return;
        }

        Identifier cacheKey = BuiltInRegistries.ITEM.getKey(stack.getItem());
        List<Component> cached = TooltipHandler.CACHE.get(cacheKey);
        if (cached == null) {
            cached = TooltipHandler.buildTooltipLines(stack);
            TooltipHandler.CACHE.put(cacheKey, cached);
        }

        if (!cached.isEmpty()) {
            if (Minecraft.getInstance().hasShiftDown()) {
                event.getToolTip().addAll(cached);
            } else {
                event.getToolTip().add(Component.translatable("tooltip.diet.category").withStyle(ChatFormatting.WHITE));
            }
        }
    }

    public static class TooltipHandler {
        public static final Map<Identifier, List<Component>> CACHE = new HashMap<>();

        public static List<Component> buildTooltipLines(ItemStack stack) {
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
            for (FoodCategories category : FoodCategories.VALUES) {
                float value = profile.get(category);
                if (value <= 0.0f) {
                    continue;
                }
                lines.add(createDietLine(category.getName(), value, category.getColor()));
            }
            return lines;
        }

        private static Component createDietLine(String translationKey, float value, int color) {
            MutableComponent line = Component.literal("");
            line.append(Component.literal(" - ").withStyle(ChatFormatting.GRAY));
            line.append(Component.translatable(translationKey).append(": ").withStyle(Style.EMPTY.withColor(color)));
            line.append(Component.literal(String.format("+%.1f", value)).withStyle(ChatFormatting.WHITE));
            return line;
        }
    }
}
