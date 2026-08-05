# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2 that gives builders more control over glass panes and adds a dedicated glassworking tool.

## Current prototype

- Clear and stained glass panes can occupy any outside face of a block space: north, east, south, west, top, or bottom.
- Existing vanilla panes remain centred when old worlds are loaded.
- Custom panes have matching visual, outline, and collision planes in all six orientations.
- Custom pane variants are waterloggable.
- The Glazier's Tool mines glass progressively and drops glass blocks and panes intact, like a dedicated Silk Touch tool.
- Right-clicking a custom pane rotates it 90° around the selected X, Y, or Z axis.
- The unassigned `Change Rotation Axis` keybind cycles X → Y → Z. The selected axis starts at Y when joining a world.
- Sneak + right-click toggles a pane between its outside-face and vanilla centred form while preserving waterlogging.
- Glazier's Tool interactions can be enabled or disabled through Mod Menu. There is no gameplay toggle keybind.
- The setting is persisted in `config/ultimate-glass-client.json`.

All world changes are performed by the logical server. Because outside-face panes are custom synchronized blocks, Ultimate Glass is required on both the server and every connecting client.

## Controls

- **Mine glass:** hold the Glazier's Tool and break the block normally.
- **Rotate pane:** right-click a custom pane.
- **Change rotation axis:** assign `Change Rotation Axis` under Controls → Key Binds → Ultimate Glass.
- **Switch outside face ↔ centred:** sneak + right-click a pane.
- **Enable/disable tool interactions:** use the Ultimate Glass settings screen in Mod Menu.

## Compatibility strategy

Ultimate Glass does not add properties to vanilla pane block states. Vanilla panes represent the centred form, while mod-owned hidden block variants represent the six outside faces. This keeps existing worlds compatible and prevents vanilla pane state serialization from changing.

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

The project builds in GitHub Actions and produces test artifacts. Horizontal placement, axis rotation, progressive glass mining, and directional waterlogging still require an in-game client/server validation pass before release.
