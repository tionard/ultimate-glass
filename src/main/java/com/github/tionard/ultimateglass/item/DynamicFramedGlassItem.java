package com.github.tionard.ultimateglass.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;

/** One registry item carries every modded-plank frame for one ordinary/full family member. */
public final class DynamicFramedGlassItem extends BlockItem {
    private static final String PLANKS_SUFFIX_KEY = "frame.ultimateglass.planks_suffix";

    private final Item baseItem;

    public DynamicFramedGlassItem(Block block, Item baseItem, Properties properties) {
        super(block, properties);
        this.baseItem = baseItem;
    }

    @Override
    public Component getName(ItemStack stack) {
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
                "item.ultimateglass.dynamic_framed_glass",
                frameName,
                new ItemStack(baseItem).getHoverName()
        );
    }
}
