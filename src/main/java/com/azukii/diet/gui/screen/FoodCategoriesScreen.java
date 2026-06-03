package com.azukii.diet.gui.screen;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.FoodRegistry;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.data.ModFoodData;
import com.azukii.diet.profile.FoodProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class FoodCategoriesScreen extends Screen {
    private static final Identifier PROGRESS_BAR_BACK = Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_progress_bar_back");
    private static final Identifier PROGRESS_BAR_FRONT = Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_progress_bar_front");
    private static final Identifier BACKGROUND = Identifier.fromNamespaceAndPath(DietMod.MODID, "textures/gui/food_categories.png");
    private static final int OVERLAY_WIDTH = 193;
    private static final int OVERLAY_HEIGHT = 141;
    private static final int BAR_WIDTH = 100;
    private static final int BAR_HEIGHT = 5;
    private static final int PADDING_TOP = 30;
    private static final int LABEL_OFFSET_X = 5;
    private static final int BAR_OFFSET_X = 70;
    private static final float TEXT_SCALE = 0.7f;
    private static final List<BarDef> BAR_DEFS = List.of(
            new BarDef("diet.category.fruit",       new ItemStack(Items.APPLE),         FoodCategories.FRUIT,       0xFFD11C52),
            new BarDef("diet.category.grain",       new ItemStack(Items.WHEAT),         FoodCategories.GRAIN,       0xFFD1A11C),
            new BarDef("diet.category.protein",     new ItemStack(Items.COOKED_BEEF),   FoodCategories.PROTEIN,     0xFFD1771C),
            new BarDef("diet.category.vegetable",   new ItemStack(Items.CARROT),        FoodCategories.VEGETABLE,   0xFF30D11C),
            new BarDef("diet.category.sugar",       new ItemStack(Items.SUGAR),         FoodCategories.SUGAR,       0xFFFC8DE6)
    );
    private record BarDef(String translationKey, ItemStack icon, FoodCategories category, int color) {}
    private final InventoryScreen parentScreen;

    public FoodCategoriesScreen(InventoryScreen parentScreen) {
        super(Component.translatable("screen.dietmod.food_categories"));
        this.parentScreen = parentScreen;
    }

    @Override
    protected void init() {
        super.init();
        int guiLeft = (this.width  - OVERLAY_WIDTH)  / 2;
        int guiTop  = (this.height - OVERLAY_HEIGHT) / 2;

        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.dietmod.close"),
                _ -> onClose()
        ).size(80, 14).pos(guiLeft + (OVERLAY_WIDTH - 80) / 2, guiTop + OVERLAY_HEIGHT - 20).build());
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int x = (this.width  - OVERLAY_WIDTH)  / 2;
        int y = (this.height - OVERLAY_HEIGHT) / 2;
        graphics.blit(RenderPipelines.GUI_TEXTURED, BACKGROUND, x, y, 0f, 0f, OVERLAY_WIDTH, OVERLAY_HEIGHT, 256, 256);
        float blurRadius = (float)this.minecraft.options.getMenuBackgroundBlurriness();
        if (blurRadius >= 1.0F) {
            graphics.blurBeforeThisStratum();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        int guiLeft = (this.width  - OVERLAY_WIDTH) / 2;
        int guiTop  = (this.height - OVERLAY_HEIGHT) / 2;

        renderTitle(graphics, guiLeft, guiTop);
        renderDietBars(graphics, guiLeft, guiTop);
    }

    private void renderTitle(GuiGraphicsExtractor graphics, int guiLeft, int guiTop) {
        Component title = Component.translatable("screen.dietmod.food_categories");
        int x = guiLeft + (OVERLAY_WIDTH - this.font.width(title)) / 2;
        graphics.text(this.font, title, x, guiTop + 6, 0xFF404040, false);
    }
    private void renderDietBars(GuiGraphicsExtractor graphics, int guiLeft, int guiTop) {
        Player player = Minecraft.getInstance().player;
        ModFoodData modFoodData = player != null ? player.getData(ModAttachments.DIET_DATA) : null;
        FoodProfile maxProfile = FoodRegistry.getMaxValues();

        int labelX = guiLeft + LABEL_OFFSET_X;
        int barX = guiLeft + BAR_OFFSET_X;
        int startY = guiTop  + PADDING_TOP;
        //int count = BAR_DEFS.size();
        float usable = (OVERLAY_HEIGHT - PADDING_TOP - 40 - BAR_HEIGHT);
        //float spacing = count > 1 ? usable / (count - 1) : 0f;

        long i = 0;
        for (FoodCategories category : FoodCategories.VALUES) {
            int barY = Math.round(startY + i * 20);
            float value = modFoodData != null ? modFoodData.get(category) : 0f;
            float max = maxProfile.get(category);
            float pct = max > 0f ? Math.clamp(value / max, 0f, 1f) : 0f;
            Component label = Component.translatable(category.getName());
            i++;
            renderBar(graphics, label, new ItemStack(category.getItem()), labelX, barX, barY, pct, category.getColor());
        }
    }

    private void renderBar(GuiGraphicsExtractor graphics, Component label, ItemStack icon, int labelX, int barX, int y, float percentage, int color) {
        graphics.item(icon, labelX, y - 6);

        graphics.pose().pushMatrix();
        graphics.pose().translate(labelX + 18, y - 1);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(this.font, label, 0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_BAR_BACK, barX, y, BAR_WIDTH, BAR_HEIGHT);

        int filledWidth = Math.round(BAR_WIDTH * percentage);
        if (filledWidth > 0) {
            graphics.enableScissor(barX, y, barX + filledWidth, y + BAR_HEIGHT);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PROGRESS_BAR_FRONT, barX, y, BAR_WIDTH, BAR_HEIGHT, color);
            graphics.disableScissor();
        }

        graphics.pose().pushMatrix();
        graphics.pose().translate(barX + BAR_WIDTH + 4, y - 1);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        renderOutlinedText(graphics, String.format("%.0f%%", percentage * 100), 0, 0, color);
        graphics.pose().popMatrix();
    }

    private void renderOutlinedText(GuiGraphicsExtractor graphics, String text, int x, int y, int color) {
        graphics.text(this.font, text, x - 1, y,0xFF000000, false);
        graphics.text(this.font, text, x + 1, y,0xFF000000, false);
        graphics.text(this.font, text, x,y - 1,0xFF000000, false);
        graphics.text(this.font, text, x,y + 1,0xFF000000, false);
        graphics.text(this.font, text, x, y, color,false);
    }
}