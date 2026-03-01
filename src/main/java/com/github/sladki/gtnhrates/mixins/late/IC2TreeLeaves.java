package com.github.sladki.gtnhrates.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;

import ic2.core.block.BlockRubLeaves;

@Mixin(value = BlockRubLeaves.class, remap = false)
public class IC2TreeLeaves {

    @ModifyArg(
        method = "dropBlockAsItemWithChance",
        at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"))
    private int saplingDropChance(int bound) {
        return Utils.applyRate(bound, ModConfig.Rates.ic2RubberTreeSaplingsDropChanceMultiplier);
    }

}
