package com.github.tionard.ultimateglass.seam;

import net.minecraft.core.BlockPos;

/** The opposite side of the same block-boundary seam in the neighboring cell. */
public record PaneSeamCounterpart(BlockPos pos, PaneSeamTarget target) {
    public static PaneSeamCounterpart of(BlockPos sourcePos, PaneSeamTarget sourceTarget) {
        return new PaneSeamCounterpart(
                sourcePos.relative(sourceTarget.boundary()),
                new PaneSeamTarget(
                        sourceTarget.plane(),
                        sourceTarget.boundary().getOpposite()
                )
        );
    }
}
