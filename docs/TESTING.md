# Ultimate Glass manual test checklist

## Environment

Test with Minecraft Java 26.2, Fabric Loader 0.19.3, Fabric API, Ultimate Glass, and Mod Menu 20.0.1 for configuration tests.

Run one dedicated server and at least one Fabric client with the same Ultimate Glass JAR.

## Normal placement: perpendicular and far side

1. Stand south of a full block, click its top face, and verify the new pane is vertical on the north edge of its block space.
2. Repeat from north, east, and west and verify the pane always appears on the edge opposite the player.
3. Stand below a block, click each vertical side face, and verify the pane becomes horizontal on the top edge.
4. Stand above a block, click each vertical side face, and verify the pane becomes horizontal on the bottom edge.
5. Test intermediate and diagonal player positions for all six clicked faces; verify the selected pane plane is perpendicular to the clicked face and uses the strongest player-side direction within that face.
6. Repeat with clear glass and every stained pane colour.
7. Verify visible geometry, selection outline, and collision occupy the same outside face.
8. Verify pane item stacks decrement normally in survival.
9. Load a world containing pre-existing vanilla panes and confirm they remain centred.

## Shift placement: copied orientation and near side fallback

1. Shift-click the edge of a custom pane and verify the new pane copies its six-way orientation regardless of player position.
2. Repeat while targeting top/bottom slabs, stairs, closed and open trapdoors, doors, and blocks with clear `facing`, `horizontal_facing`, or `half` state.
3. Verify a top slab copies `up`, a bottom slab copies `down`, and a double slab is not treated as an orientation source.
4. Verify Shift-clicking an open trapdoor copies its vertical facing plane; a closed trapdoor copies its top/bottom half.
5. Verify stair copying uses its top/bottom half when clicking a horizontal face and its horizontal facing when clicking a vertical face.
6. Shift-click logs and pillar blocks in every axis and verify their AXIS property is ignored.
7. Shift-click stone or another non-oriented full block and verify placement stays perpendicular to the clicked face but chooses the side closest to the player.
8. Repeat the normal-placement examples while holding Shift and confirm the near/far side is inverted only when no oriented source is copied.
9. Repeat in multiplayer and verify every client sees the same orientation.

## Outer-corner connections

1. Place two perpendicular panes diagonally so their outer edges meet at a convex 90° corner.
2. Verify both touching opaque end caps disappear and the transparent panes create one continuous sharp corner.
3. Verify each pane retains its normal selection and collision plane and the two shapes meet without a gap.
4. Test all horizontal corner directions and all vertical↔horizontal corner combinations.
5. Repeat with clear panes, matching stained colours, and mixed pane colours.
6. Place perpendicular panes in a concave inner-corner arrangement and verify neither end cap is removed.
7. Place perpendicular panes side-adjacent without the required diagonal outer-corner arrangement and verify they do not connect.
8. Rotate one connected pane with the Glazier's Tool and verify both panes immediately recalculate their connected edges.
9. Break either connected pane and verify the remaining pane restores its opaque end cap.
10. Convert either connected pane to a centred pane and verify the remaining pane restores its end cap.
11. Reload the world and verify outer-corner states render correctly after loading.
12. Repeat connection changes from a multiplayer client and verify server/client synchronization.

## Rotation axes

1. Open Controls → Key Binds → Ultimate Glass and verify `Change Rotation Axis` defaults to `V`.
2. Join a world and verify the default selected axis behaves as Y.
3. Press `V` repeatedly and verify the cycle is Y → Z → X → Y, with the selected axis shown in chat.
4. For each selected axis, right-click panes in all six orientations.
5. Verify each click rotates the pane 90° around the selected axis.
6. Verify panes parallel to the selected axis remain unchanged.
7. Repeat from a multiplayer client and verify the server and other clients see the same orientation.

## Glazier's Tool mining and conversion

1. Mine clear glass, stained glass, centred panes, and custom panes with the Glazier's Tool.
2. Verify breaking takes progressive mining time instead of collecting instantly.
3. Verify each block drops its correct intact vanilla glass item.
4. Verify normal glass-breaking sounds, particles, and breaking overlay occur.
5. Verify creative mode still removes glass immediately without duplicate drops.
6. Sneak + right-click a custom pane and verify conversion to a centred vanilla pane.
7. Sneak + right-click a centred pane on each clicked face and verify conversion to the corresponding outside-face pane.
8. Verify waterlogging is preserved during centred ↔ outside-face conversion.

## Mod Menu-only tool toggle

1. Verify there is no enable/disable keybind for the tool.
2. With Mod Menu installed, disable Glazier's Tool interactions in Ultimate Glass settings.
3. Verify tool mining and right-click interactions are blocked locally.
4. Restart the client and verify the previous setting persists.
5. Re-enable the tool through Mod Menu and verify interactions return.
6. Join a server while disabled and verify no accidental tool interaction packets modify glass.

## Waterlogging and directional barriers

1. Place every clear/stained custom pane variant inside water in all six orientations.
2. Verify the block remains waterlogged and does not create an air pocket.
3. Verify water can occupy the same block space on either side of the thin pane.
4. Build a small water boundary and verify flow does not cross the face covered by the pane.
5. Verify water remains free to approach from directions not covered by the pane plane.
6. Rotate a waterlogged pane through X, Y, and Z and verify it remains waterlogged.
7. Switch waterlogged custom panes to centred panes and back without losing water.
8. Create and remove waterlogged outer-corner connections and verify waterlogging remains unchanged.
9. Reload the world and verify waterlogged states persist.

## Rendering and resources

1. Inspect every pane colour in daylight and darkness for all six orientations.
2. Verify opaque edge textures remain visible on every unconnected side, including top and bottom faces.
3. Verify only the touching end cap disappears at a valid outer connection; the broad glass texture and three other end caps remain unchanged.
4. Verify transparency, texture tint, particles, breaking overlay, and neighbour visibility.
5. Check panes from both sides and at chunk boundaries.
6. Reload resources with `F3+T` and verify all normal and connected models remain valid.

## Failure cases to record

For every issue, record the Minecraft log, exact pane colour, orientation, clicked face, Shift state, source block and source state, player position, selected rotation axis, connection arrangement, whether the pane was waterlogged, singleplayer or multiplayer, and whether Mod Menu was installed.
