# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2 that gives builders more control over glass panes and adds a dedicated glassworking tool.

## Current prototype

- Clear and stained glass panes can occupy any outside face of a block space: north, east, south, west, top, or bottom.
- Normal placement creates a pane perpendicular to the clicked face and positions it on the side farthest from the player.
- Shift-placement copies the orientation of a supported oriented block. When no usable orientation exists, it uses the normal perpendicular plane but chooses the side closest to the player.
- Custom panes, slabs, stairs, trapdoors, doors, and directional blocks can act as orientation references. AXIS-only logs and pillars are intentionally ignored.
- Perpendicular panes automatically remove their touching end caps when they meet as a convex outer corner. Concave inner corners remain unconnected.
- Existing vanilla panes remain centred when old worlds are loaded.
- Custom panes have matching visual, outline, and collision planes in all six orientations.
- Custom pane variants are waterloggable.
- The Glazier's Tool mines glass progressively and drops glass blocks and panes intact, like a dedicated Silk Touch tool.
- Right-clicking a custom pane rotates it 90° around the selected X, Y, or Z axis.
- `Change Rotation Axis` defaults to `V`, cycles X → Y → Z, and starts on Y when joining a world.
- Sneak + right-click with the Glazier's Tool toggles a pane between its outside-face and vanilla centred form while preserving waterlogging.
- Glazier's Tool interactions can be enabled or disabled through Mod Menu. There is no gameplay enable/disable keybind.
- The setting is persisted in `config/ultimate-glass-client.json`.

All world changes are performed by the logical server. Because outside-face panes are custom synchronized blocks, Ultimate Glass is required on both the server and every connecting client.

## Controls

- **Place away from the player:** place a pane normally.
- **Copy orientation or place near the player:** hold Shift while placing a pane.
- **Mine glass:** hold the Glazier's Tool and break the block normally.
- **Rotate pane:** right-click a custom pane with the Glazier's Tool.
- **Change rotation axis:** press `V` by default, configurable under Controls → Key Binds → Ultimate Glass.
- **Switch outside face ↔ centred:** sneak + right-click a pane with the Glazier's Tool.
- **Enable/disable tool interactions:** use the Ultimate Glass settings screen in Mod Menu.

## Compatibility strategy

Ultimate Glass does not add properties to vanilla pane block states. Vanilla panes represent the centred form, while mod-owned hidden block variants represent the six outside faces and their outer-corner rendering state. This keeps existing worlds compatible and prevents vanilla pane state serialization from changing.

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

Version 0.1.3 is under gameplay validation. The automated build and dedicated-server startup checks cover compilation and server compatibility; placement feel, Shift orientation copying, and outer-corner rendering require in-game testing before merge.
