package com.github.tionard.ultimateglass.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.github.tionard.ultimateglass.pane.PaneFrame;

/** Shared display-name behavior for fixed-wood ordinary panes and full glass blocks. */
public final class StaticFramedGlassItem extends BlockItem {
    private final PaneFrame frame;
    private final Item baseItem;

    public StaticFramedGlassItem(
            Block block, PaneFrame frame, Item baseItem, Properties properties
    ) {
        super(block, properties);
        this.frame = frame;
        this.baseItem = baseItem;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(
                "item.ultimateglass.framed_glass",
                Component.translatable("frame.ultimateglass." + frame.path()),
                new ItemStack(baseItem).getHoverName()
        );
    }
}
