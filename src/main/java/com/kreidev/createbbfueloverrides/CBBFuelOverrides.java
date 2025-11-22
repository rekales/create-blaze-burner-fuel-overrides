package com.kreidev.createbbfueloverrides;

import com.kreidev.createbbfueloverrides.data.BlazeBurnerFuelDataLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraftforge.eventbus.api.IEventBus;

import net.minecraftforge.fml.common.Mod;

@Mod(CBBFuelOverrides.MOD_ID)
public class CBBFuelOverrides {
    public static final String MOD_ID = "createbbfueloverrides";

    @SuppressWarnings("unused")
    public static final Logger LOGGER = LogUtils.getLogger();

    public CBBFuelOverrides() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CommonConfig.SPEC);
        MinecraftForge.EVENT_BUS.addListener(CBBFuelOverrides::onAddReloadListeners);
        modEventBus.addListener(CommonConfig::onLoad);
        modEventBus.addListener(CommonConfig::onReload);
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(BlazeBurnerFuelDataLoader.INSTANCE);
    }

    public static ResourceLocation resLoc(String path, Object... args) {
        return new ResourceLocation(MOD_ID, String.format(path, args));
    }
}
