# Ultimate Glass 0.2.0-beta.1 regression checklist

Beta.1 intentionally changes no gameplay. Run the complete 0.1.8 checklist below against both a
new world and a backed-up world last saved with the released 0.1.8 JAR.

## Automated gate

1. Run `gradle build` with Java 25.
2. Confirm the pane material, plane-set, relative-geometry, and rotation unit tests pass.
3. Confirm the output is `build/libs/ultimate-glass-0.2.0-beta.1.jar`.
4. Start a dedicated server with the beta JAR and wait for the normal ready message.
5. Confirm no registered block/item IDs or BlockState property names differ from 0.1.8.

## Existing-world compatibility

1. Back up and open a representative 0.1.8 world with beta.1.
2. Visit saved clear and stained edge panes in all six orientations.
3. Visit saved centred panes on X, Y, and Z.
4. Visit saved L corners, cube corners, and waterlogged arrangements.
5. Confirm no missing blocks/items, remapped states, changed geometry, or unexpected fluid updates.
6. Save, restart, and revisit the same chunks.

## Environment

Test Minecraft Java 26.2 with Fabric Loader 0.19.3, Fabric API, Ultimate Glass, and optionally Mod Menu 20.0.1. Repeat the rendering checks with Sodium/Iris and with shaders both enabled and disabled. Back up any world previously opened with a development JAR.

## Pane items and recipes

1. Confirm Creative Building Blocks contains Ultimate Glass Pane plus all 16 stained variants.
2. Craft one vanilla glass pane by itself and confirm it produces one matching Ultimate Glass Pane.
3. Craft one Ultimate Glass Pane by itself and confirm it returns one matching vanilla pane.
4. Repeat both directions for several stained colours and verify no colour changes or item loss.
5. Place a vanilla pane item and verify it uses untouched vanilla connected-pane geometry.
6. Place an Ultimate Glass Pane and verify it uses outside-face geometry.

## Shift placement modes

1. Use the default Clicked Face mode and Shift-place Ultimate panes against all six faces of ordinary full blocks.
2. Verify each pane lies directly against the clicked surface.
3. Select Near Player in Mod Menu and verify perpendicular near-edge behavior.
4. Assign and use Toggle Shift Placement Mode; verify the mode changes and persists after restart.
5. In both modes, Shift-click custom panes, slabs, stairs, trapdoors, doors, and facing/half blocks; verify orientation copying takes priority.
6. Verify AXIS-only logs and pillars use the selected fallback rather than acting as orientation sources.
7. Repeat placement on a multiplayer client and verify the server chooses the same state.

## Tool tiers

### Copper

1. Right-click every edge and centred orientation and verify rotation.
2. Shift + right-click and verify it still only rotates rather than toggling geometry.
3. Break glass and verify normal glass behavior with no intact drop or accelerated mining.

### Iron

1. Verify normal right-click rotation.
2. Shift + right-click repeatedly and verify custom panes alternate `outside-face <-> centred full-sheet`.
3. Start from both states and verify vanilla is never produced.
4. Repeat with X-, Y-, and Z-axis panes and confirm the pane axis is preserved on every toggle.
5. Use the tool on a vanilla pane and verify it does not convert or rotate it.
6. Repeat for clear and every stained family and verify colour is preserved.
7. Break glass and verify no intact drop or accelerated mining.

### Diamond

1. Verify rotation and conversion match iron.
2. Mine clear/stained glass blocks, vanilla panes, centred panes, and edge panes in survival.
3. Verify progressive breaking, normal particles/sounds, and exactly one correct intact item drop.
4. Confirm custom geometries drop their matching Ultimate Glass Pane, while vanilla blocks/panes drop vanilla items.
5. Verify Creative mode creates no duplicate drops.
6. Break a custom pane with an ordinary Silk Touch tool and verify its Ultimate Glass Pane loot table.

### Legacy tool

1. Load a 0.1.3 world containing the old item.
2. Verify the item still exists and behaves like the diamond tier.
3. Verify it is absent from new recipes and Creative tabs.

## Connected edge geometry

1. Re-test single panes, two-plane L corners, and three-plane cube corners in all orientations.
2. Verify merged transparent geometry, shared frame lines, outlines, and collision shapes remain correct.
3. Break, rotate, or toggle each source pane and verify nearby connections refresh.
4. Save/reload and chunk-reload each arrangement.

## Centred full-sheet geometry

1. Toggle a lone edge pane and verify it becomes one complete sheet rather than a rod.
2. Create X-, Y-, and Z-axis sheets and verify their visuals, outline, collision, and support shapes agree.
3. Inspect every sheet closely and from a distance; confirm the glass texture and frame never flicker or change colour from z-fighting.
4. Repeat the previous check without shaders, with shaders, and after a chunk reload.
5. Cycle the selected rotation axis with V and rotate each sheet around X, Y, and Z.
6. Verify rotation around the sheet's own normal leaves it unchanged; each perpendicular axis swaps it to the remaining plane.
7. Shift-place an edge pane from a centred sheet and verify the copied pane is parallel to that sheet.

## Seamless connected panes

1. Confirm Seamless connected panes is enabled by default in Mod Menu and persists after restart.
2. Place two matching coplanar edge panes beside each other and verify their shared frame disappears while every exposed side retains its frame.
3. Repeat in horizontal and vertical planes, across all four in-plane directions, chunk boundaries, and chunk-section boundaries.
4. Build 2x2 and 3x3 coplanar sheets; verify only the outside perimeter is framed and no small corner gaps appear where two internal seams meet.
5. Repeat the coplanar checks with matching centred panes on X, Y, and Z axes.
6. Verify different stained colours do not join seamlessly.
7. Build 2x2 windows from several matching stained colours and verify internal seams contain no
   clear or differently tinted strips, crosses, or rectangular patches at any viewing angle.
8. Verify edge panes do not join centred panes, even when parallel.
9. Verify vanilla panes remain visually untouched.
10. Build L-shaped and three-plane cube corners; verify every angled junction retains its solid outside edge.
11. Extend an L-shaped corner through neighbouring blocks; verify flat coplanar seams disappear while the angled junction frame remains continuous through the run.
12. Disable seamless rendering and verify every pane immediately returns to the ordinary framed appearance without replacing blocks or reloading the world.
13. Re-enable it and verify the connected appearance returns immediately.
14. Break, place, rotate, and edge/centred-toggle panes; verify affected seams update without stale frame pieces.
15. Repeat with vanilla/Fabric rendering and Sodium/Iris, both shaders disabled and enabled.
16. Waterlog seamless edge and corner panes; verify frame suppression does not change water clipping, shader classification, or source-water behavior.

## Native waterlogging

1. Place edge and centred panes into source water in every orientation; verify they retain a source-water fluid state.
2. Insert and retrieve water with a bucket; verify the pane remains in place, the bucket changes correctly, and the waterlogged state toggles.
3. Repeat with clear and multiple stained variants.
4. Rotate every waterlogged edge and centred orientation; verify waterlogging is preserved.
5. Toggle waterlogged panes repeatedly between edge and centred geometry; verify the axis and source-water state are both preserved.
6. Save/reload and chunk-reload waterlogged panes, including states created by earlier 0.1.6 development builds.
7. Inspect a single waterlogged edge pane from above and both sides; verify the water surface ends at the pane's inner face and does not reach the outside block edge.
8. Repeat the clipping check for north, south, east, west, up, and down panes, including panes crossing chunk-section boundaries.
9. Build isolated 2x2 and 3x3 pools from waterlogged panes and verify their top surfaces are level, visible, and clipped consistently around the perimeter.
10. Test L-shaped and three-plane cube corners in multiple rotations; verify the water is clipped by every active pane face and does not appear lowered, diagonal, or falling down grounded sides.
11. Create states with panes on opposite faces where possible; verify both limits are applied and the remaining centre volume renders normally.
12. Compare a normal source-water pool surrounded by panes with a pool made from waterlogged edge panes.
13. Inspect waterlogged centred panes and confirm their water still uses the normal full-cell render with no edge clipping.
14. Run flowing water beside a waterlogged pane and verify it does not visually appear to originate from or cross a blocked pane face.
15. Verify gameplay flow cannot cross any active pane plane and can still use genuinely open directions.
16. Repeat all visual cases with vanilla/Fabric rendering and with Sodium/Iris, both shaders disabled and enabled.
17. Toggle shaders and reload chunks repeatedly; confirm the water remains visible, receives the active renderer's shading, and causes no crashes.

## Tool recipes and configuration

1. Craft each tool tier with its material, one string, and two sticks.
2. Verify both horizontal mirror layouts work and no old two-string recipe remains.
3. Disable each tier independently in Mod Menu and verify its recipe stops producing a result.
4. Verify disabled tools remain available through `/give` and Creative mode.
5. Verify server-authoritative settings and permission handling on a dedicated server.
6. Restart the client/server and verify settings persist in `ultimate-glass-server.json`.

## Failure details

Record the pane item/colour, custom geometry, facing or axis, connection mask, selected Shift mode, held tool tier, rotation axis, rendering stack, shader state, singleplayer/multiplayer context, and relevant log output.
