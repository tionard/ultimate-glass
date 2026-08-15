# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2 that gives builders precise edge-aligned glass panes, rotatable centred full sheets, connected corners, and tiered glassworking tools.

## Version 0.2.0-beta.2

Beta.2 adds Ultimate Tinted Glass Panes to the beta.1 architecture. Tinted panes support the same
edge, centred, corner, rotation, waterlogging, seamless-rendering, and tool behavior as every other
Ultimate pane while retaining vanilla tinted glass's complete block-light and skylight dampening.

### Gameplay features

- Vanilla glass panes remain fully vanilla and place with their normal connected geometry.
- Clear and all 16 stained variants have separate Ultimate Glass Pane items.
- Tinted glass has a distinct Ultimate Tinted Glass Pane item and is not treated as a stain colour.
- Six tinted glass blocks craft 16 Ultimate Tinted Glass Panes in the vanilla pane pattern.
- One vanilla pane converts to one matching Ultimate Glass Pane in any crafting grid, and the recipe works in reverse.
- Ultimate Glass Panes initially place against an outside face of the block space.
- Normal placement selects the side farthest from the player; Shift placement can copy an existing orientation or use the configured clicked-face/near-player fallback.
- Adjacent perpendicular edge panes create merged L-shaped and three-plane cube corners.
- Iron and diamond Glazier's Tools toggle custom panes between outside-face and centred full-sheet geometry without involving vanilla panes.
- Centred panes are complete sheets rather than vanilla's single-pane rod and support X, Y, and Z orientations.
- Both custom geometries support native source-water waterlogging and preserve it through rotation and toggling.
- Water rendered inside edge panes is clipped at every active pane's inner face, including merged L-shaped and cube corners; centred panes keep the normal water render.
- Matching coplanar Ultimate panes remove their shared frame texture by default, while L-shaped and cube-corner junctions retain their solid outside edges.

Waterlogging uses Minecraft's standard block contract. For edge panes only, a client render hook clips the active renderer's water vertices at the pane's inner faces. Vanilla uses Minecraft's standard fluid tessellator, while Sodium keeps its native fluid mesh and material path so Iris can still classify and shade it as water. No global water handler is registered, and all other water—including centred-pane water—follows the renderer's unmodified normal path. Custom panes are also registered through Fabric's supported transparent-block fluid-overlay API.

## Glazier's Tool tiers

- **Copper:** rotates custom panes with right-click.
- **Iron:** also toggles custom panes between edge and centred geometry with Shift + right-click.
- **Diamond:** also mines supported glass progressively and drops it intact.

All three recipes use one material, one string, and two sticks. They may be mirrored horizontally.

Existing 0.1.3 Glazier's Tools retain diamond-tier behavior for world compatibility but are hidden from new recipes and Creative tabs.

## Controls

- **Rotate custom pane:** right-click with any Glazier's Tool.
- **Change rotation axis:** V by default.
- **Toggle edge/centred geometry:** Shift + right-click with the iron or diamond tool.
- **Toggle Shift Placement Mode:** unassigned by default.
- **Mine glass intact:** break it normally with the diamond tool.

## Configuration

Mod Menu exposes:

- Seamless connected panes enabled/disabled (enabled by default)
- Shift placement mode: clicked face or near player
- Copper tool crafting enabled/disabled
- Iron tool crafting enabled/disabled
- Diamond tool crafting enabled/disabled

The seamless-pane setting is client-side and purely visual. It joins only matching Ultimate pane variants with the same colour and geometry; vanilla panes and edge-to-centred neighbours remain unchanged. Crafting settings are server-authoritative. Disabling a tool recipe does not remove the item from commands or Creative mode.

All world changes are performed by the logical server. Ultimate Glass is required on both the server and every connecting client. Fabric API is required; Mod Menu is optional.

## Compatibility strategy

Ultimate Glass does not add properties to vanilla pane block states or intercept vanilla pane placement. Mod-owned blocks represent centred and outside-face geometry, and mod-owned items place those blocks. Convert custom panes back to vanilla before removing the mod from a world.

## Pane architecture

Ordinary panes remain normal blocks with no BlockEntities. Both edge and centred blocks expose a
common pane appearance and produce a `PaneGeometry` made from immutable `PanePlane` values. The
same geometry now drives outline/collision shapes, seamless continuation checks, rotation, and
edge-water clipping. `PaneMaterial` keeps clear, stained, and tinted glass logically separate.
Tinted panes remain ordinary blocks and explicitly reproduce vanilla tinted-glass light dampening.

The existing `FACING`, edge connection, `AXIS`, and `WATERLOGGED` properties are unchanged.

## Development baseline

- Minecraft Java 26.2
- Fabric Loader 0.19.3
- Fabric API 0.156.0+26.2
- Java 25
- Fabric Loom 1.17.19
- Mod Menu 20.0.1, optional at runtime

## Build

```bash
gradle build
```

The remapped mod JAR is created in `build/libs`.

The build includes unit tests for pane materials, plane sets, relative edge geometry, and rotation.

## License

Ultimate Glass is available under the MIT License. See `LICENSE`.
