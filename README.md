# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2 that gives builders more control over glass panes and adds a dedicated glassworking tool.

## Current prototype

- Normal and stained glass panes place on the nearest block edge instead of the block centre.
- Existing vanilla panes remain centred when old worlds are loaded.
- Edge panes have matching outline and collision shapes for north, east, south, and west placement.
- The Glazier's Tool can collect glass blocks and panes intact.
- Sneak + right-click rotates an edge pane clockwise.
- Sneak + left-click toggles a pane between its edge and vanilla centred form.
- A client keybind enables or disables Glazier's Tool interactions. The default key is `G`.
- The setting is persisted in `config/ultimate-glass-client.json`.
- Mod Menu 20.0.1 integration provides the same enable/disable option through a settings screen.

All world changes are performed by the logical server. Because edge panes are custom synchronized blocks, Ultimate Glass is required on both the server and every connecting client.

## Compatibility strategy

Ultimate Glass does not add properties to vanilla pane block states. Vanilla panes represent the centred form, while mod-owned hidden block variants represent the four edge positions. This keeps existing worlds compatible and prevents vanilla pane state serialization from changing.

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

The project compiles successfully in GitHub Actions and produces a test artifact. The next milestone is an in-game client/server validation pass followed by fixes for placement feel, rendering, recipes, and balancing.
