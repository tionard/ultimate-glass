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
import com.github.tionard.ultimateglass.pane.PaneConnectionQueries;
import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.PanePlane;
import com.github.tionard.ultimateglass.pane.UltimatePane;

/** Removes frame pieces only where matching Ultimate panes continue the same sheet. */
public final class SeamlessPaneModels {
    private static final float PANE_THICKNESS = 2.0F / 16.0F;
    private static final float CENTER_MIN = 7.0F / 16.0F;
    private static final float CENTER_MAX = 9.0F / 16.0F;
    private static final float EPSILON = 0.0001F;
    private static final int SEAM_FILL_TINT_INDEX = 15;
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
            boolean seamless = UltimateGlassClientConfig.seamlessConnectedPanes();
            emitter.pushTransform(quad -> seamless
                    ? keepQuad(quad, level, pos, state)
                    : !isSeamFillQuad(quad));
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
        boolean seamFill = isSeamFillQuad(quad);
        if (seamFill) {
            // Tint index is only a baked-model marker. Clear it so the texture renders unchanged.
            quad.tintIndex(-1);
        }
        if (state.getBlock() instanceof EdgePaneBlock) {
            return keepEdgeQuad(quad, level, pos, state, seamFill);
        }
        if (state.getBlock() instanceof CenteredPaneBlock) {
            return keepCenteredQuad(quad, level, pos, state, seamFill);
        }
        return !seamFill;
    }

    private static boolean keepEdgeQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            boolean seamFill
    ) {
        PaneGeometry geometry = ((UltimatePane) state.getBlock()).geometry(state);
        List<PanePlane> containingPlanes = new ArrayList<>(3);
        for (PanePlane plane : geometry.planes()) {
            if (plane.isEdge() && insideFaceSlab(quad, plane.edgeDirection())) {
                containingPlanes.add(plane);
            }
        }

        // L- and cube-corner junctions are intentional outside edges, not coplanar seams.
        if (containingPlanes.size() >= 2) {
            return !seamFill;
        }

        if (containingPlanes.size() == 1) {
            PanePlane plane = containingPlanes.getFirst();
            List<Direction> borders = boundaryDirectionsExcept(quad, plane.axis());
            return keepBoundarySection(
                    seamFill, borders, level, pos, state, plane);
        }

        return !seamFill;
    }

    private static boolean keepCenteredQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            boolean seamFill
    ) {
        PaneGeometry geometry = ((UltimatePane) state.getBlock()).geometry(state);
        List<PanePlane> containingPlanes = new ArrayList<>(3);
        for (PanePlane plane : geometry.planes()) {
            if (plane.isCentered() && insideCenteredSlab(quad, plane.axis())) {
                containingPlanes.add(plane);
            }
        }

        // Shared pair/triple junction geometry is intentional and must keep its frame surfaces.
        if (containingPlanes.size() >= 2) {
            return !seamFill;
        }

        if (containingPlanes.size() == 1) {
            PanePlane plane = containingPlanes.getFirst();
            List<Direction> borders = boundaryDirectionsExcept(quad, plane.axis());
            return keepBoundarySection(seamFill, borders, level, pos, state, plane);
        }

        return !seamFill;
    }

    private static boolean keepBoundarySection(
            boolean seamFill,
            List<Direction> borders,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            PanePlane plane
    ) {
        if (borders.isEmpty()) {
            return !seamFill;
        }

        boolean everyBorderContinues = borders.stream().allMatch(direction ->
                PaneConnectionQueries.hasMatchingContinuation(
                        level, pos, state, direction, plane)
        );
        return seamFill == everyBorderContinues;
    }

    private static long continuationMask(BlockAndTintGetter level, BlockPos pos, BlockState state) {
        long mask = 0L;
        if (state.getBlock() instanceof UltimatePane pane) {
            PaneGeometry geometry = pane.geometry(state);
            for (PanePlane plane : geometry.planes()) {
                for (Direction direction : Direction.values()) {
                    if (PaneConnectionQueries.hasMatchingContinuation(
                            level, pos, state, direction, plane)) {
                        int bit = plane.ordinal() * Direction.values().length
                                + direction.ordinal();
                        mask |= 1L << bit;
                    }
                }
            }
        }
        return mask;
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

    /** Generated seam replacements carry an explicit marker that survives model baking. */
    private static boolean isSeamFillQuad(MutableQuadView quad) {
        return quad.tintIndex() == SEAM_FILL_TINT_INDEX;
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
