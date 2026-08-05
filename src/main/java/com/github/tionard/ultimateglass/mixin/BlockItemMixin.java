package com.github.tionard.ultimateglass.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;

@Mixin(BlockItem.class)
abstract class BlockItemMixin {
    @Inject(method = "getPlacementState", at = @At("HEAD"), cancellable = true)
    private void ultimateGlass$placePaneOnEdge(
            BlockPlaceContext context,
            CallbackInfoReturnable<BlockState> callback
    ) {
        BlockItem item = (BlockItem) (Object) this;
        EdgePaneBlock edgePane = UltimateGlassBlocks.edgeFor(item.getBlock());
        if (edgePane != null) {
            callback.setReturnValue(edgePane.getStateForPlacement(context));
        }
    }
}
