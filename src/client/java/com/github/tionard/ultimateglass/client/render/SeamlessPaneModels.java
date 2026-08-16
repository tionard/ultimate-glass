package com.github.tionard.ultimateglass.client.render;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.model.loading.v1.wrapper.WrapperBlockStateModel;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.CompositePaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.block.DynamicFramedPane;
import com.github.tionard.ultimateglass.block.entity.CompositePaneBlockEntity;
import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.block.entity.PaneFrameSource;
import com.github.tionard.ultimateglass.client.UltimateGlassClientConfig;
import com.github.tionard.ultimateglass.pane.PaneConnectionQueries;
import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.PanePlane;
import com.github.tionard.ultimateglass.pane.PaneSeamPolicy;
import com.github.tionard.ultimateglass.pane.UltimatePane;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;

/** Removes frame pieces only where matching Ultimate panes continue the same sheet. */
public final class SeamlessPaneModels {
    private static final float PANE_THICKNESS = 2.0F / 16.0F;
    private static final float CENTER_MIN = 7.0F / 16.0F;
    private static final float CENTER_MAX = 9.0F / 16.0F;
    private static final float EPSILON = 0.0001F;
    private static final int FRAMED_SEAM_FILL_TINT_INDEX = 11;
    private static final int DYNAMIC_FRAMED_SURFACE_TINT_INDEX = 12;
    private static final int FRAMED_SURFACE_TINT_INDEX = 13;
    private static final int SEAM_FILL_TINT_INDEX = 15;
    private static final int DYNAMIC_FRAME_TINT_INDEX = 14;
    private static boolean initialized;

    private SeamlessPaneModels() {
    }

    public static void initialize() {
        if (initialized) {
            return;
        }
        initialized = true;

        ModelLoadingPlugin.register(context -> {
            context.modifyBlockModelAfterBake().register((model, bakeContext) -> {
                BlockState state = bakeContext.state();
                if (state.getBlock() instanceof EdgePaneBlock
                        || state.getBlock() instanceof CenteredPaneBlock) {
                    return new SeamlessPaneModel(model);
                }
                if (state.getBlock() instanceof CompositePaneBlock) {
                    return new CompositePaneModel(model);
                }
                return model;
            });
            context.modifyItemModelAfterBake().register(DynamicFramePaneItemRenderer::wrap);
        });
    }

    /** Emits both stored models into one cached chunk mesh; no BlockEntityRenderer is involved. */
    private static final class CompositePaneModel extends WrapperBlockStateModel {
        private CompositePaneModel(BlockStateModel wrapped) {
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
            if (!(level.getBlockEntity(pos) instanceof CompositePaneBlockEntity composite)
                    || composite.hostState().isAir()) {
                super.emitQuads(emitter, level, pos, state, random, cullTest);
                return;
            }

            BlockState paneState = paneState(composite, state);
            var models = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
            models.get(composite.hostState()).emitQuads(
                    emitter, level, pos, composite.hostState(), random, cullTest
            );
            models.get(paneState).emitQuads(
                    emitter, level, pos, paneState, random, cullTest
            );
        }

        @Override
        public Object createGeometryKey(
                BlockAndTintGetter level,
                BlockPos pos,
                BlockState state,
                RandomSource random
        ) {
            if (!(level.getBlockEntity(pos) instanceof CompositePaneBlockEntity composite)) {
                return super.createGeometryKey(level, pos, state, random);
            }
            return new CompositeGeometryKey(
                    composite.hostState(),
                    composite.appearance(),
                    composite.paneAxis(),
                    composite.frameBlockId(),
                    state.getValue(CompositePaneBlock.WATERLOGGED)
            );
        }
    }

    private static BlockState paneState(CompositePaneBlockEntity composite, BlockState state) {
        UltimateGlassBlocks.PaneFamily family = UltimateGlassBlocks.familyFor(
                composite.appearance()
        );
        if (family == null) {
            family = UltimateGlassBlocks.familyFor(com.github.tionard.ultimateglass.pane.PaneMaterial.CLEAR);
        }
        return family.centeredPane().defaultBlockState()
                .setValue(CenteredPaneBlock.AXIS, composite.paneAxis())
                .setValue(
                        CenteredPaneBlock.WATERLOGGED,
                        state.getValue(CompositePaneBlock.WATERLOGGED)
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
            Material.Baked frameMaterial = dynamicFrameMaterial(level, pos, state);
            emitter.pushTransform(quad -> transformQuad(
                    quad, level, pos, state, seamless, frameMaterial
            ));
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
            Object frameBlock = dynamicFrameId(level, pos, state);
            if (!UltimateGlassClientConfig.seamlessConnectedPanes()) {
                return frameBlock == null
                        ? wrappedKey
                        : new GeometryKey(wrappedKey, 0L, frameBlock);
            }
            return new GeometryKey(
                    wrappedKey,
                    continuationMask(level, pos, state),
                    frameBlock
            );
        }
    }

    private static boolean transformQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            boolean seamless,
            Material.Baked frameMaterial
    ) {
        int marker = quad.tintIndex();
        boolean dynamicFrame = marker == DYNAMIC_FRAME_TINT_INDEX
                || marker == DYNAMIC_FRAMED_SURFACE_TINT_INDEX;
        boolean seamFill = marker == SEAM_FILL_TINT_INDEX
                || marker == FRAMED_SEAM_FILL_TINT_INDEX;
        boolean framedSurface = marker == FRAMED_SURFACE_TINT_INDEX
                || marker == DYNAMIC_FRAMED_SURFACE_TINT_INDEX
                || marker == FRAMED_SEAM_FILL_TINT_INDEX;
        if (isModelMarker(marker)) {
            // These tint values are baked-model metadata, never a request for item/block tinting.
            quad.tintIndex(-1);
        }
        boolean keep = seamless
                ? keepQuad(quad, level, pos, state, seamFill, framedSurface)
                : !seamFill;
        if (keep && dynamicFrame) {
            if (frameMaterial != null) {
                quad.materialBake(frameMaterial, MutableQuadView.BAKE_LOCK_UV);
            }
        }
        return keep;
    }

    private static boolean keepQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            boolean seamFill,
            boolean framedSurface
    ) {
        if (state.getBlock() instanceof EdgePaneBlock) {
            return keepEdgeQuad(quad, level, pos, state, seamFill, framedSurface);
        }
        if (state.getBlock() instanceof CenteredPaneBlock) {
            return keepCenteredQuad(quad, level, pos, state, seamFill, framedSurface);
        }
        return !seamFill;
    }

    private static boolean keepEdgeQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            boolean seamFill,
            boolean framedSurface
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
            boolean preservePerpendicularOuterEdge = preservesPerpendicularOuterEdge(
                    quad, state, plane, framedSurface
            );
            List<Direction> borders = boundaryDirectionsExcept(
                    quad,
                    plane.axis(),
                    preservePerpendicularOuterEdge ? 1.0F / 16.0F : PANE_THICKNESS
            );
            return keepBoundarySection(
                    seamFill,
                    preservePerpendicularOuterEdge,
                    borders,
                    level,
                    pos,
                    state,
                    plane
            );
        }

        return !seamFill;
    }

    private static boolean keepCenteredQuad(
            MutableQuadView quad,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            boolean seamFill,
            boolean framedSurface
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
            boolean preservePerpendicularOuterEdge = preservesPerpendicularOuterEdge(
                    quad, state, plane, framedSurface
            );
            List<Direction> borders = boundaryDirectionsExcept(
                    quad,
                    plane.axis(),
                    preservePerpendicularOuterEdge ? 1.0F / 16.0F : PANE_THICKNESS
            );
            return keepBoundarySection(
                    seamFill,
                    preservePerpendicularOuterEdge,
                    borders,
                    level,
                    pos,
                    state,
                    plane
            );
        }

        return !seamFill;
    }

    private static boolean keepBoundarySection(
            boolean seamFill,
            boolean preservePerpendicularOuterEdge,
            List<Direction> borders,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state,
            PanePlane plane
    ) {
        if (borders.isEmpty()) {
            return !seamFill;
        }

        long continuingBorders = borders.stream().filter(direction ->
                PaneConnectionQueries.hasMatchingContinuation(
                        level, pos, state, direction, plane)
        ).count();
        boolean replaceWithGlass = PaneSeamPolicy.shouldReplaceBoundary(
                preservePerpendicularOuterEdge,
                borders.size(),
                (int) continuingBorders
        );
        return seamFill == replaceWithGlass;
    }

    private static boolean preservesPerpendicularOuterEdge(
            MutableQuadView quad,
            BlockState state,
            PanePlane plane,
            boolean framedSurface
    ) {
        if (framedSurface) {
            return true;
        }
        // Plain glass broad faces use the one-pixel outline from the glass texture. Side faces and
        // framed inner bands still occupy the full two-pixel pane thickness and must disappear as
        // soon as their matching continuation exists.
        return state.getBlock() instanceof UltimatePane pane
                && !pane.appearance().isFramed()
                && quad.lightFace().getAxis() == plane.axis();
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

    private static Material.Baked dynamicFrameMaterial(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state
    ) {
        if (!(state.getBlock() instanceof DynamicFramedPane)
                || !(level.getBlockEntity(pos) instanceof PaneFrameSource frame)) {
            return null;
        }
        return Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .getParticleMaterial(frame.frameBlock().defaultBlockState());
    }

    private static Object dynamicFrameId(
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState state
    ) {
        return state.getBlock() instanceof DynamicFramedPane
                && level.getBlockEntity(pos) instanceof PaneFrameSource frame
                ? frame.frameBlockId()
                : null;
    }

    private static List<Direction> boundaryDirectionsExcept(
            MutableQuadView quad,
            Direction.Axis excludedAxis,
            float thickness
    ) {
        List<Direction> directions = new ArrayList<>(2);
        for (Direction direction : Direction.values()) {
            if (direction.getAxis() != excludedAxis
                    && insideFaceSlab(quad, direction, thickness)) {
                directions.add(direction);
            }
        }
        return directions;
    }

    private static boolean insideFaceSlab(MutableQuadView quad, Direction direction) {
        return insideFaceSlab(quad, direction, PANE_THICKNESS);
    }

    private static boolean insideFaceSlab(
            MutableQuadView quad,
            Direction direction,
            float thickness
    ) {
        boolean minimum = direction.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        for (int vertex = 0; vertex < 4; vertex++) {
            float coordinate = coordinate(quad, vertex, direction.getAxis());
            if (minimum ? coordinate > thickness + EPSILON
                    : coordinate < 1.0F - thickness - EPSILON) {
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

    private static boolean isModelMarker(int tintIndex) {
        return tintIndex >= FRAMED_SEAM_FILL_TINT_INDEX
                && tintIndex <= SEAM_FILL_TINT_INDEX;
    }

    private static float coordinate(MutableQuadView quad, int vertex, Direction.Axis axis) {
        return switch (axis) {
            case X -> quad.x(vertex);
            case Y -> quad.y(vertex);
            case Z -> quad.z(vertex);
        };
    }

    private record GeometryKey(Object wrapped, long continuations, Object frameBlock) {
    }

    private record CompositeGeometryKey(
            BlockState hostState,
            com.github.tionard.ultimateglass.pane.PaneAppearance appearance,
            Direction.Axis paneAxis,
            Object frameBlock,
            boolean waterlogged
    ) {
    }
}
