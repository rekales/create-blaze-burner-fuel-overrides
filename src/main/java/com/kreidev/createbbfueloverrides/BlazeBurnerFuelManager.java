package com.kreidev.createbbfueloverrides;

import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

public class BlazeBurnerFuelManager {

    private static final Map<Item, Integer> regularFuels = new HashMap<>();
    private static final Map<Item, Integer> specialFuels = new HashMap<>();

    public static void clearAll() {
        regularFuels.clear();
        specialFuels.clear();
    }

    public static void addRegularFuels(Map<Item, Integer> fuels) {
        regularFuels.putAll(fuels);
    }

    public static void addSpecialFuels(Map<Item, Integer> fuels) {
        specialFuels.putAll(fuels);
    }

    public static BlazeBurnerBlockEntity.FuelType getFuelType(Item item) {
        if (specialFuels.containsKey(item)) return BlazeBurnerBlockEntity.FuelType.SPECIAL;
        if (regularFuels.containsKey(item)) return BlazeBurnerBlockEntity.FuelType.NORMAL;
        return BlazeBurnerBlockEntity.FuelType.NONE;
    }

    // NOTE: returns -1 when found nothing
    public static int getRegularFuelValue(Item item) {
        Integer ticks = regularFuels.get(item);
        if (ticks != null) return ticks;
        return -1;
    }

    // NOTE: returns -1 when found nothing
    public static int getSpecialFuelValue(Item item) {
        Integer ticks = specialFuels.get(item);
        if (ticks != null) return ticks;
        return -1;
    }
}
