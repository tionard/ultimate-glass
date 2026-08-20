package com.github.tionard.ultimateglass;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tionard.ultimateglass.config.UltimateGlassServerConfig;
import com.github.tionard.ultimateglass.interaction.UltimateGlassInteractions;
import com.github.tionard.ultimateglass.network.UltimateGlassNetworking;
import com.github.tionard.ultimateglass.recipe.UltimateGlassRecipes;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlocks;
import com.github.tionard.ultimateglass.registry.UltimateGlassBlockEntities;
import com.github.tionard.ultimateglass.registry.UltimateGlassComponents;
import com.github.tionard.ultimateglass.registry.UltimateGlassItems;

public final class UltimateGlass implements ModInitializer {
    public static final String MOD_ID = "ultimateglass";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        UltimateGlassServerConfig.load();
        UltimateGlassComponents.initialize();
        UltimateGlassBlocks.initialize();
        UltimateGlassBlockEntities.initialize();
        UltimateGlassItems.initialize();
        UltimateGlassRecipes.initialize();
        UltimateGlassNetworking.initialize();
        UltimateGlassInteractions.initialize();
        LOGGER.info("Initialized Ultimate Glass");
    }
}
