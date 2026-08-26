package com.github.tionard.ultimateglass.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

import com.github.tionard.ultimateglass.UltimateGlass;

/** Persistent material data shared by the component-backed glass item families. */
public final class UltimateGlassComponents {
    public static final DataComponentType<Identifier> GLASS_MATERIAL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "glass_material"),
            DataComponentType.<Identifier>builder()
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC)
                    .build()
    );

    public static final DataComponentType<Identifier> FRAME_BLOCK = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(UltimateGlass.MOD_ID, "frame_block"),
            DataComponentType.<Identifier>builder()
                    .persistent(Identifier.CODEC)
                    .networkSynchronized(Identifier.STREAM_CODEC)
                    .build()
    );

    private UltimateGlassComponents() {
    }

    public static void initialize() {
        // Trigger static registration before pane items are created.
    }
}
