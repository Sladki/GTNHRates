package com.github.sladki.gtnhrates.mixins.late;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.github.sladki.gtnhrates.ModConfig;

import mods.railcraft.common.blocks.machine.alpha.TileSteamOven;

@Mixin(value = TileSteamOven.class, remap = false)
public abstract class RailcraftSteamOven {

    @Unique
    private static final int STEAM_REQUIRED = 8000;

    @Unique
    private static final int TICKS_TO_PROCESS = 256;

    @ModifyConstant(method = "drainSteam", constant = @Constant(intValue = STEAM_REQUIRED))
    private int onDrainSteam(int constant) {
        return applyRate(STEAM_REQUIRED, ModConfig.Rates.gtRecipesEnergyDiscount);
    }

    @ModifyConstant(method = "getCookProgressScaled", constant = @Constant(intValue = TICKS_TO_PROCESS))
    private int onGetCookProgressScaled(int constant) {
        return applyRate(TICKS_TO_PROCESS, ModConfig.Rates.gtRecipesEnergyDiscount);
    }

    @ModifyConstant(method = "updateEntity", constant = @Constant(intValue = TICKS_TO_PROCESS), remap = true)
    private int onUpdateEntity(int constant) {
        return applyRate(TICKS_TO_PROCESS, ModConfig.Rates.gtRecipesEnergyDiscount);
    }

}
