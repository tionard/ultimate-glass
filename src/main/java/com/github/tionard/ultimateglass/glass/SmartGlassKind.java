package com.github.tionard.ultimateglass.glass;

/** The six component-backed item families exposed to players from 0.2.2 onward. */
public enum SmartGlassKind {
    TEMPERED_PANE("tempered_glass_pane", GlassForm.PANE, true, false),
    TEMPERED_BLOCK("tempered_glass", GlassForm.BLOCK, true, false),
    FRAMED_PANE("framed_glass_pane", GlassForm.PANE, false, true),
    FRAMED_TEMPERED_PANE("framed_tempered_glass_pane", GlassForm.PANE, true, true),
    FRAMED_BLOCK("framed_glass", GlassForm.BLOCK, false, true),
    FRAMED_TEMPERED_BLOCK("framed_tempered_glass", GlassForm.BLOCK, true, true);

    private final String itemPath;
    private final GlassForm form;
    private final boolean tempered;
    private final boolean framed;

    SmartGlassKind(String itemPath, GlassForm form, boolean tempered, boolean framed) {
        this.itemPath = itemPath;
        this.form = form;
        this.tempered = tempered;
        this.framed = framed;
    }

    public String itemPath() {
        return itemPath;
    }

    public GlassForm form() {
        return form;
    }

    public boolean tempered() {
        return tempered;
    }

    public boolean framed() {
        return framed;
    }
}
