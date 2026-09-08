# Ultimate Glass 0.2.3

## Manual edge-pane assemblies

- Add matching Tempered Glass panes to free outside faces of an already occupied pane cell.
  Each survival insertion consumes one pane; a cell can contain up to six panes.
- Build L shapes, cube corners, parallel pairs, U shapes, and fully enclosed six-face assemblies.
- Glass material and frame wood must match. Different colours, different woods, and framed versus
  unframed panes cannot combine.
- Edge faces no longer appear or disappear when neighbouring panes are placed or removed.
- Breaking an assembly returns every installed pane under the existing harvest settings.
  Creative still drops nothing; disabling intact drops retains the existing Silk Touch/tool rules.
- Glazier's Tools rotate the complete assembly. Only single-sheet edge panes can switch to centred.
- The Glass Chisel selects the sheet actually clicked, allowing parallel panes' boundaries to be
  shown or hidden independently. Thin-rim clicks also select the nearby sheet correctly.

## Placement controls

Normal clicks try the cursor-nearest edge of the existing cell. Shift uses that cell's clicked
face; Shift-clicking a sheet's inner face can add the opposite parallel sheet. If the selected
face is occupied or the materials differ, normal adjacent placement is still available.

Centred panes, slab/stair composites, seamless textures and the native water-rendering integration
keep their existing behaviour. No new rendering or shader dependency is introduced.

## Existing worlds

Back up your world before updating. Saved automatic corners retain their existing faces on load,
and each preserved face counts as a recoverable pane, including the previously free corner faces.
No manual rebuilding or full-world scan is needed.

Do not downgrade an updated world containing these assemblies: older versions cannot represent
opposite-face panes and will resume deriving corner geometry from neighbours. In multiplayer,
install the same mod version on both client and server.

Minecraft 26.2, Java 25, Fabric Loader 0.19.3 or newer, and Fabric API are required.
