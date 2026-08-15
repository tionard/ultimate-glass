# Tempered Glass - Design

The mod ID, Java package, configuration paths, and established `ultimate_*` registry IDs remain
unchanged for compatibility. Tempered Glass is the player-facing name beginning in beta.4.

## Shared pane description

Appearance is independent from physical geometry:

- `PaneMaterial` identifies clear, stained, or tinted glass.
- `PaneFrame` identifies no frame, one of the finite vanilla woods, or the generic dynamic frame.
- `PaneAppearance` combines material and frame identity.
- `PanePlane`, `PanePlaneSet`, and `PaneGeometry` describe edge and centred sheets, junctions,
  collision, outlines, rotation, and renderer-facing geometry.
- `UltimatePane` exposes the shared appearance and geometry contracts.

Unframed panes and the 12 built-in wood-frame families are ordinary blocks with no BlockEntity.
The compatibility-sensitive `FACING`, edge connection, `AXIS`, centred connection, and
`WATERLOGGED` properties remain unchanged.

## Families, items, and tempering

Each Tempered appearance has one outside-face block, one centred block, and one shared item. The
item always places the outside-face geometry; iron/diamond tool interaction toggles it to centred
geometry without passing through a vanilla block.

Clear and stained inputs are Minecraft's normal pane blocks. The mod's `tinted_glass_pane` is an
`IronBarsBlock`-style connected pane with vanilla tinted-glass light dampening. Six tinted-glass
blocks craft 16 of these normal panes.

Vanilla-style panes are cooked into Tempered panes:

- furnace: 200 ticks;
- blast furnace: 100 ticks;
- clear/stained input: the matching Minecraft pane;
- tinted input: `ultimateglass:tinted_glass_pane`.

The former reversible shapeless conversion is deliberately absent.

## Wood frames

`WoodFramedPaneRecipe` accepts exactly one unframed Tempered pane and one item in `#minecraft:planks`.
For a vanilla plank it selects one of the fixed frame families. For any other tagged plank it emits
the material's generic dynamic frame item with a synchronized `frame_block` data component.

Fixed families encode their wood through block identity and generated models. The generic family
uses one edge and one centred block per glass material. `DynamicFrameBlockEntity` stores only the
plank block identifier, has no ticker, synchronizes on change, and exposes the same value as an item
component for placement, pick-block, harvesting, and edge/centred toggling.

The dynamic client model does not use a BlockEntityRenderer. Generated wood quads carry a private
bake marker; the normal chunk-model wrapper replaces those quads with the selected plank block
model's particle material, then emits them into the chunk mesh. The frame ID is included in the
geometry cache key. This preserves the common fixed-block path and lets mod/resource-pack textures
flow through without maintaining a wood registry in this mod.

## Framed surface model

The physical pane remains two pixels thick. Framed broad faces are divided at pixel coordinates
1, 2, 14, and 15:

- coordinates 0-1 and 15-16 use the wood texture;
- the remaining face uses the glass texture;
- the existing two-pixel outward frame and junction lines also use wood.

Normal and centre-sampled seam quads occupy the same boundary sections. At an exact continuation,
the renderer drops the normal boundary (including the wood band) and retains a centre sample from
the glass texture. At exposed borders the replacement is dropped. Corner sections remain framed
unless every boundary represented by that section continues, preserving perimeter runs.

For fixed variants, exact block identity defines a matching frame. For dynamic variants,
`PaneConnectionQueries.samePaneVariant` additionally compares the stored plank block IDs. Different
woods therefore never merge seamlessly or create centred source connections.

## Connected geometry

Edge connections build merged L and three-plane corners. Transparent sheets are trimmed at every
intersection, each pair receives one shared frame line, and triple intersections receive one
2x2x2 corner block.

Centred blocks retain `AXIS` as their primary saved plane. `connect_first` and `connect_second`
refer to stable perpendicular axes, producing X, Y, Z, XY, XZ, YZ, and XYZ sets. Connections are
derived only from directly adjacent matching primary planes, preventing ghost propagation. A
connected multi-plane centred state cannot be destructively toggled to a single edge sheet.

## Placement, rotation, and tools

Normal placement selects a far outside edge relative to the player. Shift placement first copies a
compatible orientation; otherwise it uses the configured clicked-face or near-player fallback.

The rotation-axis key cycles X, Y, and Z. Copper rotates. Iron also toggles edge/centred geometry.
Diamond additionally harvests supported glass intact. When a generic modded frame changes geometry,
its stored frame ID is copied to the new BlockEntity before neighbour connections are refreshed.

## Waterlogging and rendering

Tempered edge and centred blocks implement `SimpleWaterloggedBlock`. Edge-only renderer hooks clip
water vertices against every active pane face; centred water remains a normal full cell. Vanilla
and Sodium paths are selected by the mixin plugin so Sodium/Iris retain their normal water material
classification.

The seamless option is client-only. Its model wrapper removes explicitly marked boundary quads,
does not modify block state or collision, and invalidates compiled chunk geometry when toggled.

## World compatibility

Existing 0.1/earlier-beta custom IDs are retained. Fixed beta.4 vanilla-wood IDs are also retained
by the revised beta.4 implementation. The new normal tinted pane and generic modded-frame blocks are
additive. Removing the mod still removes mod-owned Tempered geometry, so users should back up worlds
before moving between development builds.

## Release sequence

Beta.5 is the final planned 0.2 feature beta and adds stair/slab composites. After its validation,
the next planned version is 0.2.0. The former beta.6-beta.8 mosaic and integration phases are moved
to the next feature cycle (currently 0.3.0).
