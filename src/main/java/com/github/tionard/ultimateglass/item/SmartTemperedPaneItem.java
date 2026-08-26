package com.github.tionard.ultimateglass.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.github.tionard.ultimateglass.glass.SmartGlassKind;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** Component-backed tempered pane item shared by every supported glass material. */
public final class SmartTemperedPaneItem extends TemperedPaneItem {
    private final SmartGlassKind kind;

    public SmartTemperedPaneItem(Block defaultBlock, SmartGlassKind kind, Properties properties) {
        super(defaultBlock, properties);
        this.kind = kind;
    }

    @Override
    protected Block placementBlock(ItemStack stack) {
        return UltimateGlassSmartItems.targetBlock(
                kind,
                UltimateGlassSmartItems.material(stack)
        );
    }

    @Override
    public Component getName(ItemStack stack) {
        return SmartGlassNames.name(stack, kind);
    }
}
