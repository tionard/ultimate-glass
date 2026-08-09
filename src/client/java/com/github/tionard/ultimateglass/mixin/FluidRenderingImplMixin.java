package com.github.tionard.ultimateglass.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.client.render.EdgePaneFluidClipping;

/** Routes edge-pane water through the normal fluid tessellator with block-local clipping. */
@Mixin(FluidRenderingImpl.class)
public abstract class FluidRenderingImplMixin {
    @Inject(method = "renderDefault", at = @At("HEAD"), cancellable = true)
    private static void ultimateGlass$clipEdgePaneWater(
            FluidRenderer renderer,
            FluidRenderHandler handler,
            BlockAndTintGetter level,
            BlockPos pos,
            FluidRenderer.Output output,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo callback
    ) {
        if (!(blockState.getBlock() instanceof EdgePaneBlock) || !fluidState.is(FluidTags.WATER)) {
            return;
        }

        FluidRenderingImpl.renderVanillaDefault(
                renderer,
                level,
                pos,
                EdgePaneFluidClipping.wrap(output, pos, blockState),
                blockState,
                fluidState
        );
        callback.cancel();
    }
}
