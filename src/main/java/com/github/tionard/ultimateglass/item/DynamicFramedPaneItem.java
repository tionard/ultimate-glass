package com.github.tionard.ultimateglass.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

/** One registry item can carry every modded plank frame while retaining an informative name. */
public final class DynamicFramedPaneItem extends BlockItem {
    private final PaneMaterial material;

    public DynamicFramedPaneItem(Block block, PaneMaterial material, Properties properties) {
        super(block, properties);
        this.material = material;
    }

    @Override
    public Component getName(ItemStack stack) {
        Identifier frameId = stack.getOrDefault(
                UltimateGlassComponents.FRAME_BLOCK,
                DynamicFrameBlockEntity.DEFAULT_FRAME
        );
        Block frame = BuiltInRegistries.BLOCK.getOptional(frameId).orElse(Blocks.OAK_PLANKS);
        ItemStack basePane = new ItemStack(UltimateGlassItems.paneItemFor(material));
        return Component.translatable(
                "item.ultimateglass.dynamic_framed_pane",
                new ItemStack(frame.asItem()).getHoverName(),
                basePane.getHoverName()
        );
    }
}
