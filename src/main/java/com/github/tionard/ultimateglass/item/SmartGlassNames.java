package com.github.tionard.ultimateglass.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.glass.SmartGlassKind;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.registry.UltimateGlassFamilies;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

final class SmartGlassNames {
    private static final String PLANKS_SUFFIX_KEY = "frame.ultimateglass.planks_suffix";

    private SmartGlassNames() {
    }

    static Component name(ItemStack stack, SmartGlassKind kind) {
        PaneMaterial material = UltimateGlassSmartItems.material(stack);
        Component glassName = glassName(material, kind.form(), kind.tempered());
        if (!kind.framed()) {
            return glassName;
        }

        Identifier frameId = stack.getOrDefault(
                UltimateGlassComponents.FRAME_BLOCK,
                DynamicFrameBlockEntity.DEFAULT_FRAME
        );
        Block frame = BuiltInRegistries.BLOCK.getOptional(frameId).orElse(Blocks.OAK_PLANKS);
        Component frameName = FrameDisplayName.withoutPlanksSuffix(
                new ItemStack(frame.asItem()).getHoverName(),
                Component.translatable(PLANKS_SUFFIX_KEY).getString()
        );
        return Component.translatable(
                kind.form() == GlassForm.PANE
                        ? "item.ultimateglass.dynamic_framed_pane"
                        : "item.ultimateglass.dynamic_framed_glass",
                frameName,
                glassName
        );
    }

    private static Component glassName(
            PaneMaterial material, GlassForm form, boolean tempered
    ) {
        if (!tempered) {
            Block vanilla = form == GlassForm.PANE
                    ? UltimateGlassFamilies.vanillaPane(material)
                    : UltimateGlassFamilies.vanillaBlock(material);
            return new ItemStack(vanilla.asItem()).getHoverName();
        }

        if (form == GlassForm.PANE) {
            String legacyPath = UltimateGlassBlocks.familyFor(material).itemPath();
            return Component.translatable("item.ultimateglass." + legacyPath);
        }

        String legacyPath = UltimateGlassFamilies.itemPath(new GlassVariant(
                material, GlassForm.BLOCK, true, PaneFrame.NONE
        ));
        return Component.translatable("block.ultimateglass." + legacyPath);
    }
}
