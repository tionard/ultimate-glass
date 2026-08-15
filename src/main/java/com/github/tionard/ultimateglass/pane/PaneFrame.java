package com.github.tionard.ultimateglass.pane;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;

/** Wood frame identity for the compact built-in variants and the data-driven modded variant. */
public enum PaneFrame {
    NONE(null, null),
    OAK("oak", "minecraft:block/oak_planks"),
    SPRUCE("spruce", "minecraft:block/spruce_planks"),
    BIRCH("birch", "minecraft:block/birch_planks"),
    JUNGLE("jungle", "minecraft:block/jungle_planks"),
    ACACIA("acacia", "minecraft:block/acacia_planks"),
    DARK_OAK("dark_oak", "minecraft:block/dark_oak_planks"),
    PALE_OAK("pale_oak", "minecraft:block/pale_oak_planks"),
    CRIMSON("crimson", "minecraft:block/crimson_planks"),
    WARPED("warped", "minecraft:block/warped_planks"),
    MANGROVE("mangrove", "minecraft:block/mangrove_planks"),
    BAMBOO("bamboo", "minecraft:block/bamboo_planks"),
    CHERRY("cherry", "minecraft:block/cherry_planks"),
    DYNAMIC("modded", null);

    private static final List<PaneFrame> WOOD_FRAMES = List.of(
            OAK, SPRUCE, BIRCH, JUNGLE, ACACIA, DARK_OAK, PALE_OAK,
            CRIMSON, WARPED, MANGROVE, BAMBOO, CHERRY
    );

    @Nullable
    private final String path;
    @Nullable
    private final String texture;

    PaneFrame(@Nullable String path, @Nullable String texture) {
        this.path = path;
        this.texture = texture;
    }

    public static List<PaneFrame> woodFrames() {
        return WOOD_FRAMES;
    }

    @Nullable
    public static PaneFrame fromPlank(Item item) {
        Identifier id = BuiltInRegistries.ITEM.getKey(item);
        if (!"minecraft".equals(id.getNamespace()) || !id.getPath().endsWith("_planks")) {
            return null;
        }
        String wood = id.getPath().substring(0, id.getPath().length() - "_planks".length());
        return WOOD_FRAMES.stream().filter(frame -> frame.path.equals(wood)).findFirst().orElse(null);
    }

    public String path() {
        if (path == null) {
            throw new IllegalStateException(this + " has no registry path");
        }
        return path;
    }

    public String texture() {
        if (texture == null) {
            throw new IllegalStateException(this + " has no fixed texture");
        }
        return texture;
    }

    public boolean isFramed() {
        return this != NONE;
    }

    public boolean isDynamic() {
        return this == DYNAMIC;
    }
}
