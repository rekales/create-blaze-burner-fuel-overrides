package com.kreidev.createbbfueloverrides.mixin;

import com.kreidev.createbbfueloverrides.BlazeBurnerFuelManager;
import com.kreidev.createbbfueloverrides.CBBFuelOverrides;
import com.kreidev.createbbfueloverrides.CommonConfig;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.simibubi.create.AllTags;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlock;
import com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import static com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.INSERTION_THRESHOLD;
import static com.simibubi.create.content.processing.burner.BlazeBurnerBlockEntity.MAX_HEAT_CAPACITY;

@Mixin(value = BlazeBurnerBlockEntity.class, remap = false)
public abstract class BlazeBurnerBlockEntityMixin {

//    @Inject(
//            method = "tryUpdateFuel",
//            at = @At(value = "INVOKE", target = "Lcom/simibubi/create/AllTags$AllItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z")
//    )
//    private void tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir, @Local BlazeBurnerBlockEntity.FuelType newFuel, @Local int newBurnTime) {
//        CBBFuelOverrides.LOGGER.debug(itemStack+"");
//        CBBFuelOverrides.LOGGER.debug(newFuel+"");
//    }


//    @Inject(
//            method = "tryUpdateFuel",
//            at = @At(
//                    value = "FIELD",
//                    target = "Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity$FuelType;NONE:Lcom/simibubi/create/content/processing/burner/BlazeBurnerBlockEntity$FuelType;",
//                    opcode = Opcodes.GETSTATIC,
//                    ordinal = 1
//            )
//    )
//    private void tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate, CallbackInfoReturnable<Boolean> cir, @Local LocalRef<BlazeBurnerBlockEntity.FuelType> newFuel, @Local Integer newBurnTime) {
//
//        CBBFuelOverrides.LOGGER.warn(itemStack+"");
//        CBBFuelOverrides.LOGGER.warn(remainingBurnTime+"");
//        CBBFuelOverrides.LOGGER.warn(newFuel.get()+"");
//
//        if (CommonConfig.removeFuelItems) {
////            newFuel = BlazeBurnerBlockEntity.FuelType.NONE;
//            newFuel.set(BlazeBurnerBlockEntity.FuelType.NONE);
//
//        }
//
////        if (newFuel == BlazeBurnerBlockEntity.FuelType.NONE) {
////            newFuel = BlazeBurnerFuelManager.getFuelType(itemStack.getItem());
////            if (newFuel == BlazeBurnerBlockEntity.FuelType.NONE) return;
////            if (newFuel == BlazeBurnerBlockEntity.FuelType.SPECIAL) {
////                newBurnTime = BlazeBurnerFuelManager.getSpecialFuelValue(itemStack.getItem());
////            } else {
////                newBurnTime = BlazeBurnerFuelManager.getRegularFuelValue(itemStack.getItem());
////            }
////
////            if (newBurnTime == -1) {
////                newFuel = BlazeBurnerBlockEntity.FuelType.NONE;
////            }
////        }
//    }




    @Shadow public boolean isCreative;
    @Shadow protected BlazeBurnerBlockEntity.FuelType activeFuel;
    @Shadow protected int remainingBurnTime;

    @Shadow protected abstract void playSound();
    @Shadow public abstract BlazeBurnerBlock.HeatLevel getHeatLevelFromBlock();
    @Shadow public abstract void updateBlockState();
    @Shadow public abstract void spawnParticleBurst(boolean soulFlame);

    /**
     * @author Krei
     * @reason cuz I tried and still can't figure out a good way to do this
     */
    @Overwrite
    protected boolean tryUpdateFuel(ItemStack itemStack, boolean forceOverflow, boolean simulate) {
        if (isCreative)
            return false;

        BlazeBurnerBlockEntity.FuelType newFuel = BlazeBurnerBlockEntity.FuelType.NONE;
        int newBurnTime;

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


        // Added Section
        if (CommonConfig.removeFuelItems) {
            newFuel = BlazeBurnerBlockEntity.FuelType.NONE;
            newBurnTime = 0;
        }

        if (newFuel == BlazeBurnerBlockEntity.FuelType.NONE) {
            newFuel = BlazeBurnerFuelManager.getFuelType(itemStack.getItem());
            if (newFuel == BlazeBurnerBlockEntity.FuelType.SPECIAL) {
                newBurnTime = BlazeBurnerFuelManager.getSpecialFuelValue(itemStack.getItem());
            } else if (newFuel == BlazeBurnerBlockEntity.FuelType.NORMAL){
                newBurnTime = BlazeBurnerFuelManager.getRegularFuelValue(itemStack.getItem());
            } else {
                newBurnTime = -1;
            }
            if (newBurnTime == -1) {
                newFuel = BlazeBurnerBlockEntity.FuelType.NONE;
            }
        }


        if (newFuel == BlazeBurnerBlockEntity.FuelType.NONE)
            return false;
        if (newFuel.ordinal() < activeFuel.ordinal())
            return false;

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
                return false;
            }
        }

        if (simulate)
            return true;

        activeFuel = newFuel;
        remainingBurnTime = newBurnTime;


        // Added Section
        Level level = ((BlazeBurnerBlockEntity)(Object)this).getLevel();
        BlockPos worldPosition = ((BlazeBurnerBlockEntity)(Object)this).getBlockPos();


        if (level != null && level.isClientSide()) {
            spawnParticleBurst(activeFuel == BlazeBurnerBlockEntity.FuelType.SPECIAL);
            return true;
        }

        BlazeBurnerBlock.HeatLevel prev = getHeatLevelFromBlock();
        playSound();
        updateBlockState();

        if (prev != getHeatLevelFromBlock())
            level.playSound(null, worldPosition, SoundEvents.BLAZE_AMBIENT, SoundSource.BLOCKS,
                    .125f + level.random.nextFloat() * .125f, 1.15f - level.random.nextFloat() * .25f);

        return true;
    }
}
