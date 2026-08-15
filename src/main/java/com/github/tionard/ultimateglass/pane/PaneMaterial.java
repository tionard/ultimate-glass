package com.github.tionard.ultimateglass.pane;

import org.jetbrains.annotations.Nullable;

/** The glass material carried by an Ultimate Glass pane, independent of its geometry. */
public enum PaneMaterial {
    CLEAR(Kind.CLEAR, null, "glass_pane"),
    WHITE_STAINED(Kind.STAINED, "white", "white_stained_glass_pane"),
    ORANGE_STAINED(Kind.STAINED, "orange", "orange_stained_glass_pane"),
    MAGENTA_STAINED(Kind.STAINED, "magenta", "magenta_stained_glass_pane"),
    LIGHT_BLUE_STAINED(Kind.STAINED, "light_blue", "light_blue_stained_glass_pane"),
    YELLOW_STAINED(Kind.STAINED, "yellow", "yellow_stained_glass_pane"),
    LIME_STAINED(Kind.STAINED, "lime", "lime_stained_glass_pane"),
    PINK_STAINED(Kind.STAINED, "pink", "pink_stained_glass_pane"),
    GRAY_STAINED(Kind.STAINED, "gray", "gray_stained_glass_pane"),
    LIGHT_GRAY_STAINED(Kind.STAINED, "light_gray", "light_gray_stained_glass_pane"),
    CYAN_STAINED(Kind.STAINED, "cyan", "cyan_stained_glass_pane"),
    PURPLE_STAINED(Kind.STAINED, "purple", "purple_stained_glass_pane"),
    BLUE_STAINED(Kind.STAINED, "blue", "blue_stained_glass_pane"),
    BROWN_STAINED(Kind.STAINED, "brown", "brown_stained_glass_pane"),
    GREEN_STAINED(Kind.STAINED, "green", "green_stained_glass_pane"),
    RED_STAINED(Kind.STAINED, "red", "red_stained_glass_pane"),
    BLACK_STAINED(Kind.STAINED, "black", "black_stained_glass_pane"),

    /** Reserved for the distinct light-blocking material introduced in beta.2. */
    TINTED(Kind.TINTED, null, null);

    private final Kind kind;
    @Nullable
    private final String colorName;
    @Nullable
    private final String vanillaPanePath;

    PaneMaterial(Kind kind, @Nullable String colorName, @Nullable String vanillaPanePath) {
        this.kind = kind;
        this.colorName = colorName;
        this.vanillaPanePath = vanillaPanePath;
    }

    public Kind kind() {
        return kind;
    }

    @Nullable
    public String colorName() {
        return colorName;
    }

    public boolean hasVanillaPane() {
        return vanillaPanePath != null;
    }

    /** Returns the corresponding vanilla pane path for materials which currently have one. */
    public String vanillaPanePath() {
        if (vanillaPanePath == null) {
            throw new IllegalStateException(this + " has no vanilla pane block");
        }
        return vanillaPanePath;
    }

    public enum Kind {
        CLEAR,
        STAINED,
        TINTED
    }
}
