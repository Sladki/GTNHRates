package com.github.sladki.gtnhrates.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;

import gtPlusPlus.core.item.ModItems;

@Mixin(value = ModItems.class, remap = false)
public class Radium226 {

    @ModifyConstant(method = "init()V", constant = @Constant(intValue = 90000), remap = false)
    private static int modifyRadium226DecayTime(int original) {
        return Utils.applyRate(original, ModConfig.Rates.gtPlusPlusRadium226DecayRate);
    }
}
