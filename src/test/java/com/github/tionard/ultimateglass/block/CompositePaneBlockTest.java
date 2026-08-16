package com.github.tionard.ultimateglass.block;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

final class CompositePaneBlockTest {
    @Test
    void bottomSlabLeavesOnlyUpperPaneHalfExposed() {
        VoxelShape host = Block.box(0, 0, 0, 16, 8, 16);
        VoxelShape exposed = CompositePaneBlock.exposedPaneShape(host, Direction.Axis.Z);

        assertFalse(Shapes.joinIsNotEmpty(host, exposed, BooleanOp.AND));
        assertEquals(0.5, exposed.bounds().minY);
        assertEquals(1.0, exposed.bounds().maxY);
    }

    @Test
    void topSlabLeavesOnlyLowerPaneHalfExposed() {
        VoxelShape host = Block.box(0, 8, 0, 16, 16, 16);
        VoxelShape exposed = CompositePaneBlock.exposedPaneShape(host, Direction.Axis.X);

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
        VoxelShape exposed = CompositePaneBlock.exposedPaneShape(host, Direction.Axis.Z);

        assertFalse(exposed.isEmpty());
        assertFalse(Shapes.joinIsNotEmpty(host, exposed, BooleanOp.AND));
        assertEquals(0.5, exposed.bounds().minY);
        assertEquals(1.0, exposed.bounds().maxY);
    }
}
