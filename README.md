# Ultimate Glass

Ultimate Glass makes glass panes and glass blocks much more flexible for building.

Place panes along block edges, mount them horizontally, rotate them after placement, make proper
glass corners, add wooden frames, build larger framed windows, and even combine glass with stairs
and slabs.

It stays close to the vanilla look — just with glass that is much less annoying to build with.


<details>
<summary>Tempered Glass</summary>

Smelt vanilla glass panes or full glass blocks in a furnace or blast furnace to create Tempered
Glass.

![Crafting Recipe](https://cdn.modrinth.com/data/cached_images/6de7d3f0697c7bede951f62c610287f6060b2d0d.png)

When placed normally, a Tempered Glass Pane snaps to the closest edge of the block face you
clicked.

Need more control? Shift + right-click places it directly against the clicked face instead.

Tempered panes can be:

- placed vertically or horizontally
- attached to different block edges
- waterlogged
- connected into clean corners
- rotated after placement

Matching panes also connect seamlessly, without thick borders breaking up larger windows
(configurable in Mod Menu).

Clear, stained and tinted glass are all supported as both panes and full blocks.

Six matching Tempered Glass blocks can also be crafted into 16 Tempered Glass Panes of the same
colour, just like the vanilla glass pane recipe.

</details>

<details>
<summary>Wooden frames</summary>

Combine any supported glass pane or full glass block with any plank to add a wooden frame.

![Framed Panes Recipe](https://cdn.modrinth.com/data/cached_images/7967e2a64a54a9af2c3270da01d7ccf7000097fd.png)

Both ordinary and Tempered glass can be framed. Ordinary framed glass keeps normal vanilla
breaking behaviour, while Tempered framed glass normally drops intact.

All vanilla wood types work, and modded planks are supported automatically. Frames made from
modded wood use that plank's texture, so they fit naturally with the rest of the wood set.

Matching framed full blocks remove their internal wooden borders, making it possible to build
larger clean windows. Different glass colours or wood types keep the divider between them.

</details>


<details>
<summary>New Tools</summary>

Glazier's Tools:

- Copper — rotates panes
- Iron — rotates panes and switches between edge and centred placement
- Diamond — same controls, plus faster glass breaking

The rotation-axis key can be changed in Controls and defaults to V.

Glass Chisel:

- Right-click a Tempered Glass edge to switch it between seamless and visible
- Shift + right-click resets the whole pane to automatic connections

Press V while holding the Glass Chisel to switch between editing both sides of a shared seam and
editing only the clicked edge.

The Glass Chisel works with panes and framed full glass blocks.

</details>

<details>
<summary>Experimental stair and slab glass</summary>

Tempered Glass Panes can also share the same block space with stairs and slabs, making things like
angled windows and more unusual shapes possible.

Shift + right-click a stair or slab with a Tempered Glass Pane to combine them. Normal
right-clicking still places the pane beside the block.

These combined blocks support rotation and edge/centred switching with Glazier's Tools.

This feature is currently experimental and disabled by default. You can enable it through Mod
Menu.

</details>

<details>
<summary>Important note for existing worlds</summary>

Version 0.2.2 changes framed glass to a new component-based item system. Glass placed by older
versions still loads in 0.2.2, but those old blocks are now considered legacy.

If you plan to keep updating the mod, replace old framed glass during 0.2.2. A future release may
remove the legacy block IDs.

</details>

## Configuration

Mod Menu is optional, but adds settings for seamless connections, experimental stair/slab glass,
Tempered Glass drops, reverse crafting, the Glass Chisel, and Glazier's Tool recipes.

Gameplay-related settings are synchronized by the server.

## Multiplayer

Ultimate Glass must be installed on both the server and connecting clients when playing
multiplayer.

## Development

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
