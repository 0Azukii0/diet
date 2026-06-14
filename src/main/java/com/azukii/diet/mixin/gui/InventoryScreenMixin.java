package com.azukii.diet.mixin.gui;

import com.azukii.diet.DietMod;
import com.azukii.diet.gui.IDietInventoryScreen;
import com.azukii.diet.gui.screen.FoodStatsRenderState;
import com.azukii.diet.gui.screen.FoodStatsScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin implements IDietInventoryScreen {
    @Unique
    private ImageButton diet$foodStatsButton;

    @Inject(method = "init", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        Screen screen = (Screen)(Object) this;

        if (FoodStatsRenderState.isOpen()) {
            ((AbstractContainerScreenAccessor) this).setLeftPos(self.getLeftPos() + FoodStatsScreen.OVERLAY_WIDTH / 2);
        }

        int bx = ((AbstractContainerScreen<?>)(Object) this).getLeftPos() + 134;
        int by = screen.height / 2 - 22;

        diet$foodStatsButton = new ImageButton(bx, by, 20, 18,
                new WidgetSprites(
                        Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_button"),
                        Identifier.fromNamespaceAndPath(DietMod.MODID, "hud/food_data_button_highlighted")
                ),
                _ -> {
                    FoodStatsRenderState.toggle();
                    Minecraft mc = Minecraft.getInstance();
                    if (FoodStatsRenderState.isOpen()) diet$closeRecipeBook();
                    if (mc.screen != null) mc.screen.init(mc.screen.width, mc.screen.height);
                },
                Component.empty()
        );
        ((ScreenInvoker) this).invokeAddRenderableWidget(diet$foodStatsButton);
    }

    @Inject(method = "onRecipeBookButtonClick", at = @At("HEAD"))
    private void repositionFoodStatsButton(CallbackInfo ci) {
        if (diet$foodStatsButton == null) return;
        AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
        diet$foodStatsButton.setPosition(self.getLeftPos() + 134, ((Screen)(Object) this).height / 2 - 22);
    }

    @Inject(method = "getRecipeBookButtonPosition", at = @At("RETURN"), cancellable = true)
    private void shiftRecipeBookButton(CallbackInfoReturnable<ScreenPosition> cir) {
        if (!FoodStatsRenderState.isOpen()) return;
        ScreenPosition pos = cir.getReturnValue();
        cir.setReturnValue(new ScreenPosition(pos.x() + FoodStatsScreen.OVERLAY_WIDTH / 2, pos.y()));
    }

    @Override
    public void diet$closeRecipeBook() {
        RecipeBookComponent<?> recipeBookComponent = ((AbstractRecipeBookScreenAccessor) this).getRecipeBookComponent();
        if (recipeBookComponent.isVisible()) recipeBookComponent.toggleVisibility();
    }
}
