# Ultimate Glass - Design

## Shared pane description

The 0.2 architecture separates a pane's appearance from its physical geometry:

- `PaneMaterial` identifies clear, stained, or tinted glass without treating tinted glass as a
  decorative colour.
- `PaneAppearance` is the common appearance value carried by each ordinary pane family. Later
  frame and design phases can extend this concept without encoding arbitrary data in BlockState.
- `PanePlane` identifies one edge-aligned or centred physical sheet.
- `PanePlaneSet` is an immutable set of those sheets.
- `PaneGeometry` converts a plane set into shared collision/outline geometry and supports rotation.
  Its finite ordinary block-state geometries and combined voxel shapes are cached.
- `UltimatePane` lets render and fluid paths consume appearance and geometry without duplicating
  block-specific state decoding.

Ordinary clear and stained panes continue to be normal blocks. No BlockEntity or ticker is used.
Registered IDs and the compatibility-sensitive `FACING`, connection, `AXIS`, and `WATERLOGGED`
properties remain unchanged from 0.1.8.

## Pane families and items

Each clear/stained family contains three blocks:

- the untouched vanilla pane;
- a mod-owned centred full-sheet block whose normal is stored as X, Y, or Z;
- a mod-owned outside-face block whose position is stored as one of six directions.

Only the two mod-owned geometries belong to the tool toggle. A separate Ultimate Glass Pane item places the outside-face block. Vanilla pane items place vanilla blocks and never enter the toggle cycle.

Every vanilla pane has a reversible shapeless conversion recipe at a one-to-one ratio. Both custom block geometries map back to the same Ultimate Glass Pane item when harvested intact. The family also owns one shared `PaneAppearance`, and lookups are available by vanilla block, custom block, or `PaneMaterial`.

## Placement

Normal Ultimate Glass Pane placement projects the player's position onto the clicked face and selects a perpendicular outside edge on the far side.

Shift placement first attempts to copy a clear orientation from a custom pane, slab, stair, trapdoor, door, or facing/half block. AXIS-only logs and pillars remain ignored. When no orientation is available, the player-selected fallback is used:

- **Clicked face:** place the pane flush against the clicked surface. This is the default.
- **Near player:** use the perpendicular near-edge behavior.

The client persists the selected mode, sends it to the logical server, and may toggle it through Mod Menu or an unassigned keybind.

## Connected corners

Convex outside corners use generated merged geometry:

- two perpendicular source panes create an L-shaped corner;
- three mutually perpendicular panes create a complete cube corner;
- transparent planes are trimmed at intersections;
- each pane pair has one shared frame line;
- three frame lines meet in one 2x2x2 corner block;
- outline and collision geometry include every active plane.

Concave inner arrangements remain unconnected.

Convex connection discovery lives in `PaneConnectionQueries`. Block state stores the same four
relative connection flags as 0.1.8; decoding those flags produces a world-oriented
`PaneGeometry`, which is shared by collision, seamless rendering, and water clipping.

## Seamless connected rendering

Matching Ultimate panes use seamless rendering by default. Coplanar panes of the same colour and geometry suppress only the glass/frame segments along their shared block boundary. Junction frames inside L-shaped and cube-corner blocks remain visible because they form intentional outside edges rather than flat coplanar seams.

The setting is client-side and purely visual: it does not add block-state properties, alter collision or water clipping, or require server synchronization. Generated models divide broad glass faces at the two-pixel frame boundary and provide two variants for each boundary section: the normal vanilla-texture edge and an explicitly marked replacement sampled from a real two-by-two region at the centre of the same texture. The marker survives model baking and is removed before rendering. A Fabric model wrapper keeps the normal edge while exposed, but at a matching continuation it removes the solid frame and normal texture edge and emits the centre-sampled replacement. The connected area therefore retains the material's interior colour and transparency, including with resource packs. Disabling the setting drops every replacement and restores the ordinary framed appearance. Toggling it invalidates compiled chunk geometry so the visible world updates immediately.

Only the exact same Ultimate block joins seamlessly. Different stained colours, edge-to-centred neighbours, and vanilla panes retain their complete outside frames.

## Rotation and geometry toggle

`Change Rotation Axis` is assigned to V by default and cycles X, Y, and Z. Right-clicking with any Glazier's Tool rotates either custom geometry 90 degrees around the selected axis. Rotation and conversion refresh nearby corner states.

Shift + right-click with an iron or diamond tool toggles `outside-face <-> centred full-sheet`. Edge-to-centred conversion preserves the pane normal. Centred-to-edge conversion preserves that axis and selects the clicked or player-facing side of it. The operation works identically for horizontal and vertical panes because no vanilla state is involved.

The centred model mirrors the edge model's face layout: the full glass texture draws both broad faces, while concrete is limited to the four thin outward-facing sides. The frame elements have no broad coplanar faces, preventing texture flicker while keeping edge and centred panes visually identical.

## Tool tiers

- **Copper:** rotation only.
- **Iron:** rotation plus two-state custom pane conversion.
- **Diamond:** iron abilities plus progressive, intact glass harvesting.

The legacy 0.1.3 item ID remains registered with diamond-tier behavior to protect existing worlds, but is not newly craftable or shown in Creative tabs.

Each tier uses a custom server-checked recipe with one tier material, one string, and two sticks. Mirrored patterns are accepted.

## Native waterlogging

Both custom blocks implement Minecraft's `SimpleWaterloggedBlock` contract in the same way as vanilla panes. Placement detects existing water, buckets can insert or remove a source, neighbour updates schedule water ticks, and `waterlogged=true` exposes a native source-water `FluidState`.

Rotation retains the complete block state. Edge/centred toggling explicitly copies the waterlogged value, including when loading states created by earlier 0.1.6 development builds.

Ultimate Glass registers its custom blocks as transparent through Fabric's supported fluid-overlay API. This selects the normal overlay used beside glass instead of the falling-water side texture.

Native waterlogging models fluid as a source occupying the block cell and does not expose a block-specific clipped volume. Client-only renderer-specific mixins therefore clamp water vertices to the interior boundary of every active `EdgePaneBlock` face. A single edge supplies one clipping plane; connection properties automatically add the planes required by L-shaped, cube, and opposite-edge states.

Without Sodium, Fabric's default-render handoff runs Minecraft's standard fluid tessellator through a clipping vertex output. With Sodium, an optional mixin adjusts coordinates inside Sodium's native fluid tessellator before its normal quad encoding; this preserves Sodium's water material metadata for Iris shaders. The hooks do not register or replace the global water handler, water model, textures, tint, render layer, or material. Centred panes and every non-edge water block remain on the active renderer's unmodified normal path. A mixin config plugin selects exactly one path at startup.

## World compatibility

Vanilla panes remain vanilla blocks. Centred and outside-face panes are mod-owned blocks and require Ultimate Glass to remain installed. Convert both custom geometries back to vanilla panes through the reversible item recipe before removing the mod. Back up worlds before moving between development builds.
