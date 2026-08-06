# Ultimate Glass — Design

## Placement

Glass panes support vanilla centred placement plus six custom outside-face placements.

Normal placement projects the player's position onto the clicked face and selects a perpendicular outside edge on the far side.

Shift placement first attempts to copy a clear orientation from a pane, slab, stair, trapdoor, door, or facing/half block. AXIS-only logs and pillars remain ignored. When no orientation is available, the player-selected fallback is used:

- **Clicked face:** place the pane flush against the clicked surface. This is the 0.1.4 default.
- **Near player:** preserve the previous 0.1.3 perpendicular near-edge behavior.

The client persists the selected mode, sends it to the logical server, and may toggle it through Mod Menu or an unassigned keybind.

## Connected corners

Convex outside corners use generated merged geometry:

- two perpendicular source panes create an L-shaped corner;
- three mutually perpendicular panes create a complete cube corner;
- transparent planes are trimmed at intersections;
- each pane pair has one shared frame line;
- three frame lines meet in one 2×2×2 corner block;
- outline and collision geometry include every active plane.

Concave inner arrangements remain unconnected.

## Rotation

`Change Rotation Axis` is assigned to V by default and cycles X → Y → Z. Right-clicking with any Glazier's Tool rotates a custom pane 90° around the selected axis. Rotation and conversion refresh nearby corner states.

## Tool tiers

- **Copper:** rotation only.
- **Iron:** rotation plus centred/outside-face conversion.
- **Diamond:** iron abilities plus progressive, intact glass harvesting.

The legacy 0.1.3 item ID remains registered with diamond-tier behavior to protect existing worlds, but is not newly craftable or shown in Creative tabs.

Each tier uses a custom server-checked recipe with one tier material, one string, and two sticks. Mirrored patterns are accepted.

## Configuration

The old global tool-interaction switch is removed.

Client setting:

- Shift placement mode.

Server-authoritative settings:

- copper recipe enabled;
- iron recipe enabled;
- diamond recipe enabled.

Disabled recipes still leave their items available through commands and Creative mode.

## Waterlogging

Every custom pane stores a source-water fluid state when waterlogged. Rotation and centred/outside conversion preserve it.

Connected pane collision still contains all active planes. The custom connected occlusion override is removed so L-shaped and three-plane states do not cause the fluid renderer to treat their internal source water as lowered or flowing. Gameplay validation must confirm both a flat source surface and directional blocking across every active pane plane.

## World compatibility

Vanilla panes remain vanilla blocks. Custom outside-face panes use hidden Ultimate Glass blocks with facing, waterlogged, and connection properties. Removing the mod safely requires converting custom panes back to centred panes first.
