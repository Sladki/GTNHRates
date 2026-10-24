package com.github.sladki.gtnhrates.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import gregtech.common.tileentities.machines.basic.MTERockBreaker;

@Mixin(
    value = gregtech.common.tileentities.machines.basic.MTERockBreaker.RockBreakerRecipe.Builder.class,
    remap = false)
public class GTRockBreaker {

    @Shadow
    private int duration;

    @ModifyReturnValue(method = "duration", at = @At(value = "RETURN"))
    private MTERockBreaker.RockBreakerRecipe.Builder modifyRecipeDuration(
        MTERockBreaker.RockBreakerRecipe.Builder original) {
        this.duration = Utils.applyRate(this.duration, ModConfig.Rates.gtRecipesEnergyDiscount);
        return original;
    }

}
