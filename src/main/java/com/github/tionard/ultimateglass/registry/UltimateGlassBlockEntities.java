package com.github.tionard.ultimateglass.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.block.entity.CompositePaneBlockEntity;
import com.github.tionard.ultimateglass.block.entity.PaneSeamBlockEntity;

public final class UltimateGlassBlockEntities {
    public static final BlockEntityType<PaneSeamBlockEntity> PANE_SEAMS = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "pane_seams"),
            FabricBlockEntityTypeBuilder.create(
                    PaneSeamBlockEntity::new,
                    UltimateGlassBlocks.staticPaneBlocks().toArray(
                            net.minecraft.world.level.block.Block[]::new
                    )
            ).build()
    );

    public static final BlockEntityType<CompositePaneBlockEntity> COMPOSITE_PANE = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "composite_pane"),
            FabricBlockEntityTypeBuilder.create(
                    CompositePaneBlockEntity::new,
                    UltimateGlassBlocks.COMPOSITE_PANE
            ).build()
    );

    public static final BlockEntityType<DynamicFrameBlockEntity> DYNAMIC_FRAME = Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "dynamic_pane_frame"),
            FabricBlockEntityTypeBuilder.create(
                    DynamicFrameBlockEntity::new,
                    UltimateGlassBlocks.dynamicFrameBlocks().toArray(net.minecraft.world.level.block.Block[]::new)
            ).build()
    );

    private UltimateGlassBlockEntities() {
    }

    public static void initialize() {
        // Trigger static registration.
    }
}
