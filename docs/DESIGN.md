# Ultimate Glass — Design

## Placement model

Glass panes support seven logical forms: vanilla centred placement plus six custom outside-face placements: north, east, south, west, top, and bottom.

### Normal placement

- The pane plane is perpendicular to the face the player clicked.
- The clicked face removes one axis from consideration; the player's strongest position component on the remaining two axes selects the near side.
- Normal placement uses the opposite, far side.
- Standing south and clicking a top face therefore creates a vertical pane on the north edge.
- Standing below and clicking a vertical face creates a horizontal pane on the top edge.

### Shift placement

Shift is a deliberate placement modifier.

- A custom pane copies its six-way facing.
- A top or bottom slab copies `up` or `down`; a double slab has no usable orientation.
- Closed trapdoors use their top/bottom half. Open trapdoors use their horizontal facing.
- Stairs use their half when a horizontal face is clicked and their horizontal facing when a vertical face is clicked.
- Other blocks with an unambiguous `facing`, `horizontal_facing`, or `half` state can provide orientation.
- AXIS-only blocks such as logs and pillars are ignored because an axis describes a line, not one unambiguous pane face.
- When no usable source orientation exists, placement remains perpendicular to the clicked face but uses the near side rather than the far side.

Existing panes loaded from older worlds remain centred. Centred panes preserve vanilla blocks and vanilla-style connections. Custom panes use one straight, two-pixel-thick plane matching their visible, outline, support, and collision shape.

## Outer-corner model

Only convex outer corners connect.

For a pane at position `p`, facing outward along direction `n`, one of its local edge directions is `e`. It connects across that edge only when another custom pane exists at the diagonal position `p + n + e` and that pane faces `-e`.

This rule is symmetric and deliberately excludes concave inner corners, which are side-adjacent rather than diagonally adjacent in the required arrangement.

Connection state is stored as four pane-local booleans: top, bottom, left, and right. Rendering is assembled from:

- one translucent plate model containing both broad glass faces;
- four independent opaque end-cap models;
- multipart block states that omit an end cap only when its corresponding outer connection is active.

The collision and selection geometry remains the pane's normal thin plane. Two connected planes meet at their shared sharp edge, so no additional protruding collision element is necessary.

## Rotation model

The client exposes `Change Rotation Axis`, bound to `V` by default.

- The selected axis is synchronized to the server for the current player.
- The axis cycles X → Y → Z.
- Y is selected when a player first joins, preserving the original horizontal clockwise rotation behavior.
- Right-clicking a custom pane rotates its facing direction 90° around the selected axis.
- A pane parallel to the selected axis remains unchanged for that click.
- Rotation recalculates outer-corner connections for the changed pane and nearby panes.

## World compatibility

Ultimate Glass does not inject additional properties into vanilla pane block states.

- A centred pane remains the original vanilla pane block.
- A custom pane is represented by a hidden Ultimate Glass block variant containing its six-way facing, waterlogged state, and derived outer-corner booleans.
- Each clear or stained custom variant maps back to the corresponding vanilla pane item.
- Removing the mod therefore requires converting custom panes back to centred panes first; existing vanilla panes and unrelated world data are not rewritten.

This design avoids changing the serialized state definition of vanilla blocks and keeps pre-existing builds unchanged when the mod is installed.

## Waterlogging

Every custom pane implements the standard waterlogged block contract.

- A pane placed into water stores a source-water fluid state.
- Rotation and centred/custom conversion preserve waterlogging.
- Outer-corner rendering state does not alter the fluid state.
- The pane's thin collision plane acts as the barrier across its covered face while the rest of the block space remains unoccupied.
- Directional flow behavior requires gameplay validation because Minecraft fluid propagation depends on neighbouring shapes and fluid ticks.

## Glazier's Tool

Working interaction design:

- Mine glass normally: progressive breaking speed with an intact Silk Touch-like drop.
- Right-click a custom pane: rotate it 90° around the selected axis.
- Sneak + right-click a pane: toggle between centred and outside-face placement.

The tool does not instantly remove glass. Intact drops are substituted only after normal block breaking completes.

All world mutations are server-authoritative. Client code is limited to key handling, configuration, rendering, and sending the selected rotation axis.

## Configuration

Client:

- Enable/disable Glazier's Tool interactions through Mod Menu.
- Persist the setting in `ultimate-glass-client.json`.
- Configure the rotation-axis key through Minecraft's normal keybind screen.

The old enable/disable gameplay keybind is intentionally removed.

## Validation stages

1. Compile the placement resolver, six-way states, generated multipart models, networking, waterlogging, and custom drops.
2. Validate perpendicular far-side placement from every clicked face and player direction.
3. Validate Shift orientation copying and near-side fallback for all supported source-state families.
4. Validate outer-corner detection, end-cap removal/restoration, and inner-corner exclusion.
5. Validate X/Y/Z rotation behavior and corner recalculation in singleplayer and multiplayer.
6. Validate progressive mining and intact drops for every supported glass variant.
7. Validate directional fluid blocking and state persistence.
8. Add automated interaction tests and migration safeguards.
