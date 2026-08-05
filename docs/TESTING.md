# Ultimate Glass manual test checklist

## Environment

Test with Minecraft Java 26.2, Fabric Loader 0.19.3, Fabric API, Ultimate Glass, and Mod Menu 20.0.1 for configuration tests.

Run one dedicated server and at least one Fabric client with the same Ultimate Glass JAR.

## Placement and shapes

1. Place clear and every stained glass pane against the north, east, south, west, top, and bottom faces of full blocks.
2. Verify each pane occupies the expected outside face of its own block space.
3. Verify top and bottom panes render horizontally at the correct height.
4. Verify selection outlines and collision match the visible pane in all six orientations.
5. Verify pane item stacks decrement normally in survival.
6. Load a world containing pre-existing vanilla panes and confirm they remain centred.

## Rotation axes

1. Assign the `Change Rotation Axis` key under Controls → Key Binds → Ultimate Glass.
2. Join a world and verify the default selected axis behaves as Y.
3. Press the key repeatedly and verify the cycle is Y → Z → X → Y.
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

For every issue, record the Minecraft log, exact pane colour, orientation, selected axis, clicked face, player direction, whether the pane was waterlogged, singleplayer or multiplayer, and whether Mod Menu was installed.
