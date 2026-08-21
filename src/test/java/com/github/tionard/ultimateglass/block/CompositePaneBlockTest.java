package com.github.tionard.ultimateglass.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import com.github.tionard.ultimateglass.pane.CompositePaneGeometry;
import com.github.tionard.ultimateglass.pane.PanePlane;

final class CompositePaneBlockTest {
    @Test
    void bottomSlabLeavesOnlyUpperPaneHalfExposed() {
        VoxelShape host = Block.box(0, 0, 0, 16, 8, 16);
        VoxelShape exposed = CompositePaneGeometry.exposedPaneShape(host, Direction.NORTH);

        assertFalse(Shapes.joinIsNotEmpty(host, exposed, BooleanOp.AND));
        assertEquals(0.5, exposed.bounds().minY);
        assertEquals(1.0, exposed.bounds().maxY);
    }

    @Test
    void topSlabLeavesOnlyLowerPaneHalfExposed() {
        VoxelShape host = Block.box(0, 8, 0, 16, 16, 16);
        VoxelShape exposed = CompositePaneGeometry.exposedPaneShape(host, Direction.WEST);

        assertFalse(Shapes.joinIsNotEmpty(host, exposed, BooleanOp.AND));
        assertEquals(0.0, exposed.bounds().minY);
        assertEquals(0.5, exposed.bounds().maxY);
    }

    @Test
    void stairShapeClipsBothVerticalLevelsWithoutOverlap() {
        VoxelShape host = Shapes.or(
                Block.box(0, 0, 0, 16, 8, 16),
                Block.box(0, 8, 8, 16, 16, 16)
        );
        VoxelShape exposed = CompositePaneGeometry.exposedPaneShape(host, Direction.NORTH);

        assertFalse(exposed.isEmpty());
        assertFalse(Shapes.joinIsNotEmpty(host, exposed, BooleanOp.AND));
        assertEquals(0.5, exposed.bounds().minY);
        assertEquals(1.0, exposed.bounds().maxY);
    }

    @Test
    void fullyOccupiedStairFaceCannotCreateACompositePane() {
        VoxelShape host = Shapes.or(
                Block.box(0, 0, 0, 16, 8, 16),
                Block.box(0, 8, 8, 16, 16, 16)
        );

        assertTrue(CompositePaneGeometry.exposedPaneShape(host, Direction.SOUTH).isEmpty());
    }

    @Test
    void centeredCompositeKeepsOnlyThePaneVolumeOutsideABottomSlab() {
        VoxelShape host = Block.box(0, 0, 0, 16, 8, 16);
        VoxelShape exposed = CompositePaneGeometry.exposedPaneShape(
                host, Direction.NORTH, true
        );

        assertFalse(exposed.isEmpty());
        assertFalse(Shapes.joinIsNotEmpty(host, exposed, BooleanOp.AND));
        assertEquals(0.5, exposed.bounds().minY);
        assertEquals(7.0 / 16.0, exposed.bounds().minZ);
        assertEquals(9.0 / 16.0, exposed.bounds().maxZ);
    }

    @Test
    void centeredAndEdgeCompositeModesKeepTheSameVerticalOrientation() {
        VoxelShape host = Block.box(0, 0, 0, 16, 8, 16);
        VoxelShape edge = CompositePaneGeometry.exposedPaneShape(host, Direction.WEST, false);
        VoxelShape centered = CompositePaneGeometry.exposedPaneShape(host, Direction.WEST, true);

        assertEquals(0.0, edge.bounds().minX);
        assertEquals(2.0 / 16.0, edge.bounds().maxX);
        assertEquals(7.0 / 16.0, centered.bounds().minX);
        assertEquals(9.0 / 16.0, centered.bounds().maxX);
    }

    @Test
    void edgeRotationSkipsAStairFaceCompletelyHiddenByTheHost() {
        VoxelShape host = Shapes.or(
                Block.box(0, 0, 0, 16, 8, 16),
                Block.box(0, 8, 8, 16, 16, 16)
        );

        assertTrue(CompositePaneGeometry.exposedPaneShape(
                host, Direction.SOUTH, false
        ).isEmpty());
        assertEquals(
                Direction.WEST,
                CompositePaneGeometry.nextAvailableFacing(
                        host, Direction.EAST, false
                )
        );
    }

    @Test
    void centeredRotationStaysPutWhenTheOtherAxisIsFullyHidden() {
        VoxelShape host = PanePlane.centered(Direction.Axis.X).shape();

        assertEquals(
                Direction.NORTH,
                CompositePaneGeometry.nextAvailableFacing(
                        host, Direction.NORTH, true
                )
        );
    }

    @Test
    void compositeRegistrationDisablesFullCubeShapeCaching() throws IOException {
        String source = Files.readString(Path.of(
                "src/main/java/com/github/tionard/ultimateglass/registry/UltimateGlassBlocks.java"
        ));
        assertTrue(source.contains(
                "BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).dynamicShape().setId(key)"
        ));
    }
}
