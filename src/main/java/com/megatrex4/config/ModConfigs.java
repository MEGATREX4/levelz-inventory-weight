package com.megatrex4.config;

import me.fzzyhmstrs.fzzy_config.api.ConfigApiJava;

public final class ModConfigs {

    public static LevelZInventoryWeightConfig CONFIG =
            ConfigApiJava.registerAndLoadConfig(LevelZInventoryWeightConfig::new);

    private ModConfigs() {
    }

    public static void init() {
    }
}