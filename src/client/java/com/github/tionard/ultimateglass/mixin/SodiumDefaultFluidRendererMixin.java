package com.github.tionard.ultimateglass.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.client.render.EdgePaneFluidClipping;

/** Clips edge-pane water inside Sodium's native fluid renderer so Iris retains water materials. */
@Mixin(
        targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer",
        remap = false
)
public abstract class SodiumDefaultFluidRendererMixin {
    @Unique
    private static final ThreadLocal<EdgePaneFluidClipping.ClipBounds> ULTIMATE_GLASS_BOUNDS =
            new ThreadLocal<>();

    @ModifyVariable(method = "render", at = @At("HEAD"), argsOnly = true, ordinal = 0)
    private BlockState ultimateGlass$beginEdgePaneClip(BlockState state) {
        if (state.getBlock() instanceof EdgePaneBlock) {
            ULTIMATE_GLASS_BOUNDS.set(EdgePaneFluidClipping.localBounds(state));
        } else {
            ULTIMATE_GLASS_BOUNDS.remove();
        }
        return state;
    }

    @Inject(method = "render", at = @At("RETURN"))
    private void ultimateGlass$endEdgePaneClip(CallbackInfo callback) {
        ULTIMATE_GLASS_BOUNDS.remove();
    }

    @ModifyArg(
            method = "setVertex",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;setX(IF)V"
            ),
            index = 1
    )
    private static float ultimateGlass$clipX(float x) {
        EdgePaneFluidClipping.ClipBounds bounds = ULTIMATE_GLASS_BOUNDS.get();
        return bounds == null ? x : bounds.clampX(x);
    }

    @ModifyArg(
            method = "setVertex",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;setY(IF)V"
            ),
            index = 1
    )
    private static float ultimateGlass$clipY(float y) {
        EdgePaneFluidClipping.ClipBounds bounds = ULTIMATE_GLASS_BOUNDS.get();
        return bounds == null ? y : bounds.clampY(y);
    }

    @ModifyArg(
            method = "setVertex",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/caffeinemc/mods/sodium/client/model/quad/ModelQuadViewMutable;setZ(IF)V"
            ),
            index = 1
    )
    private static float ultimateGlass$clipZ(float z) {
        EdgePaneFluidClipping.ClipBounds bounds = ULTIMATE_GLASS_BOUNDS.get();
        return bounds == null ? z : bounds.clampZ(z);
    }
}
