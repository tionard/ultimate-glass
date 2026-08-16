# Tempered Glass

Tempered Glass (internally `ultimateglass` for world compatibility) is a Fabric mod for Minecraft
Java 26.2. It gives builders edge-aligned glass panes, rotatable centred sheets, connected corners,
wood framing, and tiered glassworking tools.

## Version 0.2.0-beta.5

Beta.5 adds composite panes: a Tempered pane can occupy the open portion of a stair or single slab
without deleting the original host state. Beta.4's wood framing and furnace/blast-furnace tempering
remain unchanged.

### Stair and slab composites

- Use any Tempered pane on a stair or non-double slab to install it in the same block cell.
- Stair facing, half, shape, slab half, waterlogging, pane material, and frame identity are stored.
- The original host model and pane model are emitted into the cached chunk mesh; composites have no
  ticking logic and no per-frame BlockEntityRenderer.
- Collision and outline combine the real host shape with only the pane volume outside that host.
- Shift + right-click with an iron or diamond Glazier's Tool removes the pane, restores the exact
  host state, and returns the pane item.
- Normal breaking evaluates host drops with the original host state. The pane follows the existing
  diamond-tool recovery rule.
- Double slabs and hosts with their own BlockEntity are deliberately rejected in this beta.

### Pane progression

- Vanilla clear and stained panes retain their normal connected-pane behaviour.
- Six tinted-glass blocks craft 16 `Tinted Glass Pane` items. This new pane behaves like a normal
  vanilla-style connected pane and preserves tinted-glass light blocking.
- Smelting or blast-smelting any vanilla clear/stained pane produces the matching `Tempered` pane.
- Smelting or blast-smelting the new tinted pane produces a `Tempered Tinted Glass Pane`.
- Internal `ultimate_*` registry IDs are intentionally retained so existing worlds do not lose
  their blocks or items; only the player-facing branding changed to Tempered.

### Wood frames

Craft one Tempered pane with one item in Minecraft's `#planks` tag to frame it.

- All 12 Minecraft 26.2 plank species have fixed, BlockEntity-free variants.
- Planks added by other mods are accepted through the same tag-driven recipe.
- A modded frame stores only its plank block ID in a non-ticking BlockEntity. Its static geometry
  is still emitted into the normal chunk mesh; there is no per-frame BlockEntityRenderer.
- The frame uses the plank block model's particle texture, so resource packs and mod-provided wood
  textures carry through automatically.
- Wood covers the thin outside frame and the one-pixel band immediately inside each broad face.
- Matching glass and matching frame identity are both required for a seamless connection. Internal
  frame bands disappear; exposed borders and angled junction mullions remain.

### Existing geometry and tools

- Tempered panes place against any outside face, including horizontal orientations.
- Adjacent edge panes form merged L-shaped and three-plane corners.
- Iron and diamond Glazier's Tools toggle panes between outside-face and centred full-sheet forms.
- Centred sheets support X, Y, Z, pairwise junctions, and XYZ junctions.
- Copper rotates; iron also toggles geometry; diamond also harvests supported glass intact.
- Edge and centred panes support native source-water waterlogging. Edge water is clipped at every
  active pane face, including connected corners.
- Seamless rendering removes only matching coplanar borders and keeps each glass material's colour
  and transparency at the join.

## Controls

- Rotate a pane: right-click with any Glazier's Tool.
- Change rotation axis: `V` by default.
- Toggle edge/centred geometry: Shift + right-click with iron or diamond.
- Toggle Shift placement mode: unassigned by default.
- Mine glass intact: use the diamond tool.

## Configuration

Mod Menu exposes seamless connected panes, Shift placement mode, and server-authoritative crafting
switches for each Glazier's Tool tier. Mod Menu is optional; Fabric API is required. The mod must be
installed on the server and every connecting client.

## Release plan

- `0.2.0-beta.4`: Tempered naming/progression and universal plank framing.
- `0.2.0-beta.5`: Stair/slab composite panes; final planned feature beta for 0.2 (this iteration).
- `0.2.0`: Release after beta.5 validation, with no additional 0.2 feature betas planned.
- Mosaic foundation, layered mosaics, and the broader integration/performance phase previously
  labelled beta.6-beta.8 move to the next feature release, currently targeted as 0.3.0.

## Development baseline

- Minecraft Java 26.2
- Fabric Loader 0.19.3
- Fabric API 0.156.0+26.2
- Java 25
- Fabric Loom 1.17.19
- Mod Menu 20.0.1 (optional at runtime)

Build with:

```bash
gradle build
```

The remapped JAR is created in `build/libs`.

## License

Tempered Glass is available under the MIT License. See `LICENSE`.
