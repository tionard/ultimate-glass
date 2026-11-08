package com.github.tionard.ultimateglass.client.render;

import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/** Reprojects the plank texture onto already baked, axis-aligned frame geometry. */
final class DynamicFrameTexture {
    private DynamicFrameTexture() {
    }

    static void bake(MutableQuadView quad, TextureAtlasSprite sprite) {
        Direction previousFace = quad.nominalFace();
        // cullFace(null) clears the nominal face in 1.21.11. Without this hint,
        // BAKE_LOCK_UV scales the old atlas UVs instead of replacing them.
        quad.nominalFace(quad.lightFace());
        try {
            quad.spriteBake(sprite, MutableQuadView.BAKE_LOCK_UV);
        } finally {
            quad.nominalFace(previousFace);
        }
    }
}
