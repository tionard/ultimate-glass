package com.github.tionard.ultimateglass.block.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

/** Supplies a data-driven plank texture to the static pane chunk model. */
public interface PaneFrameSource {
    Identifier frameBlockId();

    default Block frameBlock() {
        return BuiltInRegistries.BLOCK.getOptional(frameBlockId()).orElse(Blocks.OAK_PLANKS);
    }
}
