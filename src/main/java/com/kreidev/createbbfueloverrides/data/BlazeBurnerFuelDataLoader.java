package com.kreidev.createbbfueloverrides.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.kreidev.createbbfueloverrides.BlazeBurnerFuelManager;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;

import java.util.Map;

import static com.kreidev.createbbfueloverrides.CBBFuelOverrides.resLoc;
import static com.kreidev.createbbfueloverrides.CBBFuelOverrides.LOGGER;

public class BlazeBurnerFuelDataLoader extends SimpleJsonResourceReloadListener {

    public static final Codec<Map<Item, Integer>> MAP_CODEC = Codec.unboundedMap(BuiltInRegistries.ITEM.byNameCodec(), Codec.INT);

    private static final Gson GSON = new Gson();
    public static final BlazeBurnerFuelDataLoader INSTANCE = new BlazeBurnerFuelDataLoader();

    public BlazeBurnerFuelDataLoader() {
        super(GSON, "blaze_burner_fuel_override");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profilerFiller) {
        BlazeBurnerFuelManager.clearAll();

        object.forEach((id, json) -> {
            try {
                if (id.equals(resLoc("regular"))) {
                    Map<Item, Integer> fuels = MAP_CODEC.parse(JsonOps.INSTANCE, json)
                            .getOrThrow(false, error->{});
                    BlazeBurnerFuelManager.addRegularFuels(fuels);
                    LOGGER.info("Loaded regular fuels");
                } else if (id.equals(resLoc("special"))) {
                    Map<Item, Integer> fuels = MAP_CODEC.parse(JsonOps.INSTANCE, json)
                            .getOrThrow(false, error->{});
                    BlazeBurnerFuelManager.addSpecialFuels(fuels);
                    LOGGER.info("Loaded special fuels");
                }
            } catch (Exception e) {
                LOGGER.error("Failed to load fuels: " + id);
                throw new RuntimeException("Failed to load fuels: " + id, e);
            }
        });
    }
}
