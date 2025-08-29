package com.github.sladki.gtnhrates.mixins.late;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.sladki.gtnhrates.ModConfig;

import gregtech.common.tileentities.machines.basic.MTEMiner;

@Mixin(value = MTEMiner.class, remap = false)
public abstract class GTMiner {

    @Shadow(remap = false)
    static final int[] ENERGY = { 8, 8, 32, 128, 512 };

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onMTEMinerClassInit(CallbackInfo ci) {
        for (int i = 0; i < ENERGY.length; i++) {
            ENERGY[i] = applyRate(ENERGY[i], ModConfig.Rates.gtSimpleMinersEnergyDiscount);
        }
    }

}
