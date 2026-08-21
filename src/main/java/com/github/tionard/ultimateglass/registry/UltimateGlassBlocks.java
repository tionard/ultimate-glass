package com.github.tionard.ultimateglass.registry;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;

import com.github.tionard.ultimateglass.UltimateGlass;
import com.github.tionard.ultimateglass.block.CenteredPaneBlock;
import com.github.tionard.ultimateglass.block.CompositePaneBlock;
import com.github.tionard.ultimateglass.block.DynamicFramedCenteredPaneBlock;
import com.github.tionard.ultimateglass.block.DynamicFramedEdgePaneBlock;
import com.github.tionard.ultimateglass.block.EdgePaneBlock;
import com.github.tionard.ultimateglass.block.TintedGlassPaneBlock;
import com.github.tionard.ultimateglass.pane.PaneAppearance;
import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;

public final class UltimateGlassBlocks {
    private static final Map<Block, PaneFamily> FAMILIES_BY_VANILLA = new LinkedHashMap<>();
    private static final Map<Block, PaneFamily> FAMILIES_BY_BLOCK = new LinkedHashMap<>();
    private static final Map<PaneAppearance, PaneFamily> FAMILIES_BY_APPEARANCE = new LinkedHashMap<>();
    private static final Map<PaneMaterial, PaneFamily> DYNAMIC_FAMILIES = new LinkedHashMap<>();

    /** Connected, centered-geometry-free tinted pane used as the cooking input. */
    public static final TintedGlassPaneBlock TINTED_GLASS_PANE = registerTintedGlassPane();
    /** Data-backed stair/slab host used only after a tempered pane is installed. */
    public static final CompositePaneBlock COMPOSITE_PANE = registerCompositePane();

    public static final EdgePaneBlock EDGE_GLASS_PANE = registerStatic(PaneMaterial.CLEAR, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_WHITE_STAINED_GLASS_PANE = registerStatic(PaneMaterial.WHITE_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_ORANGE_STAINED_GLASS_PANE = registerStatic(PaneMaterial.ORANGE_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_MAGENTA_STAINED_GLASS_PANE = registerStatic(PaneMaterial.MAGENTA_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_LIGHT_BLUE_STAINED_GLASS_PANE = registerStatic(PaneMaterial.LIGHT_BLUE_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_YELLOW_STAINED_GLASS_PANE = registerStatic(PaneMaterial.YELLOW_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_LIME_STAINED_GLASS_PANE = registerStatic(PaneMaterial.LIME_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_PINK_STAINED_GLASS_PANE = registerStatic(PaneMaterial.PINK_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_GRAY_STAINED_GLASS_PANE = registerStatic(PaneMaterial.GRAY_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_LIGHT_GRAY_STAINED_GLASS_PANE = registerStatic(PaneMaterial.LIGHT_GRAY_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_CYAN_STAINED_GLASS_PANE = registerStatic(PaneMaterial.CYAN_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_PURPLE_STAINED_GLASS_PANE = registerStatic(PaneMaterial.PURPLE_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_BLUE_STAINED_GLASS_PANE = registerStatic(PaneMaterial.BLUE_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_BROWN_STAINED_GLASS_PANE = registerStatic(PaneMaterial.BROWN_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_GREEN_STAINED_GLASS_PANE = registerStatic(PaneMaterial.GREEN_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_RED_STAINED_GLASS_PANE = registerStatic(PaneMaterial.RED_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_BLACK_STAINED_GLASS_PANE = registerStatic(PaneMaterial.BLACK_STAINED, PaneFrame.NONE).edgePane();
    public static final EdgePaneBlock EDGE_TINTED_GLASS_PANE = registerStatic(PaneMaterial.TINTED, PaneFrame.NONE).edgePane();

    static {
        for (PaneFrame frame : PaneFrame.woodFrames()) {
            for (PaneMaterial material : PaneMaterial.values()) {
                registerStatic(material, frame);
            }
        }
        for (PaneMaterial material : PaneMaterial.values()) {
            registerDynamic(material);
        }
    }

    private UltimateGlassBlocks() {
    }

    public static void initialize() {
        // Trigger static registration.
    }

    public static EdgePaneBlock edgeFor(Block vanillaPane) {
        PaneFamily family = FAMILIES_BY_VANILLA.get(vanillaPane);
        return family == null ? null : family.edgePane();
    }

    public static CenteredPaneBlock centeredFor(Block vanillaPane) {
        PaneFamily family = FAMILIES_BY_VANILLA.get(vanillaPane);
        return family == null ? null : family.centeredPane();
    }

    public static PaneFamily familyFor(Block block) {
        return FAMILIES_BY_BLOCK.get(block);
    }

    public static PaneFamily familyFor(PaneMaterial material) {
        return FAMILIES_BY_APPEARANCE.get(new PaneAppearance(material));
    }

    public static PaneFamily familyFor(PaneAppearance appearance) {
        return appearance.frame().isDynamic()
                ? DYNAMIC_FAMILIES.get(appearance.material())
                : FAMILIES_BY_APPEARANCE.get(appearance);
    }

    public static PaneFamily dynamicFamily(PaneMaterial material) {
        return DYNAMIC_FAMILIES.get(material);
    }

    public static PaneAppearance appearanceFor(Block block) {
        PaneFamily family = FAMILIES_BY_BLOCK.get(block);
        return family == null ? null : family.appearance();
    }

    public static Block vanillaFor(Block customPane) {
        PaneFamily family = FAMILIES_BY_BLOCK.get(customPane);
        return family == null || customPane == family.vanillaPane() ? null : family.vanillaPane();
    }

    public static Collection<EdgePaneBlock> edgePanes() {
        return FAMILIES_BY_APPEARANCE.values().stream().map(PaneFamily::edgePane).toList();
    }

    public static Collection<CenteredPaneBlock> centeredPanes() {
        return FAMILIES_BY_APPEARANCE.values().stream().map(PaneFamily::centeredPane).toList();
    }

    public static Collection<PaneFamily> paneFamilies() {
        return FAMILIES_BY_APPEARANCE.values();
    }

    public static Collection<Block> dynamicFrameBlocks() {
        return DYNAMIC_FAMILIES.values().stream()
                .flatMap(family -> java.util.stream.Stream.of(family.edgePane(), family.centeredPane()))
                .map(Block.class::cast)
                .toList();
    }

    private static TintedGlassPaneBlock registerTintedGlassPane() {
        ResourceKey<Block> key = blockKey("tinted_glass_pane");
        TintedGlassPaneBlock block = new TintedGlassPaneBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.TINTED_GLASS).setId(key)
        );
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static CompositePaneBlock registerCompositePane() {
        ResourceKey<Block> key = blockKey("composite_pane");
        CompositePaneBlock block = new CompositePaneBlock(
                BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).dynamicShape().setId(key)
        );
        return Registry.register(BuiltInRegistries.BLOCK, key, block);
    }

    private static Block vanillaBlock(String path) {
        Identifier id = Identifier.fromNamespaceAndPath("minecraft", path);
        Block block = BuiltInRegistries.BLOCK.getValue(id);
        if (block == null || block == Blocks.AIR) {
            throw new IllegalStateException("Missing vanilla block " + id);
        }
        return block;
    }

    private static Block sourcePane(PaneMaterial material) {
        return switch (material) {
            case CLEAR -> Blocks.GLASS_PANE;
            case TINTED -> TINTED_GLASS_PANE;
            default -> vanillaBlock(material.vanillaPanePath());
        };
    }

    private static PaneFamily registerStatic(PaneMaterial material, PaneFrame frame) {
        Block vanillaPane = sourcePane(material);
        PaneAppearance appearance = new PaneAppearance(material, frame);
        String panePath = panePath(material);
        String framePrefix = frame == PaneFrame.NONE ? "" : frame.path() + "_framed_";
        String edgeName = "edge_" + framePrefix + panePath;
        String centeredName = "centered_" + framePrefix + panePath;

        ResourceKey<Block> centeredKey = blockKey(centeredName);
        CenteredPaneBlock centeredPane = new CenteredPaneBlock(
                vanillaPane, appearance,
                BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(centeredKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, centeredKey, centeredPane);

        ResourceKey<Block> edgeKey = blockKey(edgeName);
        EdgePaneBlock edgePane = new EdgePaneBlock(
                vanillaPane, appearance,
                BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(edgeKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, edgeKey, edgePane);

        PaneFamily family = new PaneFamily(vanillaPane, centeredPane, edgePane, appearance);
        if (frame == PaneFrame.NONE && material != PaneMaterial.TINTED) {
            FAMILIES_BY_VANILLA.put(vanillaPane, family);
            FAMILIES_BY_BLOCK.put(vanillaPane, family);
        }
        FAMILIES_BY_BLOCK.put(centeredPane, family);
        FAMILIES_BY_BLOCK.put(edgePane, family);
        FAMILIES_BY_APPEARANCE.put(appearance, family);
        return family;
    }

    private static PaneFamily registerDynamic(PaneMaterial material) {
        Block vanillaPane = sourcePane(material);
        PaneAppearance appearance = new PaneAppearance(material, PaneFrame.DYNAMIC);
        String panePath = panePath(material);
        String centeredName = "centered_modded_framed_" + panePath;
        String edgeName = "edge_modded_framed_" + panePath;

        ResourceKey<Block> centeredKey = blockKey(centeredName);
        CenteredPaneBlock centeredPane = new DynamicFramedCenteredPaneBlock(
                vanillaPane, appearance,
                BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(centeredKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, centeredKey, centeredPane);

        ResourceKey<Block> edgeKey = blockKey(edgeName);
        EdgePaneBlock edgePane = new DynamicFramedEdgePaneBlock(
                vanillaPane, appearance,
                BlockBehaviour.Properties.ofFullCopy(vanillaPane).setId(edgeKey)
        );
        Registry.register(BuiltInRegistries.BLOCK, edgeKey, edgePane);

        PaneFamily family = new PaneFamily(vanillaPane, centeredPane, edgePane, appearance);
        FAMILIES_BY_BLOCK.put(centeredPane, family);
        FAMILIES_BY_BLOCK.put(edgePane, family);
        FAMILIES_BY_APPEARANCE.put(appearance, family);
        DYNAMIC_FAMILIES.put(material, family);
        return family;
    }

    private static String panePath(PaneMaterial material) {
        return material == PaneMaterial.TINTED ? "tinted_glass_pane" : material.vanillaPanePath();
    }

    private static ResourceKey<Block> blockKey(String name) {
        return ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, name)
        );
    }

    public record PaneFamily(
            Block vanillaPane,
            CenteredPaneBlock centeredPane,
            EdgePaneBlock edgePane,
            PaneAppearance appearance
    ) {
        public String itemPath() {
            String paneName = appearance.material() == PaneMaterial.TINTED
                    ? "tinted_glass_pane"
                    : BuiltInRegistries.BLOCK.getKey(vanillaPane).getPath();
            if (appearance.frame() == PaneFrame.NONE) {
                return "ultimate_" + paneName;
            }
            if (appearance.frame().isDynamic()) {
                return "modded_framed_ultimate_" + paneName;
            }
            return appearance.frame().path() + "_framed_ultimate_" + paneName;
        }
    }
}
