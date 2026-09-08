# 0.2.3: explicitly placed edge panes

Status: promoted from 0.2.3a after the user accepted in-game testing, including the parallel-pane
chisel correction. The experimental build passed all 95 automated tests. No Minecraft client or
server was launched by the agent; the checklist below remains useful for broader regression testing.
Inspected baseline: local main at 41e7157, version 0.2.2 (2026-09-08).
Experimental branch: codex/0.2.3a-manual-edge-panes.

## Requested behaviour

- Each of the six outside faces of a block cell can hold one explicitly placed pane.
- Adding a pane to a free face consumes one item in survival; occupied faces reject duplicates.
- Stored panes persist independently of neighbouring blocks, supporting opposite pairs, L,
  U, cube corners, and other subsets of the six faces.
- Breaking the assembly returns its installed panes, preserving material and frame data.
- Assemblies containing multiple panes cannot toggle into centred geometry.
- Centred-pane connection behaviour remains as it is in 0.2.2.

## Findings from the 0.2.2 baseline

`EdgePaneBlock` stores FACING and four relative CONNECT_* flags. Placement, neighbour updates,
and refreshConnectionsAround recompute those flags through hasOuterEdgeConnection. This
representation cannot include the face opposite FACING, and extra faces currently cost no items.

`PanePlaneSet` and PaneGeometry.of already represent arbitrary physical plane sets and support
union collision and whole-set rotation. The existing edge geometry cache and generated models,
however, assume a primary face plus four relative flags. Both need explicit occupancy support.

`TemperedPaneItem` already centralizes component-aware placement and experimental slab/stair
installation. Same-cell pane insertion belongs in this path, with explicit clicked-cell versus
adjacent-cell targeting. PanePlacementResolver currently distinguishes these coordinate contexts
for composites; blindly reusing ordinary BlockPlaceContext would target the wrong cell or face.

`GlaziersToolItem` rotates only FACING for edge panes, then recomputes neighbours. It must rotate
the installed face set and seam data together. Its edge-to-centred path has no multi-pane guard;
rejecting that toggle must also prevent falling through into ordinary rotation.

Drops currently have two relevant routes: EdgePaneBlock.getDrops and the diamond-tool fallback
in UltimateGlassInteractions. DynamicFramedEdgePaneBlock attaches plank components afterward.
All routes need consistent installed-pane counts without duplicate rewards.

Rendering, water clipping, and chisel seam targeting already consume shared pane geometry in
several places. These can reuse explicit occupancy, but generated models, geometry caching,
frame junctions, and remaining state-specific assumptions require an audit.

## Original implementation plan

1. Settle material mixing and old-world conversion rules before selecting the storage schema.
2. Represent the six occupied faces explicitly. For uniform material/frame assemblies, a six-bit
   state mask is sufficient: 63 non-empty combinations. Keep old registry IDs loadable and define
   an unambiguous migration from their derived geometry.
3. Add server-authoritative insertion into a free face. Preserve waterlogging, existing frame
   identity and seam edits; validate player permissions and added collision before consuming an
   item. Specify targeting and fallback so players can still extend a window into adjacent cells.
4. Remove neighbour-derived physical faces from the new edge placement path while retaining
   neighbour queries for visual seams and existing centred behaviour.
5. Rotate the entire assembly and reject centred toggling when its installed count exceeds one.
6. Make harvesting return the correct count/components through normal, Silk Touch, and diamond
   paths. Preserve normal Creative no-drop behaviour. Clarify how the existing configurable
   intact-drop rule applies to assemblies.
7. Generate merged geometry for every non-empty face mask, retaining outside corner frames,
   manual chisel overrides, dynamic wood textures, and native water shader classification.
8. Test insertion/count conservation, duplicate rejection, rotations, toggle rejection, reloads,
   migration, all six faces, opposite pairs, U shapes, and a six-face enclosure. Include framed
   and tinted materials, chunk boundaries, multiplayer, and Sodium/Iris visual checks.

## Accepted decisions and implementation

- Only matching glass and frame wood combine. Legacy fixed-wood frames and smart-item frames
  with the same plank ID count as matching; different colours/woods and framed/unframed do not.
- Preserve saved automatic corners and grant one recoverable pane for each saved face, as the
  user requested. Keep the original FACING/four flag schema and add CONNECT_OPPOSITE=false.
  Minecraft's normal block-state loading fills the missing new property; no world scan is needed.
- New edge panes do not gain faces from neighbours; saved faces never disappear with neighbours.
- Rotate complete assemblies; leave centred-pane and slab/stair composite behaviour unchanged.
- Preserve the existing intact-drop setting (enabled by default). Eligible harvests return the
  entire installed count; Creative still returns no drops. Diamond-tool collection now runs in
  the edge loot path, preventing a second reward from the after-break callback.
- Reuse the existing native fluid-rendering/clipping path; no custom water material was added.
- Chisel targeting uses the actual hit position within the sheet thickness, with the clicked
  face normal breaking ties at shared corners. Parallel sheets have independent seam targets;
  clicking a thin rim cannot select a distant perpendicular sheet merely sharing its normal.

## Controls and fallback

Normal clicks on an existing matching edge pane try the cursor-nearest free edge of that cell.
Shift uses the clicked face of that same cell; clicking the inner face of a sheet with Shift can
therefore add the opposite parallel sheet. The ordinary adjacent placement target is also checked
for matching assemblies. If the chosen face is occupied or materials differ, normal adjacent
placement is allowed instead. No existing sheet is overwritten. Each successful insertion costs
one survival item; a denied or obstructed insertion costs nothing.

## In-game acceptance checklist

Use a **copy** of a 0.2.2 world. Do not downgrade the edited copy: older versions cannot represent
the new opposite-face flag and would resume deriving/removing faces from neighbours.

1. Build a single sheet, L, three-plane corner, opposite pair, U, and fully enclosed six-face
   assembly. Count inventory after every insertion and verify duplicate attempts do not charge
   for another face (they may place a normal neighbouring pane if that position is free).
2. Repeat on each orientation. Verify it remains possible to extend a flat window normally.
3. Try mixed glass colours, framed/unframed, different vanilla woods and different modded woods.
   None may combine. Verify legacy oak and smart oak do combine.
4. Remove all neighbours, then reload the chunk/world. Assembly geometry and inventory count
   must remain stable. Load old saved L/cube corners: their saved faces must survive and count.
5. Rotate each assembly four times on each axis; shape, water, frame identity, and chisel seam
   overrides must return. Multi-pane edge-to-centred toggling must do nothing; single panes still
   toggle normally. Centred junctions and slab/stair composites must be unchanged.
6. Break assemblies normally (default intact drops), with Silk Touch, and with the diamond tool.
   Check count and components, including a Silk Touch-enchanted diamond tool. With intact drops
   disabled, ordinary unenchanted breaking follows existing glass rules; eligible tools still
   recover the full count. Creative must drop nothing.
7. Try obstructing a new face with a player/entity and using protected/adventure locations.
   Failed placement must not consume panes. Repeat successful/failed insertions in multiplayer.
8. Test waterlogging before/after adding faces, particularly opposite pairs and six-face shells,
   with vanilla rendering, Sodium, and Iris shaders. Inspect inner clipping, outer corner frames,
   seamless glass, tinted glass, wood textures, and chisel overrides.
9. Chisel each of the four boundaries on both sheets of an opposite pair, from inside and outside.
   Repeat for vertical and horizontal pairs, framed variants, U shapes and six-face assemblies.
   Showing/hiding a boundary on one parallel sheet must not change the other sheet's override.
   Also click thin rims and check the correct nearby sheet is edited. Shift-reset retains its
   existing whole-block behaviour.
