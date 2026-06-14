package com.azukii.diet.gui.screen;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.activity.ActivitiesCategories;
import com.azukii.diet.core.food.FoodCategories;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.core.food.ModFoodData;
import com.azukii.diet.item.FoodItemProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodStatsScreen {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(DietMod.MODID, "textures/gui/food_categories.png");
    public static final int OVERLAY_WIDTH = 148;
    private static final int OVERLAY_HEIGHT = 167;
    private static final int PADDING_TOP = 20;
    private static final int LABEL_OFFSET_X = 10;
    private static final float TEXT_SCALE = 0.7f;

    public static void render(GuiGraphicsExtractor graphics, InventoryScreen screen, int mouseX, int mouseY, float partialTick) {
        int x = screen.getLeftPos() - (2 + OVERLAY_WIDTH);
        int y = screen.getTopPos() - 1;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0f, 0f, OVERLAY_WIDTH, OVERLAY_HEIGHT, 256, 256);

        Player player = Minecraft.getInstance().player;
        ModFoodData data = player != null ? player.getData(ModAttachments.FOOD_DATA) : null;
        float max = FoodRegistry.getMaxValue();

        int labelX = x + LABEL_OFFSET_X;
        for (int i = 0; i < FoodCategories.VALUES.length; i++) {
            FoodCategories category = FoodCategories.VALUES[i];
            int barY = startY(y) + i * 31;

            float value = data != null ? data.getValues(category) : 0f;
            float percentage = max > 0f ? Math.clamp(value / max, 0f, 1f) : 0f;
            int level = data != null ? data.getLevel(category) : 0;

            renderBar(graphics, screen, new ItemStack(category.getItem()), labelX, barY, level, percentage, category.getColor());

            if (isHovered(mouseX, mouseY, labelX, barY - 8, 16, 16)) {
                graphics.setComponentTooltipForNextFrame(screen.getFont(), createExhaustionTooltip(category), mouseX, mouseY);
            }
        }
    }

    private static int startY(int y) {
        return y + PADDING_TOP;
    }

    private static boolean isHovered(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    private static void renderBar(GuiGraphicsExtractor graphics, InventoryScreen screen, ItemStack icon, int labelX, int y, int level, float percentage, int color) {
        graphics.item(icon, labelX, y - 8);

        graphics.pose().pushMatrix();
        graphics.pose().translate(labelX + 20, y - 7);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.translatable("tooltip.diet.category.level", level).withColor(color),0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(labelX + 20, y + 2);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.literal(String.format(Locale.ROOT, "%.2f%%", percentage * 100f)).withColor(color),0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();
    }

    public static List<Component> createExhaustionTooltip(FoodCategories category) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.diet.action_eaten").withStyle(ChatFormatting.BLUE));
        lines.add(Component.translatable("tooltip.diet.action_exhaustion").withStyle(ChatFormatting.BLUE));
        for (ActivitiesCategories action : ActivitiesCategories.VALUES) {
            if (action.getRelatedCategory().equals(category)) {
                lines.add(Component.translatable("tooltip.diet.action_categories",
                        Component.translatable(action.getTranslationKey())));
            }
        }
        return lines;
    }
}