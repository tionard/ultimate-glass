# Ultimate Glass — Design

## Placement model

Glass panes support seven logical forms: vanilla centred placement plus six custom outside-face placements: north, east, south, west, top, and bottom.

- Existing panes loaded from older worlds remain centred.
- Placing a pane against a block face chooses the opposite face inside the target block space.
- Clicking the top of a supporting block creates a bottom-aligned horizontal pane; clicking its underside creates a top-aligned pane.
- Centred panes preserve vanilla blocks and vanilla-style connections.
- Custom panes use one straight, two-pixel-thick plane matching their visible, outline, support, and collision shape.

## Rotation model

The client exposes an unassigned `Change Rotation Axis` keybind.

- The selected axis is synchronized to the server for the current player.
- The axis cycles X → Y → Z.
- Y is selected when a player first joins, preserving the original horizontal clockwise rotation behavior.
- Right-clicking a custom pane rotates its facing direction 90° around the selected axis.
- A pane parallel to the selected axis remains unchanged for that click.

## World compatibility

Ultimate Glass does not inject additional properties into vanilla pane block states.

- A centred pane remains the original vanilla pane block.
- A custom pane is represented by a hidden Ultimate Glass block variant containing its six-way facing and waterlogged state.
- Each clear or stained custom variant maps back to the corresponding vanilla pane item.
- Removing the mod therefore requires converting custom panes back to centred panes first; existing vanilla panes and unrelated world data are not rewritten.

This design avoids changing the serialized state definition of vanilla blocks and keeps pre-existing builds unchanged when the mod is installed.

## Waterlogging

Every custom pane implements the standard waterlogged block contract.

- A pane placed into water stores a source-water fluid state.
- Rotation and centred/custom conversion preserve waterlogging.
- The pane's thin collision plane acts as the barrier across its covered face while the rest of the block space remains unoccupied.
- Directional flow behavior requires gameplay validation because Minecraft fluid propagation depends on neighbouring shapes and fluid ticks.

## Glazier's Tool

Working interaction design:

- Mine glass normally: progressive breaking speed with an intact Silk Touch-like drop.
- Right-click a custom pane: rotate it 90° around the selected axis.
- Sneak + right-click a pane: toggle between centred and outside-face placement.

The tool no longer instantly removes glass. Intact drops are substituted only after normal block breaking completes.

All world mutations are server-authoritative. Client code is limited to key handling, configuration, rendering, and sending the selected rotation axis.

## Configuration

Client:

- Enable/disable Glazier's Tool interactions through Mod Menu.
- Persist the setting in `ultimate-glass-client.json`.
- Configure the rotation-axis key through Minecraft's normal keybind screen.

The old enable/disable gameplay keybind is intentionally removed.

## Validation stages

1. Compile six-way block states, waterlogging, networking, and custom drops.
2. Validate vertical and horizontal placement/model rotations.
3. Validate X/Y/Z rotation behavior in singleplayer and multiplayer.
4. Validate progressive mining and intact drops for every supported glass variant.
5. Validate directional fluid blocking and state persistence.
6. Add automated interaction tests and migration safeguards.
