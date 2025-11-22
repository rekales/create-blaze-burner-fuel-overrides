package com.kreidev.createbbfueloverrides;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public class CommonConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue REMOVE_FUEL_ITEMS = BUILDER
            .comment("disable fuels from items with fuel values and fuel tags")
            .define("removeDefaultFuels", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean removeFuelItems;

    static void onLoad(final ModConfigEvent.Loading event) {
        removeFuelItems = REMOVE_FUEL_ITEMS.get();
    }

    static void onReload(final ModConfigEvent.Reloading event) {
        removeFuelItems = REMOVE_FUEL_ITEMS.get();
    }
}
