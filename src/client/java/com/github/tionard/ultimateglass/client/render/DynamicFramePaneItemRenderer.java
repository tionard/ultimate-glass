package com.github.tionard.ultimateglass.client.render;

import java.util.function.Consumer;

import org.joml.Vector3f;
import org.joml.Vector3fc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.CuboidItemModelWrapper;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.item.SpecialModelWrapper;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import net.fabricmc.fabric.api.client.model.loading.v1.ModelModifier;

import com.github.tionard.ultimateglass.block.entity.DynamicFrameBlockEntity;
import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.glass.SmartGlassKind;
import com.github.tionard.ultimateglass.pane.PaneMaterial;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.registry.UltimateGlassFamilies;
import com.github.tionard.ultimateglass.registry.UltimateGlassFamilyItems;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;
import com.github.tionard.ultimateglass.registry.UltimateGlassSmartItems;

/** Renders component-backed glass and frames without registering every material combination. */
final class DynamicFramePaneItemRenderer
        implements SpecialModelRenderer<DynamicFramePaneItemRenderer.RenderMaterials> {
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float DEPTH = 2.0F / 16.0F;

    private final PaneMaterial fixedMaterial;
    private final boolean fullBlock;
    private final boolean framed;

    private DynamicFramePaneItemRenderer(
            PaneMaterial fixedMaterial,
            boolean fullBlock,
            boolean framed
    ) {
        this.fixedMaterial = fixedMaterial;
        this.fullBlock = fullBlock;
        this.framed = framed;
    }

    static ItemModel wrap(
            ItemModel original,
            ModelModifier.AfterBakeItem.Context context
    ) {
        GlassVariant completeVariant = UltimateGlassFamilyItems.dynamicVariant(context.itemId());
        PaneMaterial legacyPaneMaterial = UltimateGlassItems.dynamicFrameMaterial(context.itemId());
        SmartGlassKind smartKind = UltimateGlassSmartItems.kind(context.itemId());
        if (legacyPaneMaterial == null && completeVariant == null && smartKind == null) {
            return original;
        }
        if (!(context.sourceModel() instanceof CuboidItemModelWrapper.Unbaked source)) {
            return original;
        }

        ResolvedModel resolved = context.bakingContext().blockModelBaker().getModel(source.model());
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(
                context.bakingContext().blockModelBaker(),
                resolved,
                resolved.getTopTextureSlots()
        );
        return new SpecialModelWrapper<>(
                new DynamicFramePaneItemRenderer(
                        completeVariant != null
                                ? completeVariant.material()
                                : legacyPaneMaterial,
                        smartKind != null
                                ? smartKind.form() == GlassForm.BLOCK
                                : completeVariant != null
                                        && completeVariant.form() == GlassForm.BLOCK,
                        smartKind == null || smartKind.framed()
                ),
                properties,
                context.transformation()
        );
    }

    @Override
    public void submit(
            RenderMaterials materials,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int light,
            int overlay,
            boolean foil,
            int outline
    ) {
        TextureAtlasSprite paneSprite = materials.glass().sprite();
        collector.order(0).submitCustomGeometry(
                poseStack,
                RenderTypes.itemTranslucent(paneSprite.atlasLocation()),
                (pose, vertices) -> {
                    renderGlassGeometry(pose, vertices, paneSprite, light, overlay);
                }
        );
        TextureAtlasSprite frameSprite = materials.frame() == null
                ? null
                : materials.frame().sprite();
        if (framed && frameSprite != null) {
            collector.order(1).submitCustomGeometry(
                    poseStack,
                    RenderTypes.itemCutout(frameSprite.atlasLocation()),
                    (pose, vertices) -> {
                        if (fullBlock) {
                            renderBlockFrame(pose, vertices, frameSprite, light, overlay);
                        } else {
                            renderFrame(pose, vertices, frameSprite, light, overlay);
                        }
                    }
            );
        }
        if (foil) {
            collector.order(2).submitCustomGeometry(
                    poseStack,
                    RenderTypes.glint(),
                    (pose, vertices) -> {
                        renderGlassGeometry(pose, vertices, paneSprite, light, overlay);
                        if (fullBlock) {
                            if (framed && frameSprite != null) {
                                renderBlockFrame(pose, vertices, frameSprite, light, overlay);
                            }
                        } else {
                            if (framed && frameSprite != null) {
                                renderFrame(pose, vertices, frameSprite, light, overlay);
                            }
                        }
                    }
            );
        }
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (float x : new float[] {0.0F, 1.0F}) {
            for (float y : new float[] {0.0F, 1.0F}) {
                for (float z : new float[] {0.0F, fullBlock ? 1.0F : DEPTH}) {
                    output.accept(new Vector3f(x, y, z));
                }
            }
        }
    }

    @Override
    public RenderMaterials extractArgument(ItemStack stack) {
        PaneMaterial material = fixedMaterial == null
                ? UltimateGlassSmartItems.material(stack)
                : fixedMaterial;
        Block glass = fullBlock
                ? UltimateGlassFamilies.vanillaBlock(material)
                : UltimateGlassFamilies.vanillaPane(material);
        Material.Baked glassMaterial = Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .getParticleMaterial(glass.defaultBlockState());
        if (!framed) {
            return new RenderMaterials(glassMaterial, null);
        }

        Identifier frameId = stack.getOrDefault(
                UltimateGlassComponents.FRAME_BLOCK,
                DynamicFrameBlockEntity.DEFAULT_FRAME
        );
        Block frame = BuiltInRegistries.BLOCK.getOptional(frameId).orElse(Blocks.OAK_PLANKS);
        Material.Baked frameMaterial = Minecraft.getInstance()
                .getModelManager()
                .getBlockStateModelSet()
                .getParticleMaterial(frame.defaultBlockState());
        return new RenderMaterials(glassMaterial, frameMaterial);
    }

    record RenderMaterials(Material.Baked glass, Material.Baked frame) {
    }

    private void renderGlassGeometry(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            int light,
            int overlay
    ) {
        if (!framed) {
            emitCuboid(
                    pose, vertices, sprite,
                    0.0F, 0.0F, 0.0F,
                    1.0F, 1.0F, fullBlock ? 1.0F : DEPTH,
                    light, overlay
            );
        } else if (fullBlock) {
            renderGlassBlock(pose, vertices, sprite, light, overlay);
        } else {
            renderGlass(pose, vertices, sprite, light, overlay);
        }
    }

    private static void renderGlass(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            int light,
            int overlay
    ) {
        emitNorthSouthFaces(
                pose, vertices, sprite,
                PIXEL, PIXEL, 0.0F,
                1.0F - PIXEL, 1.0F - PIXEL, DEPTH,
                light, overlay
        );
    }

    private static void renderFrame(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            int light,
            int overlay
    ) {
        emitCuboid(pose, vertices, sprite, 0.0F, 0.0F, 0.0F,
                1.0F, PIXEL, DEPTH, light, overlay);
        emitCuboid(pose, vertices, sprite, 0.0F, 1.0F - PIXEL, 0.0F,
                1.0F, 1.0F, DEPTH, light, overlay);
        emitCuboid(pose, vertices, sprite, 0.0F, PIXEL, 0.0F,
                PIXEL, 1.0F - PIXEL, DEPTH, light, overlay);
        emitCuboid(pose, vertices, sprite, 1.0F - PIXEL, PIXEL, 0.0F,
                1.0F, 1.0F - PIXEL, DEPTH, light, overlay);
    }

    private static void renderGlassBlock(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            int light,
            int overlay
    ) {
        float high = 1.0F - PIXEL;
        emitNorthSouthFaces(
                pose, vertices, sprite,
                PIXEL, PIXEL, 0.0F,
                high, high, 1.0F,
                light, overlay
        );
        emitQuad(pose, vertices, sprite, light, overlay, -1, 0, 0,
                0.0F, PIXEL, high, high, high,
                0.0F, high, high, high, PIXEL,
                0.0F, high, PIXEL, PIXEL, PIXEL,
                0.0F, PIXEL, PIXEL, PIXEL, high);
        emitQuad(pose, vertices, sprite, light, overlay, 1, 0, 0,
                1.0F, PIXEL, PIXEL, PIXEL, high,
                1.0F, high, PIXEL, PIXEL, PIXEL,
                1.0F, high, high, high, PIXEL,
                1.0F, PIXEL, high, high, high);
        emitQuad(pose, vertices, sprite, light, overlay, 0, -1, 0,
                PIXEL, 0.0F, high, PIXEL, high,
                PIXEL, 0.0F, PIXEL, PIXEL, PIXEL,
                high, 0.0F, PIXEL, high, PIXEL,
                high, 0.0F, high, high, high);
        emitQuad(pose, vertices, sprite, light, overlay, 0, 1, 0,
                PIXEL, 1.0F, high, PIXEL, high,
                high, 1.0F, high, high, high,
                high, 1.0F, PIXEL, high, PIXEL,
                PIXEL, 1.0F, PIXEL, PIXEL, PIXEL);
    }

    private static void renderBlockFrame(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            int light,
            int overlay
    ) {
        float high = 1.0F - PIXEL;
        for (float y : new float[] {0.0F, high}) {
            for (float z : new float[] {0.0F, high}) {
                emitCuboid(pose, vertices, sprite, 0.0F, y, z,
                        1.0F, y + PIXEL, z + PIXEL, light, overlay);
            }
        }
        for (float x : new float[] {0.0F, high}) {
            for (float z : new float[] {0.0F, high}) {
                emitCuboid(pose, vertices, sprite, x, PIXEL, z,
                        x + PIXEL, high, z + PIXEL, light, overlay);
            }
        }
        for (float x : new float[] {0.0F, high}) {
            for (float y : new float[] {0.0F, high}) {
                emitCuboid(pose, vertices, sprite, x, y, PIXEL,
                        x + PIXEL, y + PIXEL, high, light, overlay);
            }
        }
    }

    private static void emitCuboid(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            int light,
            int overlay
    ) {
        emitNorthSouthFaces(pose, vertices, sprite, x0, y0, z0, x1, y1, z1, light, overlay);

        emitQuad(pose, vertices, sprite, light, overlay, -1, 0, 0,
                x0, y0, z1, z1, 1 - y0,
                x0, y1, z1, z1, 1 - y1,
                x0, y1, z0, z0, 1 - y1,
                x0, y0, z0, z0, 1 - y0);
        emitQuad(pose, vertices, sprite, light, overlay, 1, 0, 0,
                x1, y0, z0, z0, 1 - y0,
                x1, y1, z0, z0, 1 - y1,
                x1, y1, z1, z1, 1 - y1,
                x1, y0, z1, z1, 1 - y0);
        emitQuad(pose, vertices, sprite, light, overlay, 0, -1, 0,
                x0, y0, z1, x0, z1,
                x0, y0, z0, x0, z0,
                x1, y0, z0, x1, z0,
                x1, y0, z1, x1, z1);
        emitQuad(pose, vertices, sprite, light, overlay, 0, 1, 0,
                x0, y1, z1, x0, z1,
                x1, y1, z1, x1, z1,
                x1, y1, z0, x1, z0,
                x0, y1, z0, x0, z0);
    }

    private static void emitNorthSouthFaces(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            int light,
            int overlay
    ) {
        emitQuad(pose, vertices, sprite, light, overlay, 0, 0, -1,
                x0, y0, z0, x0, 1 - y0,
                x0, y1, z0, x0, 1 - y1,
                x1, y1, z0, x1, 1 - y1,
                x1, y0, z0, x1, 1 - y0);
        emitQuad(pose, vertices, sprite, light, overlay, 0, 0, 1,
                x1, y0, z1, 1 - x1, 1 - y0,
                x1, y1, z1, 1 - x1, 1 - y1,
                x0, y1, z1, 1 - x0, 1 - y1,
                x0, y0, z1, 1 - x0, 1 - y0);
    }

    private static void emitQuad(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            int light,
            int overlay,
            float normalX,
            float normalY,
            float normalZ,
            float x0, float y0, float z0, float u0, float v0,
            float x1, float y1, float z1, float u1, float v1,
            float x2, float y2, float z2, float u2, float v2,
            float x3, float y3, float z3, float u3, float v3
    ) {
        emitVertex(pose, vertices, sprite, light, overlay,
                normalX, normalY, normalZ, x0, y0, z0, u0, v0);
        emitVertex(pose, vertices, sprite, light, overlay,
                normalX, normalY, normalZ, x1, y1, z1, u1, v1);
        emitVertex(pose, vertices, sprite, light, overlay,
                normalX, normalY, normalZ, x2, y2, z2, u2, v2);
        emitVertex(pose, vertices, sprite, light, overlay,
                normalX, normalY, normalZ, x3, y3, z3, u3, v3);
    }

    private static void emitVertex(
            PoseStack.Pose pose,
            VertexConsumer vertices,
            TextureAtlasSprite sprite,
            int light,
            int overlay,
            float normalX,
            float normalY,
            float normalZ,
            float x,
            float y,
            float z,
            float u,
            float v
    ) {
        vertices.addVertex(pose, x, y, z)
                .setColor(0xFFFFFFFF)
                .setUv(sprite.getU(u), sprite.getV(v))
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, normalX, normalY, normalZ);
    }
}
