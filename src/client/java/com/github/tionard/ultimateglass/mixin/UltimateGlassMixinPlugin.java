package com.github.tionard.ultimateglass.mixin;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import net.fabricmc.loader.api.FabricLoader;

/** Selects the native clipping hook for the active chunk renderer. */
public final class UltimateGlassMixinPlugin implements IMixinConfigPlugin {
    private static final String FABRIC_DEFAULT_MIXIN = ".FluidRenderingImplMixin";
    private static final String SODIUM_MIXIN = ".SodiumDefaultFluidRendererMixin";

    private boolean sodiumLoaded;

    @Override
    public void onLoad(String mixinPackage) {
        sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.endsWith(FABRIC_DEFAULT_MIXIN)) {
            return !sodiumLoaded;
        }
        if (mixinClassName.endsWith(SODIUM_MIXIN)) {
            return sodiumLoaded;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }

    @Override
    public void postApply(
            String targetClassName,
            ClassNode targetClass,
            String mixinClassName,
            IMixinInfo mixinInfo
    ) {
    }
}
