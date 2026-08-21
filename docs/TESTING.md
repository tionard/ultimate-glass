# Ultimate Glass 0.2.0-beta.5 regression checklist

Beta.5 adds stair/slab composites on top of beta.4's tempering and universal wood frames. Run this
checklist in a new world and a backed-up existing world.

## Automated gate

1. Run `gradle test` with Java 25 and confirm all pane/model tests pass.
2. Run `gradle build` and confirm `build/libs/ultimate-glass-0.2.0-beta.5.jar` is produced.
3. Start a dedicated 26.2 Fabric server and confirm all recipes load and the ready message appears.
4. Confirm pre-beta.4 `ultimate_*` block/item IDs and saved BlockState property names are unchanged.

## Stair and slab composites

1. Confirm composites are disabled in a fresh config, enable `Experimental stair/slab panes` in
   Mod Menu, and reconnect once to verify the server-authoritative value remains synchronized.
2. Install clear, several stained colours, tinted, fixed-framed, and modded-framed Tempered panes
   into bottom slabs, top slabs, and stairs. Confirm the host and pane occupy one block cell.
3. Confirm composites are edge-bound. On each host face, click near all four edges and verify normal
   placement uses the nearest edge. Confirm Shift placement copies the clicked face exactly.
4. Confirm a double slab, horizontal clicked face, or fully occupied stair face does not create a
   composite and leaves ordinary pane placement available.
5. Test every horizontal stair facing, top/bottom half, and straight/inner/outer shape. Confirm the
   original stair model and correct open-volume pane orientation survive placement.
6. Inspect rendering from both sides. No glass or frame pixel may flicker or bleed through the host.
7. Inspect collision and outline shapes. The host must keep its normal shape, the exposed pane must
   collide, and no hidden full-block camera collision may appear.
8. Waterlog every slab/stair combination, install a pane, bucket water in/out where applicable,
   save/reload, then break it. Confirm exactly one water source and the correct host drop.
9. Save/reload, cross a chunk boundary, unload/reload chunks, reconnect, and restart a dedicated
   server. Confirm host facing/half/shape, pane material, pane facing, and frame identity.
10. Shift + right-click composites with iron and diamond Glazier's Tools. Confirm each use toggles
    edge/centred geometry, preserves host/pane/water/frame data, and returns no item.
11. Break composites with representative correct and incorrect host tools, including bare hands.
    Confirm exactly one host block item and one pane always drop, including the modded frame
    component.
12. Place composites beside ordinary matching and nonmatching Tempered panes and inspect every seam.
13. Verify `/data get block` shows a non-ticking data holder and confirm no composite
    BlockEntityRenderer appears in a client profiler capture.
14. Try a modded stair/slab with no BlockEntity and record compatibility. Confirm a host that owns a
    BlockEntity is rejected without changing the block or consuming the pane.

## Tempering and names

1. Confirm the mod and settings category say `Ultimate Glass`, while enhanced pane items remain
   named `Tempered ...`.
2. Confirm commands and existing saves still recognize the retained `ultimateglass:ultimate_*` IDs.
3. Smelt and blast-smelt clear plus several stained vanilla panes; verify the matching Tempered item,
   200/100-tick timing, and no colour change.
4. Confirm the Tempered-to-vanilla recipe is absent by default. Enable it in Mod Menu and verify one
   unframed Tempered pane returns one matching vanilla-style pane; framed panes must not match.
5. Craft six tinted-glass blocks in two rows; verify 16 normal `Tinted Glass Pane` items.
6. Place those tinted panes and verify normal post/side connections, waterlogging, harvesting, and
   complete tinted-glass block/skylight dampening.
7. Smelt and blast-smelt the normal tinted pane; verify `Tempered Tinted Glass Pane` output.
8. Confirm the normal tinted pane is not directly rotatable or edge/centred-toggleable.

## Fixed wood frames

1. Craft each Tempered material with oak, spruce, birch, jungle, acacia, dark oak, pale oak,
   crimson, warped, mangrove, bamboo, and cherry planks.
2. Verify the result name, Creative entry, and placed texture match the wood.
3. Inspect a lone pane from the front/back and confirm wood covers both the outer edge and exactly
   the closest one-pixel face band.
4. Repeat for edge, horizontal, centred, L, cube-corner, XY/XZ/YZ, and XYZ geometry.
5. Verify fixed variants have no BlockEntity with `/data get block` or equivalent inspection.
6. Pick-block, Silk Touch, and diamond-tool harvest each family; verify the exact framed item.

## Modded plank frames

1. Install at least two mods that add visibly different planks to `#minecraft:planks`.
2. Craft and place at least three connected panes using one modded plank. Confirm every already
   placed pane keeps that modded texture when the next pane changes its connection state.
3. Confirm a plank named `Umbran Planks` produces an item named `Umbran-Framed ...`, while a
   nonstandard plank-tagged item whose name does not end in `Planks` keeps its full display name.
4. Craft each plank with clear, one stained, and tinted Tempered panes.
5. Verify the item retains the correct source plank and the placed frame uses that plank block's
   texture under both default resources and a resource pack.
6. Save/reload, cross a chunk boundary, unload/reload chunks, and restart the server; verify the
   frame identity persists.
7. Pick-block, Silk Touch, and diamond-tool harvest the pane; replace it and verify identity.
8. Toggle edge/centred geometry and rotate it; verify identity and texture remain unchanged.
9. Confirm the dynamic frame BlockEntity has no ticker and no dedicated per-frame renderer.
10. Remove the contributing wood mod only in a disposable world and confirm the fallback does not
   crash; restore the mod before evaluating data preservation.

## Seamless frame rules

1. Join matching framed panes in horizontal/vertical runs, 2x2 grids, and 3x3 grids.
2. Confirm internal wood bands disappear completely and are replaced by the same glass colour and
   transparency as the pane interior.
3. Confirm the outside one-pixel band remains continuous around the window perimeter.
4. Join different fixed woods, different modded woods, and fixed versus modded representations;
   verify their complete borders remain.
5. Verify different glass materials never join even when frame wood matches.
6. Extend coplanar runs from L/cube/centred junctions; verify only flat continuations become
   seamless and angled mullions remain.
7. Disable/re-enable seamless rendering and verify immediate chunk rebuild with no stale pieces.
8. Repeat with Fabric rendering and Sodium/Iris, shaders off/on, and a resource pack.

## Geometry and tool regression

1. Click near all four edges of every block face and confirm normal placement chooses the nearest
   edge, including top/bottom edges on vertical faces and all four directions on horizontal faces.
2. Shift + right-click all six faces of full blocks, slabs/stairs, and existing Tempered panes;
   confirm the new pane always lies against the clicked face and never copies source orientation.
3. Rotate every edge and centred orientation around X/Y/Z.
4. Toggle single panes edge/centred; verify material, frame, water, and axis are preserved.
5. Build/reload edge L and cube corners; verify transparent intersections and shared mullions.
6. Build centred L and cube-corner junctions from one-sided sources. Confirm there are no opposite
   arms sticking through the centre unless a real pane source exists on that side.
7. Remove source panes and verify connection flags and one-sided arms clear without ghost geometry.
8. Confirm connected multi-plane centred panes refuse destructive conversion until sources are removed.
9. Verify copper/iron/diamond tier restrictions and legacy-tool compatibility.

## Water and harvesting regression

1. Waterlog clear, stained, tinted, fixed-framed, and modded-framed edge/centred panes.
2. Rotate, toggle, save/reload, bucket, and break them; verify one source state and correct drops.
3. Inspect all edge orientations plus L/cube corners; water must stop at every active pane face.
4. Confirm centred panes keep normal full-cell water rendering.
5. Repeat on Fabric and Sodium/Iris with shaders off/on.
6. With `Tempered panes never shatter` enabled, verify bare hands, wrong tools, Silk Touch, and the
   diamond tool each produce exactly one item, including the stored modded-frame component. Disable
   the setting and verify the former Silk Touch/diamond rule returns; Creative mode produces none.

## Dedicated-server and compatibility checks

1. Join with a client containing Fabric API and optionally Mod Menu; verify registry sync.
2. Verify server-authoritative tool recipe switches, intact-drop and reverse-recipe settings, and
   multiplayer placement.
3. Load a representative 0.1.8/beta.3 world and inspect saved orientations, centred connections,
   corners, tinted panes, waterlogging, and legacy tools.
4. Load a world created with the first beta.4 build and verify its fixed vanilla-wood IDs remain.
5. Record renderer, shader/resource-pack stack, pane material, frame source ID, geometry, direction,
   water state, and logs for every failure.
