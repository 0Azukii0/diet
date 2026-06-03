package com.azukii.diet.mixin;

import com.azukii.diet.activity.ActivitiesCategories;
import com.azukii.diet.activity.ActivityEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {
    @Inject(at = @At("TAIL"), method = "playerWillDestroy")
    protected void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player, CallbackInfoReturnable<BlockState> cir) {
        ActivityEvents.exhaustionReductionShortSheen(player, ActivitiesCategories.MINE);
    }

    @Inject(at = @At("TAIL"), method = "playerDestroy")
    protected void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, BlockEntity blockEntity, ItemStack destroyedWith, CallbackInfo ci) {
        float ratio = ActivityEvents.exhaustionReductionLongSheen(player, ActivitiesCategories.MINE);
        player.causeFoodExhaustion(0.005F * ratio);
    }
}
