package com.github.tionard.ultimateglass.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.client.render.PaneAwareFluidRenderView;

/** Routes edge-pane fluids through the vanilla-compatible mesh hook, including under Sodium. */
@Mixin(FluidRenderingImpl.class)
abstract class FabricFluidRenderingMixin {
    @Inject(method = "renderDefault", at = @At("HEAD"), cancellable = true)
    private static void ultimateGlass$renderBoundedPaneFluid(
            FluidRenderer renderer,
            FluidRenderHandler handler,
            BlockAndTintGetter level,
            BlockPos pos,
            FluidRenderer.Output output,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo callback
    ) {
        if (!(blockState.getBlock() instanceof EdgePaneBlock)
                || !blockState.getValue(EdgePaneBlock.WATERLOGGED)) {
            return;
        }

        BlockAndTintGetter paneAwareLevel = new PaneAwareFluidRenderView(
                level,
                pos.immutable(),
                blockState
        );
        FluidRenderingImpl.renderVanillaDefault(
                renderer,
                paneAwareLevel,
                pos,
                output,
                blockState,
                fluidState
        );
        callback.cancel();
    }
}
