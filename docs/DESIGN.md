# Ultimate Glass — Initial Design

## Placement model

Glass panes support five logical positions: center, north edge, east edge, south edge, and west edge.

- Existing panes loaded from older worlds remain centered.
- Side-face placement chooses the corresponding block edge.
- Top/bottom placement chooses the nearest horizontal edge from the hit position.
- Centered panes preserve vanilla-style connections.
- Edge panes use a straight vertical plane and rotate clockwise through the four edges.

## Glazier's Tool

Working interaction design:

- Use on glass: collect the glass block or pane intact.
- Sneak + attack pane: toggle between center and edge placement.
- Sneak + use pane: rotate the edge position clockwise.

All world mutations are server-authoritative. Client code is limited to input handling, feedback, previews, and configuration UI.

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

## Implementation direction

The first prototype will establish custom pane state storage, shapes, placement rules, rotation/toggle packets, and game tests before adding polished assets and configuration screens.
