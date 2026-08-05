package com.github.tionard.ultimateglass;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UltimateGlass implements ModInitializer {
    public static final String MOD_ID = "ultimateglass";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing Ultimate Glass");
    }
}
