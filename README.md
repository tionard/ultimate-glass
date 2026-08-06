# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2 that gives builders precise six-direction glass-pane placement, connected corners, waterlogging, and tiered glassworking tools.

## Version 0.1.5 features

- Normal placement creates a pane perpendicular to the clicked face on the side farthest from the player.
- Shift placement copies panes and other clearly oriented blocks.
- When no orientation can be copied, the default Shift mode lays the pane directly against the clicked face.
- The previous near-player Shift placement remains available in Mod Menu and through an unassigned keybind.
- Adjacent perpendicular panes create merged L-shaped and three-plane cube corners.
- Clear and all stained outside-face panes are waterloggable.
- Waterlogged corners render level source water clipped inside their pane walls, including with Sodium.

## Glazier's Tool tiers

- **Copper:** rotates custom panes with right-click.
- **Iron:** also switches panes between centred and outside-face placement with Shift + right-click.
- **Diamond:** also mines supported glass progressively and drops it intact.

All three recipes use one material, one string, and two sticks. They may be mirrored horizontally.

Existing 0.1.3 Glazier's Tools retain diamond-tier behavior for world compatibility but are hidden from new recipes and Creative tabs.

## Controls

- **Rotate pane:** right-click with any Glazier's Tool.
- **Change rotation axis:** V by default.
- **Switch outside face ↔ centred:** Shift + right-click with the iron or diamond tool.
- **Toggle Shift Placement Mode:** unassigned by default.
- **Mine glass intact:** break it normally with the diamond tool.

## Configuration

Mod Menu exposes:

- Shift placement mode: clicked face or near player
- Copper tool crafting enabled/disabled
- Iron tool crafting enabled/disabled
- Diamond tool crafting enabled/disabled

Crafting settings are server-authoritative. Disabling a recipe does not remove the item from commands or Creative mode.

All world changes are performed by the logical server. Ultimate Glass is required on both the server and every connecting client. Fabric API is required; Mod Menu is optional.

## Compatibility strategy

Ultimate Glass does not add properties to vanilla pane block states. Vanilla panes remain centred, while hidden mod-owned blocks represent outside-face panes and connected corner states. Existing vanilla panes therefore remain unchanged when the mod is installed.

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

## License

Ultimate Glass is available under the MIT License. See `LICENSE`.
