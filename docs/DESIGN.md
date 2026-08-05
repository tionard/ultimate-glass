# Ultimate Glass — Initial Design

## Placement model

Glass panes support five logical positions: center, north edge, east edge, south edge, and west edge.

- Existing panes loaded from older worlds remain centered.
- Side-face placement chooses the inside edge of the target block space.
- Top/bottom placement chooses the nearest horizontal edge from the hit position.
- Centered panes preserve vanilla blocks and vanilla-style connections.
- Edge panes use a straight vertical plane and rotate clockwise through the four edges.

## World compatibility

Ultimate Glass does not inject additional properties into vanilla pane block states.

- A centered pane remains the original vanilla pane block.
- An edge pane is represented by a hidden Ultimate Glass block variant that remembers its horizontal edge.
- Each clear or stained edge variant maps back to the corresponding vanilla pane item.
- Removing the mod therefore requires converting edge panes back to centered panes first; existing vanilla panes and unrelated world data are not rewritten.

This design avoids changing the serialized state definition of vanilla blocks and keeps pre-existing builds unchanged when the mod is installed.

## Glazier's Tool

Working interaction design:

- Left-click glass: collect the glass block or pane intact.
- Right-click an edge pane: rotate the edge position clockwise.
- Sneak + right-click a pane: toggle between center and edge placement.

All world mutations are server-authoritative. Client code is limited to input handling, feedback, previews, rendering setup, and configuration UI.

## Configuration

Client:

- Enable/disable tool controls.
- Placement preview.
- Configurable keybind.

Server:

- Enable/disable tool functionality.
- Recipe and durability behavior.
- Supported glass block tags.
- Permission checks.

## Prototype stages

1. Register edge-pane variants and placement interception.
2. Implement tool collection, center toggle, and rotation.
3. Add transparent models for every vanilla pane color.
4. Add client keybind, Mod Menu screen, and synchronized server policy.
5. Add automated interaction tests and migration safeguards.
