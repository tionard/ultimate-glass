# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2 that gives builders more control over glass panes and adds a dedicated glassworking tool.

## Version 0.1.3 test features

- Normal pane placement is perpendicular to the clicked face and chooses the outside edge farthest from the player.
- Shift placement copies a supported clicked block orientation; without one, it chooses the perpendicular edge closest to the player.
- Logs and pillars with only an axis are intentionally ignored as orientation references.
- Adjacent perpendicular outside panes form a continuous L-shaped corner.
- Three mutually perpendicular outside panes can form a complete cube corner with three generated pane planes.
- Connection models trim transparent panes at shared edges, use one shared frame line per pane pair, and use one small frame block where three planes meet.
- The Glazier's Tool mines glass progressively and drops it intact.
- Right-click rotates custom panes 90° around the selected X, Y, or Z axis.
- `Change Rotation Axis` is assigned to V by default.
- Sneak + right-click with the tool toggles outside-face and centred panes while preserving waterlogging.
- Glazier's Tool interactions can be enabled or disabled only through Mod Menu.

All world changes are performed by the logical server. Ultimate Glass is required on both the server and every connecting client.

## Placement examples

- Stand south and click a block's top face: a vertical pane appears on the north edge.
- Stand below and click a block's side: a horizontal pane appears on the top edge.
- Shift-click a pane, stair, slab, trapdoor, door, or clearly facing block: the new pane copies the relevant orientation.
- Shift-click an ordinary block: the same perpendicular rule is used, but the near edge is selected.

## Controls

- **Mine glass:** break it normally while holding the Glazier's Tool.
- **Rotate pane:** right-click a custom pane.
- **Change rotation axis:** V by default under Controls → Key Binds → Ultimate Glass.
- **Switch outside face ↔ centred:** sneak + right-click a pane with the tool.
- **Enable/disable tool interactions:** use Ultimate Glass settings in Mod Menu.

## Compatibility strategy

Ultimate Glass does not add properties to vanilla pane block states. Vanilla panes represent the centred form, while hidden mod-owned blocks represent outside-face panes and their generated corner state. Existing vanilla panes therefore remain unchanged when the mod is installed.

## Development baseline

- Minecraft Java 26.2
- Fabric Loader 0.19.3
- Fabric API 0.156.0+26.2
- Java 25
- Fabric Loom 1.17
- Mod Menu 20.0.1, optional at runtime

## Build

```bash
gradle build
```

The remapped mod JAR is created in `build/libs`.

## Status

The project builds in GitHub Actions and passes its dedicated-server startup smoke test. The 0.1.3 placement and two-/three-plane corner behavior remains in draft testing before merge.
