package com.azukii.diet.client.gui.screen;

import com.azukii.diet.DietMod;
import com.azukii.diet.registry.FoodDataCategories;
import com.azukii.diet.registry.FoodDataMixinAccess;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
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

        for (int i = 0; i < FoodDataCategories.VALUES.length; i++) {
            FoodDataCategories category = FoodDataCategories.VALUES[i];
            int row = i / 2;
            int col = i % 2;
            int itemX = x + (col == 0 ? COL_0_X : COL_1_X);
            int itemY = y + ROW_START_Y + row * ROW_HEIGHT;

            if (!(Minecraft.getInstance().getCameraEntity() instanceof Player player)) return;
            FoodData foodData = player.getFoodData();
            if (foodData instanceof FoodDataMixinAccess access) {
                renderCategorySlot(graphics, screen, category, new ItemStack(category.getItem()), itemX, itemY, access.diet$getCategorySaturationLevels(category));
            }

            if (isHovered(mouseX, mouseY, itemX, itemY, 42, 16)) {
                graphics.setComponentTooltipForNextFrame(screen.getFont(), createExhaustionTooltip(category), mouseX, mouseY);
            }

            int satX = x + COL_1_X;
            int satY = y + ROW_START_Y + 2 * ROW_HEIGHT;
            renderSaturationSlot(graphics, screen, foodData, satX, satY, mouseX, mouseY);
        }
    }

    private static void renderCategorySlot(GuiGraphicsExtractor graphics, InventoryScreen screen, FoodDataCategories category, ItemStack icon, int itemX, int itemY, float saturation) {
        graphics.item(icon, itemX, itemY);

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX, itemY + TITLE_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), category.getDisplayName(), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX + TEXT_OFFSET_X, itemY + TEXT_LINE_2_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.literal(String.format(Locale.ROOT, "%.1f", saturation)).withColor(category.getColor()), 5, -5, 0xFF404040, false);
        graphics.pose().popMatrix();
    }

    private static void renderSaturationSlot(GuiGraphicsExtractor graphics, InventoryScreen screen, FoodData foodData, int itemX, int itemY, int mouseX, int mouseY) {
        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX, itemY + TITLE_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.translatable("tooltip.diet.saturation.title").withColor(CommonColors.WHITE), 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food"), itemX, itemY, 16, 16);

        graphics.pose().pushMatrix();
        graphics.pose().translate(itemX + TEXT_OFFSET_X, itemY + TEXT_LINE_2_Y);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.literal(String.format(Locale.ROOT, "%.1f", foodData.getSaturationLevel())).withColor(CommonColors.WHITE), 5, -5, 0xFF404040, false);
        graphics.pose().popMatrix();

        if (isHovered(mouseX, mouseY, itemX, itemY, 42, 16)) {
            List<FormattedCharSequence> tooltip = screen.getFont().split(
                    Component.translatable("tooltip.diet.saturation.desc"),
                    150
            );
            graphics.setTooltipForNextFrame(screen.getFont(), tooltip, mouseX, mouseY);
        }
    }

    private static boolean isHovered(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }

    public static List<Component> createExhaustionTooltip(FoodDataCategories category) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.diet.activity_title").withStyle(ChatFormatting.BLUE));
        lines.add(Component.translatable("tooltip.diet.activity_desc"));
        for (FoodDataCategories.Activity activity : FoodDataCategories.Activity.VALUES) {
            if (activity.getRelatedCategory().equals(category)) {
                lines.add(Component.translatable("tooltip.diet.activity_categories", activity.getDisplayName()).withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }
}