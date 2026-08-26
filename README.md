# Ultimate Glass

Ultimate Glass is a Fabric mod for Minecraft Java 26.2. It gives builders edge-aligned Tempered
Glass Panes, rotatable centred sheets, connected corners, wood framing, and tiered glassworking
tools.

## Version 0.2.2b

Version 0.2.2b is the second test build of the complete glass families. It keeps everything from
0.2.1 and adds Tempered full blocks plus ordinary and Tempered framed blocks and panes.

### 0.2.2b corrections

- Framed pane and block drops retain their exact glass material and plank instead of reverting to
  clear glass with oak.
- Framed pane items use normal pane-item sizing in the hand, inventory, and world.
- The Glass Chisel can edit the twelve physical wood edges of framed full glass blocks.
- Chisel edits on stair/slab composites rebuild immediately without requiring a reconnect.
- Tempered panes combine with an experimental stair/slab host only through Shift + right-click;
  ordinary right-click remains normal adjacent pane placement.

### Complete glass families

- Clear, all 16 stained colours, and tinted glass now have matching Tempered full blocks.
- Smelt one vanilla glass block into one Tempered block in a furnace, or use a blast furnace for
  the faster version.
- The default-on `Tempered glass never shatters` setting applies to panes and full blocks. The
  off-by-default reverse recipe likewise converts either unframed form back at a 1:1 ratio.
- Craft one ordinary vanilla-style pane or full glass block with one tagged plank to make a
  breakable framed version. Tempered panes and full blocks can be framed through the same recipe.
- Tinted ordinary frames retain tinted glass's normal intact-drop behaviour; other ordinary clear
  and stained frames need Silk Touch.
- Matching framed full blocks merge identical internal wood borders, producing clean multi-block
  windows. Different glass families or woods keep the divider between them.
- Framed stacks now use data components for both their glass material and exact plank block. The
  same six smart item families cover every colour, form, tempering state, and tagged wood without
  registering their full cross-product as inventory items.

### Manual pane edges

- The `Glass Chisel` edits the exact edge of the pane or framed full block that was clicked.
- Right-click changes the current visible result to its opposite. By default, both sides of a
  shared pane seam change together so borders cannot accidentally mismatch.
- Press `V` while holding the Glass Chisel to switch between paired-seam and single-edge editing. The
  binding is configurable in Minecraft's Controls menu.
- Single-edge mode changes only the clicked pane, allowing intentional mismatched borders.
- Shift + right-click clears every manual choice on the clicked pane and returns the whole pane to
  automatic seamless behavior.
- A forced seamless edge is allowed beside any block or open space; it does not require another
  compatible pane.
- Manual choices work on unframed, wood-framed, edge-bound, centered, and composite Tempered panes,
  plus framed full blocks, and survive save/reload.

### Creative inventory

- Ultimate Glass now has its own Creative tab containing its panes and glassworking tools.
- The tab lists the 18 Tempered pane materials, the 18 Tempered full-block materials, and one oak
  example of each framed family. Other woods and framed colours are produced by the universal
  recipe instead of flooding the tab with hundreds of combinations.
- The existing vanilla Building Blocks and Tools placements remain available as well.

### Stair and slab composites

- Enable `Experimental stair/slab panes` in Mod Menu (off by default), then Shift + right-click a
  stair or non-double slab with any Tempered pane to install it in the same block cell. A normal
  right-click places the pane beside the host instead.
- Stair facing, half, shape, slab half, waterlogging, pane material, and frame identity are stored.
- Composite panes use edge-bound placement against the Shift-clicked host face. Fully occupied
  stair faces fall back to ordinary pane placement instead of creating invisible composites.
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
- Legacy `ultimate_*` and fixed-frame registry IDs remain loadable during the 0.2.2 transition.
  New recipes, drops, and pick-block results use the component-backed items instead.

### Wood frames

Craft one supported pane or full glass block with one item in Minecraft's `#planks` tag to frame
it. Ordinary inputs produce ordinary breakable frames; Tempered inputs remain Tempered.

- Vanilla and modded planks follow the same tag-driven recipe and data path.
- A framed stack stores `glass_material` and `frame_block` components. After placement, the
  material selects the correct internal glass block and a non-ticking BlockEntity stores only the
  arbitrary plank identity plus existing seam data.
- Static geometry is still emitted into the normal chunk mesh; there is no per-frame
  BlockEntityRenderer.
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
the off-by-default experimental stair/slab feature, default-on intact Tempered-glass drops, the
off-by-default Tempered-to-vanilla recipe, and server-authoritative crafting switches for each
Glazier's Tool tier. Mod Menu is optional; Fabric API is required. The mod must be installed on the
server and every connecting client.

## Release plan

- `0.2.0-beta.4`: Tempered naming/progression and universal plank framing.
- `0.2.0-beta.5`: Stair/slab composite panes; final feature beta for 0.2.
- `0.2.0`: Stable release after beta.5 validation.
- `0.2.1a`: Alpha for manual pane seams and the Ultimate Glass Creative tab.
- `0.2.1`: Manual pane seams, paired/single-edge editing, whole-pane reset, and the Creative tab.
- `0.2.2a`: First alpha for complete ordinary/Tempered pane and full-block glass families.
- `0.2.2b`: Drop, item-scale, full-block chisel, composite refresh, and placement corrections.
- `0.2.2`: Stable complete-family update after alpha/beta player testing.
- `0.2.2` is also a transition release: fixed-combination IDs stay loadable but no longer appear in
  recipes or Creative. Existing legacy windows should be replaced before their later removal.
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
