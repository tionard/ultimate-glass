# Ultimate Glass 0.2.3 for Minecraft 1.21.11

This branch (`version/1.21.11`) backports the 0.2.3 feature set. It does not
claim compatibility with other 1.21.x releases.

## Requirements

- Minecraft Java Edition 1.21.11
- Java 21 or newer
- Fabric Loader 0.19.3 or newer
- Fabric API for 1.21.11 (built against 0.141.6+1.21.11)
- Optional: Mod Menu 17.0.1

Install the remapped `ultimate-glass-0.2.3+mc1.21.11.jar` on both the client
and server. Do not install the development or sources JAR.

## Port-specific changes

- Restore Loom remapping with official Mojang mappings for obfuscated Minecraft.
- Build and run regression tests with the Java 21 toolchain.
- Adapt crafting, networking, creative-tab registration, and tinted light blocking.
- Use the older block/item model, sprite, keybinding, screen and fluid APIs.
- Register translucent block layers explicitly; 1.21.11 ignores the newer
  model JSON `render_type` property. Composite hosts retain their own render layer.
- Keep water clipping in the native Fabric/Sodium fluid renderers.
- Refresh chunk geometry when the seamless-texture setting changes.
- Explicitly set the geometric face while projecting dynamic plank textures.
  Uncullable quads in 1.21.11 can have no nominal face; without one, UV locking
  reuses already-baked atlas coordinates and the frame looks like a flat colour.

The optional Sodium mixin targets were checked against
`mc1.21.11-0.8.14-fabric`. This is not a substitute for visual shader testing.

## In-game acceptance

Automated verification includes the Java 21 regression suite and headless client
texture tests against Fabric's actual sprite baker, covering all six face directions,
missing/stale nominal faces, preserved culling and repeat texture baking.
The packaged JAR uses Java 21 bytecode and Fabric's intermediary namespace.
`runServer --args=--initSettings` initialized Ultimate Glass and exited
successfully, without accepting the EULA or loading a world. A full server/world
and visual client check are still pending.

Use a new 1.21.11 test world or a backup of an existing 1.21.11 world.
Do not open a 26.x world in 1.21.11; this port does not downgrade world saves.

1. Check the creative tab, crafting, inventory models, all glass colours and wood frames.
2. Place and rotate centred/edge sheets; combine matching edge panes, including
   opposite faces, and verify the number of recovered panes.
3. Chisel each face of a parallel assembly independently. Toggle seamless textures
   in Mod Menu and check that the world updates immediately.
4. Compare waterlogged corner pools with ordinary water, with Fabric alone,
   with Sodium, and with Iris shaders. Check clipping, colour and transparency.
5. If using experimental composites, check that the host stays opaque where
   appropriate and the glass remains transparent.
6. Check a dedicated server with the same mod and Fabric API.

No additional Minecraft versions are included in this backport.
