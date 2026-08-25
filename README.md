# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2. It gives builders edge-aligned Tempered
Glass Panes, rotatable centred sheets, connected corners, wood framing, and tiered glassworking
tools.

## Version 0.2.1

Version 0.2.1 adds manual pane-edge control and the Ultimate Glass Creative tab. The
complete-glass-family expansion is planned separately for 0.2.2.

### Manual pane edges

- The `Glass Chisel` edits the exact edge of the pane block that was clicked.
- Right-click changes the current visible result to its opposite. By default, both sides of a
  shared pane seam change together so borders cannot accidentally mismatch.
- Press `V` while holding the Glass Chisel to switch between paired-seam and single-edge editing. The
  binding is configurable in Minecraft's Controls menu.
- Single-edge mode changes only the clicked pane, allowing intentional mismatched borders.
- Shift + right-click clears every manual choice on the clicked pane and returns the whole pane to
  automatic seamless behavior.
- A forced seamless edge is allowed beside any block or open space; it does not require another
  compatible pane.
- Manual choices work on unframed, wood-framed, edge-bound, centered, and composite Tempered panes
  and survive save/reload.

### Creative inventory

- Ultimate Glass now has its own Creative tab containing its panes and glassworking tools.
- The existing vanilla Building Blocks and Tools placements remain available as well.

### Stair and slab composites

- Enable `Experimental stair/slab panes` in Mod Menu (off by default), then use any Tempered pane on
  a stair or non-double slab to install it in the same block cell.
- Stair facing, half, shape, slab half, waterlogging, pane material, and frame identity are stored.
- Composite panes use edge-bound placement. Normal use selects the nearest edge of the clicked
  face; Shift use copies the clicked face exactly. Fully occupied stair faces fall back to ordinary
  pane placement instead of creating invisible composites.
- The renderer emits only pane sections outside the host volume, preventing glass from bleeding or
  flickering through stair/slab geometry.
- Collision and outline combine the real host shape with only the pane volume outside that host.
- Composite collision is evaluated dynamically, so the stored stair/slab keeps its normal hopper
  pass-through behaviour instead of being cached as a full cube.
- Right-click with any Glazier's Tool rotates the installed glass clockwise to the next position
  with exposed glass, skipping stair faces completely occupied by the host.
- Shift + right-click with an iron or diamond Glazier's Tool toggles the installed pane between its
  edge-bound and centred forms without replacing the host.
- Normal breaking always returns the host stair/slab item and the exact pane, including a modded
  frame identity, regardless of the tool used.
- Double slabs and hosts with their own BlockEntity are deliberately rejected in this beta.

### Pane progression

- Vanilla clear and stained panes retain their normal connected-pane behaviour.
- Six tinted-glass blocks craft 16 `Tinted Glass Pane` items. This new pane behaves like a normal
  vanilla-style connected pane and preserves tinted-glass light blocking.
- Smelting or blast-smelting any vanilla clear/stained pane produces the matching `Tempered` pane.
- Smelting or blast-smelting the new tinted pane produces a `Tempered Tinted Glass Pane`.
- An optional, off-by-default shapeless recipe converts one unframed Tempered pane back to its
  matching vanilla-style pane at a 1:1 ratio.
- Internal `ultimate_*` registry IDs are intentionally retained so existing worlds do not lose
  their blocks or items. The mod is Ultimate Glass; the enhanced panes are named Tempered.

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
- Centred sheets support X, Y, Z, one-sided L junctions, and three-plane cube corners. Angled
  connections stop at the centre instead of adding unsupported arms through the block.
- Copper rotates; iron also toggles geometry; diamond additionally mines supported glass faster.
- Edge and centred panes support native source-water waterlogging. Edge water is clipped at every
  active pane face, including connected corners.
- Seamless rendering removes only matching coplanar borders and keeps each glass material's colour
  and transparency at the join.

## Controls

- Rotate a pane: right-click with any Glazier's Tool; composites skip fully hidden positions.
- Change rotation axis: `V` by default while holding a Glazier's Tool.
- Toggle paired/single-edge Glass Chisel mode: `V` by default while holding the Chisel.
- Toggle edge/centred geometry: Shift + right-click with iron or diamond.
- Place normally: right-click; the cursor's nearest edge on the clicked face selects the pane edge.
- Force a clicked face: Shift + right-click while holding a Tempered pane.
- Mine Tempered glass intact: any tool or bare hand by default; this can be disabled in Mod Menu.

## Configuration

Mod Menu exposes seamless connected panes, a default-on server switch for the Glass Chisel,
the off-by-default experimental stair/slab feature, default-on intact Tempered-pane drops, the
off-by-default Tempered-to-vanilla recipe, and server-authoritative crafting switches for each
Glazier's Tool tier. Mod Menu is optional; Fabric API is required. The mod must be installed on the
server and every connecting client.

## Release plan

- `0.2.0-beta.4`: Tempered naming/progression and universal plank framing.
- `0.2.0-beta.5`: Stair/slab composite panes; final feature beta for 0.2.
- `0.2.0`: Stable release after beta.5 validation.
- `0.2.1a`: Alpha for manual pane seams and the Ultimate Glass Creative tab.
- `0.2.1`: Manual pane seams, paired/single-edge editing, whole-pane reset, and the Creative tab.
- `0.2.2`: Planned complete glass families, to be specified after 0.2.1 testing.
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

Ultimate Glass is available under the MIT License. See `LICENSE`.
