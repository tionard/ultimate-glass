package com.github.tionard.ultimateglass.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.pane.PaneGeometry;
import com.github.tionard.ultimateglass.pane.UltimatePane;

/** Crops the standard fluid mesh to the interior side of every pane face in an edge-pane state. */
public final class EdgePaneFluidClipping {
    private static final float PANE_THICKNESS = 2.0F / 16.0F;
    private static final float INNER_FACE_OFFSET = 0.001F;

    private EdgePaneFluidClipping() {
    }

    public static FluidRenderer.Output wrap(
            FluidRenderer.Output output,
            BlockPos pos,
            BlockState state
    ) {
        ClipBounds bounds = ClipBounds.forState(pos, state);
        return layer -> new ClippingVertexConsumer(output.getBuilder(layer), bounds);
    }

    public static ClipBounds localBounds(BlockState state) {
        return ClipBounds.forState(BlockPos.ZERO, state);
    }

    public record ClipBounds(
            float minX,
            float maxX,
            float minY,
            float maxY,
            float minZ,
            float maxZ
    ) {
        private static ClipBounds forState(BlockPos pos, BlockState state) {
            float baseX = pos.getX() & 15;
            float baseY = pos.getY() & 15;
            float baseZ = pos.getZ() & 15;

            float minX = baseX;
            float maxX = baseX + 1.0F;
            float minY = baseY;
            float maxY = baseY + 1.0F;
            float minZ = baseZ;
            float maxZ = baseZ + 1.0F;

            PaneGeometry geometry = ((UltimatePane) state.getBlock()).geometry(state);

            if (geometry.hasEdgePlane(Direction.WEST)) {
                minX += PANE_THICKNESS + INNER_FACE_OFFSET;
            }
            if (geometry.hasEdgePlane(Direction.EAST)) {
                maxX -= PANE_THICKNESS + INNER_FACE_OFFSET;
            }
            if (geometry.hasEdgePlane(Direction.DOWN)) {
                minY += PANE_THICKNESS + INNER_FACE_OFFSET;
            }
            if (geometry.hasEdgePlane(Direction.UP)) {
                maxY -= PANE_THICKNESS + INNER_FACE_OFFSET;
            }
            if (geometry.hasEdgePlane(Direction.NORTH)) {
                minZ += PANE_THICKNESS + INNER_FACE_OFFSET;
            }
            if (geometry.hasEdgePlane(Direction.SOUTH)) {
                maxZ -= PANE_THICKNESS + INNER_FACE_OFFSET;
            }

            return new ClipBounds(minX, maxX, minY, maxY, minZ, maxZ);
        }

        public float clampX(float value) {
            return Math.clamp(value, minX, maxX);
        }

        public float clampY(float value) {
            return Math.clamp(value, minY, maxY);
        }

        public float clampZ(float value) {
            return Math.clamp(value, minZ, maxZ);
        }
    }

    private static final class ClippingVertexConsumer implements VertexConsumer {
        private final VertexConsumer delegate;
        private final ClipBounds bounds;

        private ClippingVertexConsumer(VertexConsumer delegate, ClipBounds bounds) {
            this.delegate = delegate;
            this.bounds = bounds;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            delegate.addVertex(bounds.clampX(x), bounds.clampY(y), bounds.clampZ(z));
            return this;
        }

        @Override
        public VertexConsumer setColor(int red, int green, int blue, int alpha) {
            delegate.setColor(red, green, blue, alpha);
            return this;
        }

        @Override
        public VertexConsumer setColor(int color) {
            delegate.setColor(color);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            delegate.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            delegate.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            delegate.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            delegate.setNormal(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setLineWidth(float width) {
            delegate.setLineWidth(width);
            return this;
        }

        @Override
        public void addVertex(
                float x,
                float y,
                float z,
                int color,
                float u,
                float v,
                int overlay,
                int light,
                float normalX,
                float normalY,
                float normalZ
        ) {
            delegate.addVertex(
                    bounds.clampX(x),
                    bounds.clampY(y),
                    bounds.clampZ(z),
                    color,
                    u,
                    v,
                    overlay,
                    light,
                    normalX,
                    normalY,
                    normalZ
            );
        }
    }
}
