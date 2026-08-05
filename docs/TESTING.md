# Ultimate Glass manual test checklist

## Environment

Test with Minecraft Java 26.2, Fabric Loader 0.19.3, Fabric API, Ultimate Glass, and optionally Mod Menu 20.0.1.

Run one dedicated server and one Fabric client with the same Ultimate Glass JAR.

## Placement

1. Place clear and every stained glass pane against each horizontal side of a full block.
2. Verify that the pane occupies the expected outside edge of its own block space.
3. Place panes by clicking the top and bottom of blocks near each corner and edge.
4. Verify that the nearest horizontal edge is selected.
5. Verify selection outlines and collision match the visible pane.
6. Verify pane item stacks decrement normally in survival.
7. Load a world containing pre-existing vanilla panes and confirm they remain centred.

## Glazier's Tool

1. Craft or obtain the Glazier's Tool.
2. Right-click clear glass, stained glass, centred panes, and edge panes.
3. Verify the correct vanilla item enters the inventory and the world block is removed.
4. Fill the inventory and verify the collected item drops safely.
5. Sneak + right-click each edge orientation and verify clockwise rotation.
6. Sneak + left-click an edge pane and verify conversion to a centred vanilla pane.
7. Sneak + left-click a centred pane and verify conversion to an edge pane.
8. Repeat all transformations from a multiplayer client and verify the server and other clients stay synchronized.

## Client toggle and Mod Menu

1. Press `G` and verify Glazier's Tool left- and right-click interactions are disabled locally.
2. Press `G` again and verify interactions return.
3. Restart the client and verify the previous setting persists.
4. With Mod Menu installed, open Ultimate Glass settings and toggle the tool.
5. Verify the Mod Menu toggle and keybind update the same persisted setting.
6. Join a server while the tool is disabled and verify no accidental interaction packet changes glass.

## Rendering and resources

1. Inspect every edge-pane colour in daylight and darkness.
2. Verify transparency, texture tint, particles, breaking overlay, and neighbour visibility.
3. Check panes from both sides and at chunk boundaries.
4. Reload resources with `F3+T` and verify models remain valid.

## Failure cases to record

For every issue, record the Minecraft log, exact pane colour, orientation, clicked face, player direction, singleplayer or multiplayer, and whether Mod Menu was installed.
