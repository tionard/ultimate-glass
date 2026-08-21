# Ultimate Glass - Design

The mod ID, Java package, configuration paths, and established `ultimate_*` registry IDs remain
unchanged for compatibility. Ultimate Glass is the mod name; its enhanced pane items retain the
`Tempered ...` naming introduced in beta.4.

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
remain compatibility-sensitive summaries of which perpendicular axes have direct sources. The
actual collision and client model query each source direction and add only the half-plane from that
neighbour to the centre junction. One source therefore creates an L rather than a full `+`; two or
three real sources can still create supported multi-arm and cube-corner geometry. Derived arms
never source further derived arms, preventing ghost propagation.

## Placement, rotation, and tools

Normal right-click placement uses the player's horizontal direction, matching stair orientation.
Shift + right-click is a fixed precision override: it always places the pane against the clicked
face and never copies the orientation of panes, slabs, stairs, trapdoors, or other half-blocks.

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

## Stair and slab composites

`CompositePaneBlockEntity` stores the complete original host `BlockState`, the `PaneAppearance`, a
vertical facing, an edge/centred mode flag, and the optional dynamic plank block ID. It has no
ticker. The composite block state carries only waterlogging and tinted-light flags needed by
state-only engine queries.

Composite placement is server-authoritative, experimental, and disabled by default. Tempered pane
items intercept use on stairs and non-double slabs only when it is enabled. Hosts with a
BlockEntity are rejected so beta.5 cannot silently discard mod-owned data. Normal placement uses
the nearest edge of the clicked host face; Shift uses the clicked host face exactly. Unsupported
horizontal composite orientations and fully occupied stair faces fall through to ordinary pane
placement.

The client wrapper emits the stored host model and corresponding edge-pane model into one normal
cached chunk mesh. Generated pane and frame sections are split at half-block boundaries; sections
whose inward sample lies inside the stored host shape are discarded. Collision and outline use the
same host-volume subtraction, so glass neither renders nor collides inside the stair/slab body.

Shift-use with an iron or diamond Glazier's Tool toggles the installed pane between edge and centred
geometry. Ordinary breaking independently returns the stored host block item and the exact pane
stack, retaining dynamic frame data, regardless of tool suitability. Composite water state remains
stored with the composite.

The server config defaults Tempered panes to intact drops for every tool and bare-hand harvest.
Disabling that option restores the Silk Touch/diamond-tool rule. A separate off-by-default custom
recipe converts exactly one unframed Tempered pane into its matching vanilla-style pane; framed
panes are deliberately excluded so crafting cannot silently discard their frame material.

## World compatibility

Existing 0.1/earlier-beta custom IDs are retained. Fixed beta.4 vanilla-wood IDs are also retained
by the revised beta.4 implementation. The new normal tinted pane and generic modded-frame blocks are
additive. Removing the mod still removes mod-owned Tempered geometry, so users should back up worlds
before moving between development builds.

## Release sequence

Beta.5 is the final planned 0.2 feature beta and adds stair/slab composites. After its validation,
the next planned version is 0.2.0. The former beta.6-beta.8 mosaic and integration phases are moved
to the next feature cycle (currently 0.3.0).
