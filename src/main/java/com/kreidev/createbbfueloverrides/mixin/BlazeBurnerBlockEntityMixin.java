package com.kreidev.createbbfueloverrides.mixin;

import com.kreidev.createbbfueloverrides.BlazeBurnerFuelManager;
import com.kreidev.createbbfueloverrides.CommonConfig;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.INSERTION_THRESHOLD;
import static com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY;

// NOTE: priority 500 to make it run after mixins from mods that add liquid fuels
@Mixin(value = BlazeBurnerBlockEntity.class, remap = false, priority = 500)
public abstract class BlazeBurnerBlockEntityMixin {

    @Shadow public boolean isCreative;
    @Shadow protected BlazeBurnerBlockEntity.FuelType activeFuel;
    @Shadow protected int remainingBurnTime;

    @Shadow protected abstract void playSound();
    @Shadow public abstract BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock();
    @Shadow public abstract void updateBlockState();
    @Shadow public abstract void spawnParticleBurst(boolean soulFlame);

    // NOTE: maybe rewrite again to make it more elegant
    @Inject(
            method = "tryUpdateFuel",
            at = @At("HEAD"),
            cancellable = true
    )
    private void tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir) {
        if (isCreative) {
            cir.setReturnValue(false);
            return;
        }

        BlazeBurnerBlockEntity.FuelType newFuel = BlazeBurnerBlockEntity.FuelType.NONE;
        int newBurnTime = -1;

        if (!CommonConfig.removeFuelItems) {
            if (AllTags.AllItemTags.BLAZE_BURNER_FUEL_SPECIAL.matches(itemStack)) {
                newBurnTime = 3200;
                newFuel = BlazeBurnerBlockEntity.FuelType.SPECIAL;
            } else {
                newBurnTime = ForgeHooks.getBurnTime(itemStack, null);
                if (newBurnTime > 0) {
                    newFuel = BlazeBurnerBlockEntity.FuelType.NORMAL;
                } else if (AllTags.AllItemTags.BLAZE_BURNER_FUEL_REGULAR.matches(itemStack)) {
                    newBurnTime = 1600; // Same as coal
                    newFuel = BlazeBurnerBlockEntity.FuelType.NORMAL;
                }
            }
        }

        if (BlazeBurnerFuelManager.getFuelType(itemStack.getItem()) != BlazeBurnerBlockEntity.FuelType.NONE) {
            newFuel = BlazeBurnerFuelManager.getFuelType(itemStack.getItem());
            if (newFuel == BlazeBurnerBlockEntity.FuelType.SPECIAL) {
                newBurnTime = BlazeBurnerFuelManager.getSpecialFuelValue(itemStack.getItem());
            } else if (newFuel == BlazeBurnerBlockEntity.FuelType.NORMAL){
                newBurnTime = BlazeBurnerFuelManager.getRegularFuelValue(itemStack.getItem());
            }
        }

        if (newFuel == BlazeBurnerBlockEntity.FuelType.NONE
                || newBurnTime == -1
                || newFuel.ordinal() < activeFuel.ordinal()) {
            cir.setReturnValue(false);
            return;
        }

        if (newFuel == activeFuel) {
            if (remainingBurnTime <= INSERTION_THRESHOLD) {
                newBurnTime += remainingBurnTime;
            } else if (forceOverflow && newFuel == BlazeBurnerBlockEntity.FuelType.NORMAL) {
                if (remainingBurnTime < MAX_HEAT_CAPACITY) {
                    newBurnTime = Math.min(remainingBurnTime + newBurnTime, MAX_HEAT_CAPACITY);
                } else {
                    newBurnTime = remainingBurnTime;
                }
            } else {
                cir.setReturnValue(false);
                return;
            }
        }

        if (simulate){
            cir.setReturnValue(true);
            return;
        }

        activeFuel = newFuel;
        remainingBurnTime = newBurnTime;

        Level level = ((BlazeBurnerBlockEntity)(Object)this).getLevel();
        BlockPos worldPosition = ((BlazeBurnerBlockEntity)(Object)this).getBlockPos();

        if (level != null && level.isClientSide()) {
            spawnParticleBurst(activeFuel == BlazeBurnerBlockEntity.FuelType.SPECIAL);
            cir.setReturnValue(true);
            return;
        }

        BlazeBurnerBlock.HeatLevel prev = getHeatLevelFromBlock();
        playSound();
        updateBlockState();

        if (prev != getHeatLevelFromBlock() && level != null)
            level.playSound(null, worldPosition, SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
                    .125f + level.random.nextFloat() * .125f, 1.15f - level.random.nextFloat() * .25f);

        cir.setReturnValue(true);
    }
}
