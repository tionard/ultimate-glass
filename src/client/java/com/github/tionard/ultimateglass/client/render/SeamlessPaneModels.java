package com.github.tionard.ultimateglass.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.client.UltimateGlassClientConfig;

/** Removes frame pieces only where matching Ultimate panes continue the same sheet. */
public final class SeamlessPaneModels {
    private static final float PANE_THICKNESS = 2.0F / 16.0F;
    private static final float CENTER_MIN = 7.0F / 16.0F;
    private static final float CENTER_MAX = 9.0F / 16.0F;
    private static final float EPSILON = 0.0001F;
    private static boolean initialized;

    private SeamlessPaneModels() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModelLoadingPlugin.register(context ->
                context.modifyBlockModelAfterBake().register((model, bakeContext) -> {
                    BlockState state = bakeContext.state();
                    if (state.getBlock() instanceof EdgePaneBlock
                            || state.getBlock() instanceof CenteredPaneBlock) {
                        return new SeamlessPaneModel(model);
                    }
                    return model;
                })
        );
    }

    private static final class SeamlessPaneModel extends WrapperBlockStateModel {
        private SeamlessPaneModel(BlockStateModel wrapped) {
            super(wrapped);
        }

        @Override
        public void emitQuads(
                QuadEmitter emitter,
                BlockAndTintGetter level,
                BlockPos pos,
                BlockState state,
                RandomSource random,
                Predicate<Direction> cullTest
        ) {
            if (!UltimateGlassClientConfig.seamlessConnectedPanes()) {
                super.emitQuads(emitter, level, pos, state, random, cullTest);
                return;
            }

            emitter.pushTransform(quad -> keepQuad(quad, level, pos, state));
            try {
                super.emitQuads(emitter, level, pos, state, random, cullTest);
            } finally {
                emitter.popTransform();
            }
        }

        @Override
        public Object createGeometryKey(
                BlockAndTintGetter level,
                BlockPos pos,
                BlockState state,
                RandomSource random
        ) {
            Object wrappedKey = super.createGeometryKey(level, pos, state, random);
            if (!UltimateGlassClientConfig.seamlessConnectedPanes()) {
                return wrappedKey;
            }
            return new GeometryKey(wrappedKey, continuationMask(level, pos, state));
        }
    }

    private static boolean keepQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state
    ) {
        if (state.getBlock() instanceof EdgePaneBlock) {
            return keepEdgeQuad(quad, level, pos, state);
        }
        if (state.getBlock() instanceof CenteredPaneBlock) {
            return keepCenteredQuad(quad, level, pos, state);
        }
        return true;
    }

    private static boolean keepEdgeQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state
    ) {
        List<Direction> containingPlanes = new ArrayList<>(3);
        for (Direction direction : Direction.values()) {
            if (EdgePaneBlock.hasPaneOnFace(state, direction) && insideFaceSlab(quad, direction)) {
                containingPlanes.add(direction);
            }
        }

        // L- and cube-corner junctions are intentional outside edges, not coplanar seams.
        if (containingPlanes.size() >= 2) {
            return true;
        }

        if (containingPlanes.size() == 1) {
            Direction plane = containingPlanes.getFirst();
            List<Direction> borders = boundaryDirectionsExcept(quad, plane.getAxis());
            return borders.isEmpty() || borders.stream().anyMatch(direction ->
                    !edgeContinues(level, pos, state, direction, List.of(plane))
            );
        }

        return true;
    }

    private static boolean keepCenteredQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state
    ) {
        Direction.Axis axis = state.getValue(CenteredPaneBlock.AXIS);
        if (!insideCenteredSlab(quad, axis)) {
            return true;
        }

        List<Direction> borders = boundaryDirectionsExcept(quad, axis);
        return borders.isEmpty() || borders.stream().anyMatch(direction ->
                !centeredContinues(level, pos, state, direction, axis)
        );
    }

    private static long continuationMask(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        long mask = 0L;
        if (state.getBlock() instanceof EdgePaneBlock) {
            for (Direction plane : Direction.values()) {
                if (!EdgePaneBlock.hasPaneOnFace(state, plane)) {
                    continue;
                }
                for (Direction direction : Direction.values()) {
                    if (direction.getAxis() != plane.getAxis()
                            && edgeContinues(level, pos, state, direction, List.of(plane))) {
                        mask |= 1L << (plane.ordinal() * Direction.values().length + direction.ordinal());
                    }
                }
            }
        } else if (state.getBlock() instanceof CenteredPaneBlock) {
            Direction.Axis axis = state.getValue(CenteredPaneBlock.AXIS);
            for (Direction direction : Direction.values()) {
                if (direction.getAxis() != axis
                        && centeredContinues(level, pos, state, direction, axis)) {
                    mask |= 1L << direction.ordinal();
                }
            }
        }
        return mask;
    }

    private static boolean edgeContinues(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            Direction neighborDirection,
            List<Direction> planes
    ) {
        BlockState neighbor = level.getBlockState(pos.relative(neighborDirection));
        if (neighbor.getBlock() != state.getBlock()) {
            return false;
        }
        return planes.stream().allMatch(plane -> EdgePaneBlock.hasPaneOnFace(neighbor, plane));
    }

    private static boolean centeredContinues(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            Direction neighborDirection,
            Direction.Axis axis
    ) {
        BlockState neighbor = level.getBlockState(pos.relative(neighborDirection));
        return neighbor.getBlock() == state.getBlock()
                && neighbor.getValue(CenteredPaneBlock.AXIS) == axis;
    }

    private static List<Direction> boundaryDirectionsExcept(
            MutableQuadView quad,
            Direction.Axis excludedAxis
    ) {
        List<Direction> directions = new ArrayList<>(2);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != excludedAxis && insideFaceSlab(quad, direction)) {
                directions.add(direction);
            }
        }
        return directions;
    }

    private static boolean insideFaceSlab(MutableQuadView quad, Direction direction) {
        boolean minimum = direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        for (int vertex = 0; vertex < 4; vertex++) {
            float coordinate = coordinate(quad, vertex, direction.getAxis());
            if (minimum ? coordinate > PANE_THICKNESS + EPSILON
                    : coordinate < 1.0F - PANE_THICKNESS - EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static boolean insideCenteredSlab(MutableQuadView quad, Direction.Axis axis) {
        for (int vertex = 0; vertex < 4; vertex++) {
            float coordinate = coordinate(quad, vertex, axis);
            if (coordinate < CENTER_MIN - EPSILON || coordinate > CENTER_MAX + EPSILON) {
                return false;
            }
        }
        return true;
    }

    private static float coordinate(MutableQuadView quad, int vertex, Direction.Axis axis) {
        return switch (axis) {
            case X -> quad.x(vertex);
            case Y -> quad.y(vertex);
            case Z -> quad.z(vertex);
        };
    }

    private record GeometryKey(Object wrapped, long continuations) {
    }
}
