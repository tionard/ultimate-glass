package com.github.tionard.ultimateglass.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

public final class StaticFramedPaneItem extends TemperedPaneItem {
    private final PaneFrame frame;
    private final PaneMaterial material;

    public StaticFramedPaneItem(
            Block block,
            PaneFrame frame,
            PaneMaterial material,
            Properties properties
    ) {
        super(block, properties);
        this.frame = frame;
        this.material = material;
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(
                "item.ultimateglass.framed_pane",
                Component.translatable("frame.ultimateglass." + frame.path()),
                new ItemStack(UltimateGlassItems.paneItemFor(material)).getHoverName()
        );
    }
}
