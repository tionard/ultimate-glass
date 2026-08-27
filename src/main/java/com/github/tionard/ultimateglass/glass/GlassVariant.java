package com.github.tionard.ultimateglass.glass;

import java.util.Objects;

import com.github.tionard.ultimateglass.pane.PaneFrame;
import com.github.tionard.ultimateglass.pane.PaneMaterial;

/** Material, form, tempering, and frame identity for one complete-family variant. */
public record GlassVariant(
        PaneMaterial material,
        GlassForm form,
        boolean tempered,
        PaneFrame frame
) {
    public GlassVariant {
        Objects.requireNonNull(material, "material");
        Objects.requireNonNull(form, "form");
        Objects.requireNonNull(frame, "frame");
    }

    public GlassVariant withFrame(PaneFrame requestedFrame) {
        return new GlassVariant(material, form, tempered, requestedFrame);
    }

    public boolean isFramed() {
        return frame.isFramed();
    }
}
