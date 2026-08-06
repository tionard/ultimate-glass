# Ultimate Glass — Design

## Placement model

Glass panes support vanilla centred placement plus six custom outside-face placements: north, east, south, west, top, and bottom.

### Normal placement

- The pane is perpendicular to the clicked face.
- The player's position is projected onto that face.
- The outside-face position farthest from the player is selected.
- Example: standing south and clicking a top face creates a vertical pane on the north edge.
- Example: standing below and clicking a vertical face creates a horizontal pane on the top edge.

### Shift placement

- A clearly oriented clicked block supplies the pane orientation without using the player's position.
- Supported references include custom panes, slabs, stairs, trapdoors, doors, and blocks exposing a clear facing or half.
- AXIS-only logs and pillars are intentionally ignored because an axis does not identify one unambiguous face.
- When no supported orientation is available, placement remains perpendicular but selects the side closest to the player.

## Outer-corner connections

Custom panes may add generated geometry when source panes form a convex outside corner.

### Two-plane corner

- The first perpendicular pane is found in the next block around the outside face.
- The receiving block renders its original pane and one perpendicular wing.
- The result is one continuous L-shaped corner spanning the adjacent blocks.

### Three-plane cube corner

- After finding the first perpendicular pane, the resolver may continue one step farther around that pane's outside face.
- A second perpendicular pane on the third axis activates a second wing.
- The receiving block then contains three mutually perpendicular pane planes.
- The connection refresh radius is two blocks so placement, breaking, rotation, and conversion update the complete chain.

Concave inner arrangements do not use this outside chain and therefore remain unconnected.

## Merged corner models

Connection geometry is generated as one complete model for each of the 16 local connection masks rather than layering independent full panes.

- Every transparent pane plane is shortened by two pixels at each shared edge.
- Every pair of perpendicular panes receives exactly one opaque shared frame line.
- When three panes meet, their three frame lines terminate at one opaque 2×2×2 corner block.
- Normal outside frame surfaces remain on edges without a connection.
- This prevents overlapping transparent surfaces, doubled caps, missing inner lines, and frame pieces passing through another pane.
- Outline, support, occlusion, and collision shapes use the same active pane planes.

The 16 base models are shared by all clear and stained variants through texture-substituting child models. Each six-way blockstate selects one exact model based on `connect_top`, `connect_bottom`, `connect_left`, and `connect_right`.

## Rotation model

The client exposes `Change Rotation Axis`, assigned to V by default.

- The selected axis is synchronized to the server for the current player.
- The axis cycles X → Y → Z.
- Y is selected when a player first joins.
- Right-clicking a custom pane rotates its facing direction 90° around the selected axis.
- A pane parallel to the selected axis remains unchanged for that click.
- Rotation refreshes nearby two- and three-plane connections.

## World compatibility

Ultimate Glass does not inject additional properties into vanilla pane block states.

- A centred pane remains the original vanilla pane block.
- A custom pane is represented by a hidden Ultimate Glass block variant containing facing, waterlogged, and connection properties.
- Each clear or stained custom variant maps back to the corresponding vanilla pane item.
- Removing the mod therefore requires converting custom panes back to centred panes first; existing vanilla panes and unrelated world data are not rewritten.

## Waterlogging

Every custom pane implements the standard waterlogged block contract.

- A pane placed into water stores a source-water fluid state.
- Rotation and centred/custom conversion preserve waterlogging.
- Active pane planes contribute to the block's collision and occlusion geometry.
- Directional flow behavior still requires gameplay validation because Minecraft fluid propagation depends on neighbouring shapes and fluid ticks.

## Glazier's Tool

- Mine glass normally: progressive breaking speed with an intact Silk Touch-like drop.
- Right-click a custom pane: rotate it 90° around the selected axis.
- Sneak + right-click a pane: toggle between centred and outside-face placement.

All world mutations are server-authoritative. Client code is limited to key handling, configuration, rendering, and sending the selected rotation axis.

## Configuration

Client:

- Enable/disable Glazier's Tool interactions through Mod Menu.
- Persist the setting in `ultimate-glass-client.json`.
- Configure the rotation-axis key through Minecraft's normal keybind screen.

There is no gameplay keybind for enabling or disabling the tool.

## Validation stages

1. Validate normal and Shift placement from all six clicked faces.
2. Validate supported orientation references and AXIS-only exclusions.
3. Validate two-plane L corners and three-plane cube corners.
4. Validate merged frame lines and transparent geometry from every viewing direction.
5. Validate connection recalculation after placement, breaking, rotation, and conversion.
6. Validate waterlogging, multiplayer synchronization, and every stained variant.
