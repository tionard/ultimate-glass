package com.github.tionard.ultimateglass.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.block.DynamicFramedGlassBlock;
import com.github.tionard.ultimateglass.block.DynamicFramedVanillaPaneBlock;
import com.github.tionard.ultimateglass.block.FramedGlassBlock;
import com.github.tionard.ultimateglass.block.FramedVanillaPaneBlock;
import com.github.tionard.ultimateglass.block.TemperedGlassBlock;
import com.github.tionard.ultimateglass.block.entity.PaneFrameSource;
import com.github.tionard.ultimateglass.glass.GlassFamilyBlock;
import com.github.tionard.ultimateglass.glass.GlassForm;
import com.github.tionard.ultimateglass.glass.GlassVariant;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;

/** Registrations and lookups for complete pane/block and ordinary/tempered glass families. */
public final class UltimateGlassFamilies {
    private static final Map<GlassVariant, Block> BLOCKS_BY_VARIANT = new LinkedHashMap<>();
    private static final Map<Block, GlassVariant> VARIANTS_BY_BLOCK = new LinkedHashMap<>();
    private static final Map<PaneMaterial, Block> VANILLA_BLOCKS = new LinkedHashMap<>();
    private static final Map<PaneMaterial, Block> VANILLA_PANES = new LinkedHashMap<>();
    private static final Map<PaneMaterial, TemperedGlassBlock> TEMPERED_BLOCKS =
            new LinkedHashMap<>();

    static {
        for (PaneMaterial material : PaneMaterial.values()) {
            Block vanillaBlock = vanillaBlock(material.vanillaBlockPath());
            Block vanillaPane = material == PaneMaterial.TINTED
                    ? UltimateGlassBlocks.TINTED_GLASS_PANE
                    : vanillaBlock(material.vanillaPanePath());
            VANILLA_BLOCKS.put(material, vanillaBlock);
            VANILLA_PANES.put(material, vanillaPane);

            registerTemperedBlock(material, vanillaBlock);
            for (PaneFrame frame : PaneFrame.woodFrames()) {
                registerOrdinaryPane(material, vanillaPane, frame);
                registerFramedBlock(material, vanillaBlock, false, frame);
                registerFramedBlock(material, vanillaBlock, true, frame);
            }
            registerOrdinaryPane(material, vanillaPane, PaneFrame.DYNAMIC);
            registerFramedBlock(material, vanillaBlock, false, PaneFrame.DYNAMIC);
            registerFramedBlock(material, vanillaBlock, true, PaneFrame.DYNAMIC);
        }
    }

    private UltimateGlassFamilies() {
    }

    public static void initialize() {
        // Trigger static registration.
    }

    public static Block vanillaBlock(PaneMaterial material) {
        return VANILLA_BLOCKS.get(material);
    }

    public static Block vanillaPane(PaneMaterial material) {
        return VANILLA_PANES.get(material);
    }

    public static TemperedGlassBlock temperedBlock(PaneMaterial material) {
        return TEMPERED_BLOCKS.get(material);
    }

    public static Block blockFor(GlassVariant variant) {
        return BLOCKS_BY_VARIANT.get(variant);
    }

    public static GlassVariant variantFor(Block block) {
        return VARIANTS_BY_BLOCK.get(block);
    }

    public static Collection<Block> registeredBlocks() {
        return VARIANTS_BY_BLOCK.keySet();
    }

    public static Collection<Block> dynamicFrameBlocks() {
        return VARIANTS_BY_BLOCK.entrySet().stream()
                .filter(entry -> entry.getValue().frame().isDynamic())
                .map(Map.Entry::getKey)
                .toList();
    }

    public static Collection<Map.Entry<GlassVariant, Block>> variants() {
        return BLOCKS_BY_VARIANT.entrySet();
    }

    /** Matching full framed blocks share material, tempering state, and exact plank identity. */
    public static boolean matchingFramedBlock(
            BlockGetter level,
            BlockPos firstPos,
            BlockState first,
            BlockPos secondPos,
            BlockState second
    ) {
        if (!(first.getBlock() instanceof GlassFamilyBlock firstGlass)
                || !(second.getBlock() instanceof GlassFamilyBlock secondGlass)) {
            return false;
        }
        GlassVariant firstVariant = firstGlass.glassVariant();
        GlassVariant secondVariant = secondGlass.glassVariant();
        if (firstVariant.form() != GlassForm.BLOCK
                || !firstVariant.isFramed()
                || !firstVariant.equals(secondVariant)) {
            return false;
        }
        if (!firstVariant.frame().isDynamic()) {
            return true;
        }
        return level.getBlockEntity(firstPos) instanceof PaneFrameSource firstFrame
                && level.getBlockEntity(secondPos) instanceof PaneFrameSource secondFrame
                && firstFrame.frameBlockId().equals(secondFrame.frameBlockId());
    }

    public static String itemPath(GlassVariant variant) {
        String sourcePath = variant.form() == GlassForm.PANE
                ? panePath(variant.material())
                : variant.material().vanillaBlockPath();
        String temperedPrefix = variant.tempered() ? "ultimate_" : "";
        if (!variant.isFramed()) {
            return temperedPrefix + sourcePath;
        }
        String framePrefix = variant.frame().isDynamic()
                ? "modded_framed_"
                : variant.frame().path() + "_framed_";
        return framePrefix + temperedPrefix + sourcePath;
    }

    private static void registerTemperedBlock(PaneMaterial material, Block vanillaBlock) {
        GlassVariant variant = new GlassVariant(
                material, GlassForm.BLOCK, true, PaneFrame.NONE
        );
        ResourceKey<Block> key = blockKey(itemPath(variant));
        TemperedGlassBlock block = new TemperedGlassBlock(
                material,
                BlockBehaviour.Properties.ofFullCopy(vanillaBlock).setId(key)
        );
        register(key, block, variant);
        TEMPERED_BLOCKS.put(material, block);
    }

    private static void registerOrdinaryPane(
            PaneMaterial material, Block vanillaPane, PaneFrame frame
    ) {
        GlassVariant variant = new GlassVariant(material, GlassForm.PANE, false, frame);
        ResourceKey<Block> key = blockKey(itemPath(variant));
        Block block = frame.isDynamic()
                ? new DynamicFramedVanillaPaneBlock(
                        variant,
                        BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(key)
                )
                : new FramedVanillaPaneBlock(
                        variant,
                        BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(key)
                );
        register(key, block, variant);
    }

    private static void registerFramedBlock(
            PaneMaterial material, Block vanillaBlock, boolean tempered, PaneFrame frame
    ) {
        GlassVariant variant = new GlassVariant(material, GlassForm.BLOCK, tempered, frame);
        ResourceKey<Block> key = blockKey(itemPath(variant));
        Block block = frame.isDynamic()
                ? new DynamicFramedGlassBlock(
                        variant,
                        BlockBehaviour.Properties.ofFullCopy(vanillaBlock).setId(key)
                )
                : new FramedGlassBlock(
                        variant,
                        BlockBehaviour.Properties.ofFullCopy(vanillaBlock).setId(key)
                );
        register(key, block, variant);
    }

    private static void register(ResourceKey<Block> key, Block block, GlassVariant variant) {
        Registry.register(BuiltInRegistries.BLOCK, key, block);
        BLOCKS_BY_VARIANT.put(variant, block);
        VARIANTS_BY_BLOCK.put(block, variant);
    }

    private static Block vanillaBlock(String path) {
        Identifier id = Identifier.fromNamespaceAndPath("minecraft", path);
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("Missing vanilla block " + id);
        }
        return block;
    }

    private static String panePath(PaneMaterial material) {
        return material == PaneMaterial.TINTED
                ? "tinted_glass_pane"
                : material.vanillaPanePath();
    }

    private static ResourceKey<Block> blockKey(String path) {
        return ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, path)
        );
    }
}
