package com.github.tionard.ultimateglass.client.render;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import org.junit.jupiter.api.Test;

final class DynamicFrameTextureTest {
    // Sprite geometry only: no image pixels, native allocation or GPU is required.
    private static final class TestSprite extends TextureAtlasSprite {
        TestSprite() {
            super(Identifier.withDefaultNamespace("textures/atlas/blocks.png"),
                    new SpriteContents(Identifier.withDefaultNamespace("block/oak_planks"),
                            new FrameSize(16, 16), null),
                    256, 256, 64, 128, 0);
        }
    }

    @Test
    void uncullableFramesUseFullPlankCoordinatesOnEveryOrientation() {
        TextureAtlasSprite sprite = new TestSprite();
        for (Direction face : Direction.values()) {
            for (Direction hint : new Direction[] {null, face, face.getOpposite()}) {
                Quad fixture = new Quad(face, hint);
                MutableQuadView quad = fixture.view();
                DynamicFrameTexture.bake(quad, sprite);
                for (int i = 0; i < 4; i++) {
                    float x = fixture.pos[i][0], y = fixture.pos[i][1], z = fixture.pos[i][2];
                    float u = switch (face) {
                        case DOWN, UP, SOUTH -> x;
                        case NORTH -> 1 - x;
                        case WEST -> z;
                        case EAST -> 1 - z;
                    };
                    float v = switch (face) {
                        case DOWN -> 1 - z;
                        case UP -> z;
                        default -> 1 - y;
                    };
                    assertEquals(sprite.getU(u), fixture.uv[i][0], 0.000001F, face.toString());
                    assertEquals(sprite.getV(v), fixture.uv[i][1], 0.000001F, face.toString());
                }
                assertSame(hint, quad.nominalFace(), "Do not change the caller's face hint");
                assertNull(quad.cullFace(), "Pane interiors must remain uncullable");
                float[][] first = java.util.Arrays.stream(fixture.uv).map(float[]::clone).toArray(float[][]::new);
                DynamicFrameTexture.bake(quad, sprite);
                for (int i = 0; i < 4; i++) {
                    assertArrayEquals(first[i], fixture.uv[i], 0.000001F, "Rebaking must not shrink UVs");
                }
            }
        }
    }

    @Test
    void regressionFixtureReproducesCollapsedUvsWithoutFaceHint() {
        Quad fixture = new Quad(Direction.SOUTH, null);
        fixture.view().spriteBake(new TestSprite(), MutableQuadView.BAKE_LOCK_UV);
        float span = Math.abs(fixture.uv[1][0] - fixture.uv[0][0]);
        assertTrue(span < 0.0001F, "Old code samples a tiny patch rather than the plank strip");
    }

    private static final class Quad {
        private final Direction face;
        private Direction nominal;
        private final float[][] pos = new float[4][3];
        private final float[][] uv = new float[][] {
                {0.7F, 0.6F}, {0.7002F, 0.6F}, {0.7002F, 0.602F}, {0.7F, 0.602F}
        };

        Quad(Direction face, Direction nominal) {
            this.face = face;
            this.nominal = nominal;
            float[][] strip = {{0, 0}, {1F / 16, 0}, {1F / 16, 1}, {0, 1}};
            for (int i = 0; i < 4; i++) {
                pos[i] = switch (face.getAxis()) {
                    case X -> new float[] {0.125F, strip[i][1], strip[i][0]};
                    case Y -> new float[] {strip[i][0], 0.125F, strip[i][1]};
                    case Z -> new float[] {strip[i][0], strip[i][1], 0.125F};
                };
            }
        }

        MutableQuadView view() {
            return (MutableQuadView) Proxy.newProxyInstance(MutableQuadView.class.getClassLoader(),
                    new Class<?>[] {MutableQuadView.class}, (proxy, method, args) -> {
                        if (method.isDefault()) {
                            return InvocationHandler.invokeDefault(proxy, method, args);
                        }
                        return switch (method.getName()) {
                            case "nominalFace" -> {
                                if (args == null || args.length == 0) yield nominal;
                                nominal = (Direction) args[0];
                                yield proxy;
                            }
                            case "lightFace" -> face;
                            case "cullFace" -> null;
                            case "x" -> pos[(int) args[0]][0];
                            case "y" -> pos[(int) args[0]][1];
                            case "z" -> pos[(int) args[0]][2];
                            case "u" -> uv[(int) args[0]][0];
                            case "v" -> uv[(int) args[0]][1];
                            case "uv" -> {
                                uv[(int) args[0]][0] = (float) args[1];
                                uv[(int) args[0]][1] = (float) args[2];
                                yield proxy;
                            }
                            case "atlas" -> proxy;
                            default -> throw new AssertionError("Unexpected quad method: " + method.getName());
                        };
                    });
        }
    }
}
