package com.azukii.diet.gui.screen;

import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.core.thirst.ThirstData;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Locale;

public class ThirstScreen {

    //renderThirst(graphics, screen, player, x, y);
    private static final float TEXT_SCALE = 0.7f;

    private static void renderThirst(GuiGraphicsExtractor graphics, InventoryScreen screen, Player player, int x, int y) {
        ThirstData thirstData = player != null ? player.getData(ModAttachments.THIRST_DATA) : null;
        float thirstValue = thirstData != null ? thirstData.getThirstValue() : 0.0F;
        int thirstLevel = thirstData != null ? thirstData.getThirstLevel() : 1;

        graphics.item(new ItemStack(Items.POTION), x + 100, y + 90);

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 100, y + 110);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.translatable("tooltip.diet.category.level", thirstLevel),0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();

        graphics.pose().pushMatrix();
        graphics.pose().translate(x + 100, y + 120);
        graphics.pose().scale(TEXT_SCALE, TEXT_SCALE);
        graphics.text(screen.getFont(), Component.literal(String.format(Locale.ROOT, "%.2f%%", thirstValue)),0, 0, 0xFF404040, false);
        graphics.pose().popMatrix();
    }
}
