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

The compatibility-sensitive `FACING`, edge connection, `AXIS`, centred connection, and
`WATERLOGGED` properties remain unchanged. Starting with 0.2.1a, ordinary panes use a small,
non-ticking `PaneSeamBlockEntity` to store independent manual boundary choices. Dynamic frames and
composites store the same seam data in their existing BlockEntities.

## Families, items, and tempering

Material-specific outside-face, centred, and full-block implementations remain internal so colour,
tinted lighting, collision, waterlogging, and vanilla-style behaviour stay state-driven. Player
inventories expose six component-backed item families instead: unframed Tempered panes/blocks and
ordinary/Tempered framed panes/blocks. `glass_material` chooses the internal material block during
placement; form, tempering, and framing remain structural properties of the item ID so invalid
combinations cannot be produced by recipes.

Clear and stained inputs are Minecraft's normal pane blocks. The mod's `tinted_glass_pane` is an
`IronBarsBlock`-style connected pane with vanilla tinted-glass light dampening. Six tinted-glass
blocks craft 16 of these normal panes.

Vanilla-style panes are cooked into Tempered panes:

- furnace: 200 ticks;
- blast furnace: 100 ticks;
- clear/stained input: the matching Minecraft pane;
- tinted input: `ultimateglass:tinted_glass_pane`.

Six matching unframed Tempered full blocks in the vanilla two-row pane pattern produce 16
component-backed Tempered panes. The custom recipe reads `glass_material` from its inputs, rejects
mixed materials, and copies the matching material to its output.

Starting with 0.2.2, `GlassVariant` describes the material, pane/full-block form, tempering state,
and optional frame as one shared family key. Every supported vanilla full glass block has an
unframed Tempered counterpart. The existing server settings apply consistently to both physical
forms: Tempered blocks follow the intact-drop switch, and the optional shapeless reverse recipe
accepts one unframed Tempered pane or block and returns one matching vanilla-style item.

## Wood frames

`WoodFramedPaneRecipe` accepts exactly one supported unframed pane or full block and one item in
`#minecraft:planks`. The glass input may be ordinary or Tempered. Every plank—including all vanilla
woods—produces the same smart item family with the input material in `glass_material` and the exact
namespaced plank block ID in `frame_block`.

The internal dynamic family uses one edge/centred pair or full block per glass material; wood never
multiplies block or item registrations. `DynamicFrameBlockEntity` stores the plank identifier, has
no ticker, synchronizes on change, and exposes the same value as an item component for placement,
pick-block, harvesting, and edge/centred toggling.

The dynamic client model does not use a BlockEntityRenderer. Generated wood quads carry a private
bake marker; the normal chunk-model wrapper replaces those quads with the selected plank block
model's particle material, then emits them into the chunk mesh. The frame ID is included in the
geometry cache key. This lets vanilla, mod, and resource-pack wood textures flow through one path
without maintaining a wood registry in this mod. Old fixed-wood blocks remain registered only as
hidden transition aliases for existing 0.2.1 worlds.

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

Framed full blocks split each outside face into a one-pixel wood perimeter and a glass centre.
When two blocks have the same complete glass family and exact frame identity, their hidden meeting
faces and the two matching perimeter runs disappear from the chunk mesh. Corner wood stays until
both adjacent window directions continue, preserving one clean outside perimeter. Dynamic full
blocks use the same plank-particle substitution and geometry-cache identity as dynamic panes.

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

Normal right-click placement selects the edge nearest the cursor on the clicked face. Shift +
right-click is a fixed precision override: it always places the pane against the clicked face and
never copies the orientation of panes, slabs, stairs, trapdoors, or other half-blocks.

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

## Manual seam editing

`PaneSeamData` stores two compact masks per pane: forced-visible and forced-seamless boundaries.
Any boundary absent from both masks remains automatic. Each key contains both a physical
`PanePlane` and one in-plane world direction, so opposite sides remain independently representable
even though the default editing mode changes a shared seam as a pair.

The Glass Chisel resolves the clicked sheet and its nearest edge on the client. Framed full blocks
expose their twelve physical cube edges through the same target format; both visible surface halves
of one cube edge are updated together. A normal click
sends the opposite of the player's currently rendered result. Paired mode is the default: when a
coplanar pane continues into the neighboring cell, the server writes the same forced result to its
opposite boundary without requiring matching material or frame identity. The configurable `V`
binding switches to persistent single-edge mode for deliberate mismatches. Shift-use clears both
masks for the entire clicked pane block. The server checks the enabled setting, tool ownership,
reach, plane geometry, and boundary validity before saving any requested state. Forced seamless
boundaries never require a matching pane or any particular neighbouring block.

The renderer applies manual choices over the client seamless preference: forced choices always
win, while automatic boundaries retain the existing exact-variant continuation rule. Saved masks
are included in the chunk geometry key and synchronized with the BlockEntity update packet.
Existing Glazier's Tool rotations and edge/centred toggles remap the masks with the physical glass.

## Stair and slab composites

`CompositePaneBlockEntity` stores the complete original host `BlockState`, the `PaneAppearance`, a
vertical facing, an edge/centred mode flag, and the optional dynamic plank block ID. It has no
ticker. The composite block state carries only waterlogging and tinted-light flags needed by
state-only engine queries.

Composite placement is server-authoritative, experimental, and disabled by default. Tempered pane
items intercept Shift-use on stairs and non-double slabs only when it is enabled; ordinary use
continues through normal pane placement beside the host. Hosts with a
BlockEntity are rejected so beta.5 cannot silently discard mod-owned data. The installed pane uses
the clicked host face exactly. Unsupported horizontal composite orientations and fully occupied
stair faces fall through to ordinary pane placement.

The client wrapper emits the stored host model and corresponding edge-pane model into one normal
cached chunk mesh. Generated pane and frame sections are split at half-block boundaries; sections
whose inward sample lies inside the stored host shape are discarded. Collision and outline use the
same host-volume subtraction, so glass neither renders nor collides inside the stair/slab body.
The composite block is explicitly marked as dynamically shaped because its collision depends on
BlockEntity data. Without that flag Minecraft caches the pre-BlockEntity full-cube fallback, which
also prevents the hopper below from collecting items through the open half-block volume.

Normal use with any Glazier's Tool cycles clockwise to the next pane facing whose host-subtracted
shape is non-empty, skipping fully occupied stair faces. Shift-use with an iron or diamond tool
toggles the installed pane between edge and centred geometry. Ordinary breaking independently
returns the stored host block item and the exact pane stack, retaining dynamic frame data,
regardless of tool suitability. Composite water state remains stored with the composite.

The server config defaults Tempered glass to intact drops for every tool and bare-hand harvest.
Disabling that option restores the Silk Touch/diamond-tool rule. A separate off-by-default custom
recipe converts exactly one unframed Tempered pane or full block into its matching vanilla-style
form; framed glass is deliberately excluded so crafting cannot silently discard its frame
material.

## World compatibility

Version 0.2.2 is transitional. Existing 0.1/0.2.1 and fixed beta.4 block/item IDs remain registered
and loadable but are hidden from Creative and are no longer recipe or drop outputs. All newly made,
picked, or harvested supported glass uses the six smart item families. Players should replace old
fixed-frame windows during 0.2.2. A later release must run an explicit migration or retain aliases;
simply unregistering a missing block ID is not a safe vanilla-glass conversion.

## Release sequence

Version 0.2.1 is the stable manual-seam and dedicated-Creative-tab release. Version 0.2.2a began
player testing for complete ordinary/Tempered pane and full-block families; 0.2.2c is its current
regression-fix build. Mosaics and their
Glazier's Table remain a later feature cycle.
