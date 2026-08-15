package com.github.tionard.ultimateglass.item;

import net.minecraft.network.chat.Component;

final class FrameDisplayName {
    private FrameDisplayName() {
    }

    static Component withoutPlanksSuffix(Component frameName, String suffix) {
        String name = frameName.getString();
        if (suffix.isEmpty() || name.length() <= suffix.length()
                || !name.regionMatches(true, name.length() - suffix.length(), suffix, 0, suffix.length())) {
            return frameName;
        }
        return Component.literal(name.substring(0, name.length() - suffix.length()))
                .withStyle(frameName.getStyle());
    }
}
