package com.github.tionard.ultimateglass.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

import net.minecraft.network.chat.Component;

final class DynamicFramedPaneItemTest {
    @Test
    void removesLocalizedPlanksSuffixFromDynamicFrameName() {
        Component result = FrameDisplayName.withoutPlanksSuffix(
                Component.literal("Umbran Planks"), " Planks"
        );

        assertEquals("Umbran", result.getString());
    }

    @Test
    void suffixComparisonIsCaseInsensitive() {
        Component result = FrameDisplayName.withoutPlanksSuffix(
                Component.literal("Umbran planks"), " Planks"
        );

        assertEquals("Umbran", result.getString());
    }

    @Test
    void keepsNonPlankDisplayNameUnchanged() {
        Component original = Component.literal("Polished Umbran Boards");

        assertSame(original, FrameDisplayName.withoutPlanksSuffix(original, " Planks"));
    }
}
