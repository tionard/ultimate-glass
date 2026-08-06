# Ultimate Glass 0.1.4 manual test checklist

## Environment

Test Minecraft Java 26.2 with Fabric Loader 0.19.3, Fabric API, Ultimate Glass, and optionally Mod Menu 20.0.1. Test both singleplayer and a dedicated server with matching client/server JARs.

## Shift placement modes

1. Use the default Clicked Face mode and Shift-place against all six faces of ordinary full blocks.
2. Verify each pane lies directly against the clicked surface.
3. Select Near Player in Mod Menu and verify the old perpendicular near-edge behavior.
4. Assign and use Toggle Shift Placement Mode; verify the mode changes and persists after restart.
5. In both modes, Shift-click panes, slabs, stairs, trapdoors, doors, and facing/half blocks; verify orientation copying takes priority.
6. Verify AXIS-only logs and pillars use the selected fallback rather than acting as orientation sources.
7. Repeat on a multiplayer client and verify server-authoritative placement.

## Tool tiers

### Copper

1. Right-click every edge-pane orientation and verify rotation.
2. Shift + right-click and verify it still only rotates rather than converting.
3. Break glass and verify normal glass behavior with no intact drop or accelerated mining.

### Iron

1. Verify normal right-click rotation.
2. Shift + right-click centred and edge panes; verify conversion in both directions.
3. Verify waterlogging survives conversion.
4. Break glass and verify no intact drop or accelerated mining.

### Diamond

1. Verify rotation and conversion match iron.
2. Mine clear/stained blocks, centred panes, and edge panes in survival.
3. Verify progressive breaking, normal particles/sounds, and one correct intact item drop.
4. Verify Creative mode creates no duplicate drops.

### Legacy tool

1. Load a 0.1.3 world containing the old item.
2. Verify the item still exists and behaves like the diamond tier.
3. Verify it is absent from new recipes and Creative tabs.

## Recipes and configuration

1. Craft each tier with its material, one string, and two sticks.
2. Verify both horizontal mirror layouts work and no old two-string recipe remains.
3. Disable each tier independently in Mod Menu and verify its recipe stops producing a result.
4. Verify disabled tools remain available through `/give` and Creative mode.
5. Verify non-operator multiplayer clients cannot change server crafting settings.
6. Verify an operator/integrated-server owner can change them and connected clients receive the new state.
7. Restart the server and verify settings persist in `ultimate-glass-server.json`.
8. Verify the old global tool-interactions option is absent.

## Connected geometry

1. Re-test single panes, two-plane L corners, and three-plane cube corners in all orientations.
2. Verify merged transparent geometry, shared frame lines, outlines, and collision shapes remain correct.
3. Break, rotate, or convert each source pane and verify nearby connections refresh.

## Waterlogging regression

1. Reproduce the 0.1.3 comparison: a single edge pane beside an L-shaped connected pane in source water.
2. Verify both display the same flat, full source-water surface.
3. Repeat with three-plane cube corners and all horizontal/vertical combinations.
4. Verify the fluid state remains a water source after placement, connection changes, rotation, conversion, save/reload, and chunk reload.
5. Build controlled water channels and verify water cannot cross any face occupied by an active glass plane.
6. Verify water may still flow through directions not covered by a pane plane.
7. Repeat on a dedicated server and with clear plus multiple stained variants.

## Failure details

Record the pane colour, facing, connection mask, waterlogged state, selected Shift mode, held tool tier, rotation axis, singleplayer/multiplayer context, and relevant log output.
