package com.github.sladki.gtnhrates.mixins.late;

import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.sladki.gtnhrates.ModConfig;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.common.tileentities.machines.multi.MTEOilDrillBase;

@Mixin(value = MTEOilDrillBase.class, remap = false)
public abstract class GTOilDrill {

    @ModifyReturnValue(method = "pumpOil", at = @At(value = "RETURN"))
    private FluidStack onPumpOil(FluidStack original) {
        if (original != null) {
            original.amount = (int) Math.ceil(original.amount * ModConfig.Rates.gtPumpOutput);
        }
        return original;
    }
}
