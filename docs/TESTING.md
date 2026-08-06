# Ultimate Glass manual test checklist

## Environment

Test with Minecraft Java 26.2, Fabric Loader 0.19.3, Fabric API, Ultimate Glass, and Mod Menu 20.0.1 for configuration tests.

Run one dedicated server and at least one Fabric client with the same Ultimate Glass JAR.

## Normal placement

1. Stand south of a block and click its top face. Verify the pane is vertical on the north side, away from the player.
2. Repeat from north, east, and west and verify the pane moves to the opposite side.
3. Stand below a block and click a vertical side. Verify the pane is horizontal at the top edge, away from the player.
4. Stand above a block and click a vertical side. Verify the pane is horizontal at the bottom edge.
5. Repeat against every clicked face while varying the player's dominant projected direction.
6. Verify the pane is always perpendicular to the clicked face.
7. Verify visual geometry, outline, and collision occupy the same face.

## Shift placement

1. Shift-click a non-oriented full block from every side and verify placement remains perpendicular but chooses the side closer to the player.
2. Shift-click an existing custom pane and verify the new pane copies the clicked pane's orientation.
3. Test orientation copying from top/bottom slabs, stairs, trapdoors, doors, and horizontal/vertical facing blocks.
4. Verify logs, pillars, and other AXIS-only blocks do not act as orientation references.
5. Verify Shift placement against ignored blocks uses the closer-to-player fallback.
6. Verify placement remains server-authoritative in multiplayer.

## Outer-corner connections

1. Place directly adjacent perpendicular panes in the convex outer-corner arrangement.
2. Verify the outside pane gains a complete second pane plane, forming a continuous L shape with no block-sized gap.
3. Inspect the joint from inside and outside. Verify there is exactly one shared frame line and no cap passes through the other pane.
4. Verify the transparent sections stop at the shared line and do not darken from overlapping surfaces.
5. Verify the selection and collision shape include both planes.
6. Place the concave inner-corner arrangement and verify it does not connect.
7. Break either source pane and verify the added plane disappears and the normal outside frame returns.
8. Rotate either source pane and verify the corner recalculates.
9. Convert either source pane to a centred pane and verify the corner recalculates.
10. Repeat with horizontal/vertical combinations and all stained variants.

## Three-plane cube corners

1. Build three mutually perpendicular outside panes around one cube corner.
2. Verify the generated corner block contains three visible pane planes rather than only an L shape.
3. Verify each pair of panes has exactly one shared inner frame line.
4. Verify the three frame lines meet in one small corner block without one line passing through another plane.
5. Inspect the corner from all eight viewing directions and verify there are no overlapping transparent surfaces or missing inner lines.
6. Verify the outline and collision shape contain all three planes.
7. Remove each of the three source panes in turn and verify the generated geometry falls back from three planes to the correct L shape or single pane.
8. Rotate each source pane in turn and verify all affected corners recalculate.
9. Repeat with a combination containing one horizontal and two vertical panes.
10. Repeat while waterlogged and from a multiplayer client.

## Rotation axes

1. Verify `Change Rotation Axis` is assigned to V by default under Controls → Key Binds → Ultimate Glass.
2. Join a world and verify the default selected axis behaves as Y.
3. Press V repeatedly and verify the cycle is Y → Z → X → Y.
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
8. Reload the world and verify waterlogged states persist.

## Rendering and resources

1. Inspect every pane colour in daylight and darkness for all six orientations.
2. Verify opaque edge textures remain visible on every side, including top and bottom faces.
3. Verify transparency, texture tint, particles, breaking overlay, and neighbour visibility.
4. Check panes from both sides and at chunk boundaries.
5. Reload resources with `F3+T` and verify models remain valid.

## Failure cases to record

For every issue, record the Minecraft log, exact pane colour, orientation, selected axis, clicked face, player direction, connection arrangement, whether the pane was waterlogged, singleplayer or multiplayer, and whether Mod Menu was installed.
