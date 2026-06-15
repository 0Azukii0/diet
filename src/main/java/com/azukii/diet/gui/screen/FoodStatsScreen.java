package com.azukii.diet.gui.screen;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.activity.ActivitiesCategories;
import com.azukii.diet.core.food.FoodCategories;
import com.azukii.diet.core.food.FoodRegistry;
import com.azukii.diet.core.food.ModFoodData;
import com.azukii.diet.core.thirst.ThirstData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class FoodStatsScreen {
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(DietMod.MODID, "textures/gui/food_categories.png");
    public static final int OVERLAY_WIDTH = 128;
    private static final int OVERLAY_HEIGHT = 119;
    private static final float TEXT_SCALE = 0.7f;

    private static final int COL_0_X = 15;
    private static final int COL_1_X = 71;
    private static final int ROW_START_Y = 19;
    private static final int ROW_HEIGHT = 33;

    private static final int TITLE_Y = -10;
    private static final int TEXT_OFFSET_X = 20;
    private static final int TEXT_LINE_1_Y = 1;
    private static final int TEXT_LINE_2_Y = 10;

    public static void render(GuiGraphicsExtractor graphics, InventoryScreen screen, int mouseX, int mouseY, float partialTick) {
        int x = screen.getLeftPos() - (2 + OVERLAY_WIDTH);
        int y = screen.getTopPos() - 1;

        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0f, 0f, OVERLAY_WIDTH, OVERLAY_HEIGHT, 256, 256);

        Player player = Minecraft.getInstance().player;
        ModFoodData foodData = player != null ? player.getData(ModAttachments.FOOD_DATA) : null;
        float max = FoodRegistry.getMaxValue();

        for (int i = 0; i < FoodCategories.VALUES.length; i++) {
            FoodCategories category = FoodCategories.VALUES[i];
            int row = i / 2;
            int col = i % 2;
            int itemX = x + (col == 0 ? COL_0_X : COL_1_X);
            int itemY = y + ROW_START_Y + row * ROW_HEIGHT;

            float value = foodData != null ? foodData.getValue(category) : 0f;
            float percentage = max > 0f ? Math.clamp(value / max, 0f, 1f) : 0f;
            int level = foodData != null ? foodData.getLevel(category) : 0;

            renderCategorySlot(graphics, screen, category, new ItemStack(category.getItem()), itemX, itemY, level, percentage);

            if (isHovered(mouseX, mouseY, itemX, itemY, 16, 16)) {
                graphics.setComponentTooltipForNextFrame(screen.getFont(), createExhaustionTooltip(category), mouseX, mouseY);
            }
        }

        int satX = x + COL_1_X;
        int satY = y + ROW_START_Y + 2 * ROW_HEIGHT;
        renderSaturationSlot(graphics, screen, player, foodData, satX, satY, mouseX, mouseY);
    }

    private static void renderCategorySlot(GuiGraphicsExtractor graphics, InventoryScreen screen, FoodCategories category, ItemStack icon, int itemX, int itemY, int level, float percentage) {
        graphics.item(icon, itemX, itemY);

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX, itemY + TITLE_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.translatable(category.getName()).withColor(category.getColor()), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX + TEXT_OFFSET_X, itemY + TEXT_LINE_1_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.translatable("tooltip.diet.category.level", level).withColor(category.getColor()), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX + TEXT_OFFSET_X, itemY + TEXT_LINE_2_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.literal(percentage == Math.round(percentage) ? String.format(Locale.ROOT, "%.0f%%", percentage * 100f) : String.format(Locale.ROOT, "%.2f%%", percentage * 100f))
                .withColor(category.getColor()), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();
    }

    private static void renderSaturationSlot(GuiGraphicsExtractor graphics, InventoryScreen screen, Player player, ModFoodData foodData, int itemX, int itemY, int mouseX, int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX, itemY + TITLE_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.translatable("tooltip.diet.saturation.title").withColor(CommonColors.WHITE), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food"), itemX, itemY, 16, 16);

        int saturationLevel = foodData != null ? Arrays.stream(foodData.copyLevels()).sum() : 0;
        float saturationValue = player != null ? player.getFoodData().getSaturationLevel() : 0f;

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX + TEXT_OFFSET_X - 3, itemY + TEXT_LINE_1_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.translatable("tooltip.diet.saturation.level", saturationLevel).withColor(CommonColors.WHITE), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX + TEXT_OFFSET_X, itemY + TEXT_LINE_2_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.literal(String.format(Locale.ROOT, "%.1f", saturationValue)).withColor(CommonColors.WHITE), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        if (isHovered(mouseX, mouseY, itemX, itemY, 16, 16)) {
            // TODO : tooltip saturation
            graphics.setComponentTooltipForNextFrame(screen.getFont(), List.of(Component.translatable("tooltip.diet.saturation_desc")), mouseX, mouseY);
        }
    }

    private static boolean isHovered(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public static List<Component> createExhaustionTooltip(FoodCategories category) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.diet.action_eaten").withStyle(ChatFormatting.BLUE));
        lines.add(Component.translatable("tooltip.diet.action_exhaustion").withStyle(ChatFormatting.BLUE));
        for (ActivitiesCategories action : ActivitiesCategories.VALUES) {
            if (action.getRelatedCategory().equals(category)) {
                lines.add(Component.translatable("tooltip.diet.action_categories", Component.translatable(action.getTranslationKey())));
            }
        }
        return lines;
    }
}