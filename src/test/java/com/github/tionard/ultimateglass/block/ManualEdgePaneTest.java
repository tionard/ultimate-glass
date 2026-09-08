package com.github.tionard.ultimateglass.block;

import static org.junit.jupiter.api.Assertions.*;

import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.mojang.serialization.Lifecycle;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

final class ManualEdgePaneTest {
    private static EdgePaneBlock pane;
    private static MappedRegistry<Block> registry;
    private static final Identifier ID = Identifier.fromNamespaceAndPath("ultimateglass", "test_edge");

    @BeforeAll
    static void initialize() throws ReflectiveOperationException {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        // Plain JUnit has no Fabric registry-unfreeze hook. Temporarily allow the
        // Block constructor to allocate its holder, without registering or changing
        // any vanilla entries. Restore both fields even if construction fails.
        var frozen = MappedRegistry.class.getDeclaredField("frozen");
        var holders = MappedRegistry.class.getDeclaredField("unregisteredIntrusiveHolders");
        frozen.setAccessible(true);
        holders.setAccessible(true);
        Object previousHolders = holders.get(BuiltInRegistries.BLOCK);
        boolean previousFrozen = frozen.getBoolean(BuiltInRegistries.BLOCK);
        try {
            frozen.setBoolean(BuiltInRegistries.BLOCK, false);
            holders.set(BuiltInRegistries.BLOCK, new java.util.IdentityHashMap<>());
            pane = new EdgePaneBlock(Blocks.GLASS_PANE,
                    new PaneAppearance(PaneMaterial.CLEAR, PaneFrame.NONE),
                    Block.Properties.of().setId(ResourceKey.create(Registries.BLOCK, ID)));
        } finally {
            holders.set(BuiltInRegistries.BLOCK, previousHolders);
            frozen.setBoolean(BuiltInRegistries.BLOCK, previousFrozen);
        }
        registry = new MappedRegistry<>(Registries.BLOCK, Lifecycle.stable());
        registry.register(ResourceKey.create(Registries.BLOCK, ID), pane, RegistrationInfo.BUILT_IN);
        registry.freeze();
    }

    @Test
    void everyFaceSetAddsExactlyOnePanePerFreeFaceAndRotatesWithoutLoss() {
        for (int mask = 1; mask < 64; mask++) {
            Direction primary = Direction.values()[Integer.numberOfTrailingZeros(mask)];
            var state = pane.defaultBlockState().setValue(EdgePaneBlock.FACING, primary)
                    .setValue(EdgePaneBlock.WATERLOGGED, true);
            for (Direction face : Direction.values()) {
                if ((mask & (1 << face.ordinal())) != 0) {
                    state = EdgePaneBlock.addPane(state, face);
                }
            }
            assertEquals(Integer.bitCount(mask), EdgePaneBlock.paneCount(state));
            for (Direction face : Direction.values()) {
                boolean occupied = (mask & (1 << face.ordinal())) != 0;
                assertEquals(occupied, EdgePaneBlock.hasPaneOnFace(state, face));
                var added = EdgePaneBlock.addPane(state, face);
                assertEquals(Integer.bitCount(mask) + (occupied ? 0 : 1), EdgePaneBlock.paneCount(added));
                assertTrue(added.getValue(EdgePaneBlock.WATERLOGGED));
            }
            for (Direction.Axis axis : Direction.Axis.values()) {
                var rotated = EdgePaneBlock.rotateAssembly(state, axis);
                assertEquals(pane.geometry(state).rotateAround(axis).planes(), pane.geometry(rotated).planes());
                for (int turn = 1; turn < 4; turn++) {
                    rotated = EdgePaneBlock.rotateAssembly(rotated, axis);
                }
                assertEquals(state, rotated);
            }
            assertSame(state, pane.withConnections(state, null, null));
            CompoundTag saved = new CompoundTag();
            saved.putString("Name", ID.toString());
            CompoundTag properties = new CompoundTag();
            properties.putString("facing", state.getValue(EdgePaneBlock.FACING).getSerializedName());
            for (var property : java.util.List.of(EdgePaneBlock.WATERLOGGED, EdgePaneBlock.CONNECT_TOP,
                    EdgePaneBlock.CONNECT_BOTTOM, EdgePaneBlock.CONNECT_LEFT, EdgePaneBlock.CONNECT_RIGHT,
                    EdgePaneBlock.CONNECT_OPPOSITE)) {
                properties.putString(property.getName(), state.getValue(property).toString());
            }
            saved.put("Properties", properties);
            assertSame(state, NbtUtils.readBlockState(registry, saved));
        }
    }

    @Test
    void oldSavedCornerFlagsLoadAsCountedPanesWithoutOppositeProperty() {
        for (Direction facing : Direction.values()) {
            for (int mask = 0; mask < 16; mask++) {
                CompoundTag properties = new CompoundTag();
                properties.putString("facing", facing.getSerializedName());
                properties.putString("waterlogged", "true");
                properties.putString("connect_top", Boolean.toString((mask & 1) != 0));
                properties.putString("connect_bottom", Boolean.toString((mask & 2) != 0));
                properties.putString("connect_left", Boolean.toString((mask & 4) != 0));
                properties.putString("connect_right", Boolean.toString((mask & 8) != 0));
                CompoundTag saved = new CompoundTag();
                saved.putString("Name", ID.toString());
                saved.put("Properties", properties);
                var loaded = NbtUtils.readBlockState(registry, saved);
                assertSame(pane, loaded.getBlock());
                assertEquals(1 + Integer.bitCount(mask), EdgePaneBlock.paneCount(loaded));
                assertFalse(loaded.getValue(EdgePaneBlock.CONNECT_OPPOSITE));
                assertTrue(loaded.getValue(EdgePaneBlock.WATERLOGGED));
                assertEquals(facing, loaded.getValue(EdgePaneBlock.FACING));
            }
        }
    }
}
