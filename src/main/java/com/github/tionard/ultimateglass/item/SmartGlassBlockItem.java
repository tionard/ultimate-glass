package com.github.tionard.ultimateglass.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.glass.SmartGlassKind;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** Selects a material-specific internal block from one component-backed stack item. */
public final class SmartGlassBlockItem extends BlockItem {
    private final SmartGlassKind kind;

    public SmartGlassBlockItem(Block defaultBlock, SmartGlassKind kind, Properties properties) {
        super(defaultBlock, properties);
        this.kind = kind;
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        Block target = UltimateGlassSmartItems.targetBlock(
                kind,
                UltimateGlassSmartItems.material(context.getItemInHand())
        );
        BlockState state = target == null ? null : target.getStateForPlacement(context);
        return state != null && canPlace(context, state) ? state : null;
    }

    @Override
    public Component getName(ItemStack stack) {
        return SmartGlassNames.name(stack, kind);
    }
}
