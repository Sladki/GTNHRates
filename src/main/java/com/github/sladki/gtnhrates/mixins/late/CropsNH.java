package com.github.sladki.gtnhrates.mixins.late;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import com.github.sladki.gtnhrates.ModConfig;
import com.gtnewhorizon.cropsnh.tileentity.TileEntityCropSticks;

public class CropsNH {

    public static List<String> mixins() {
        return Stream.of("TileEntityCropSticksMixin")
            .map(s -> "CropsNH$" + s)
            .collect(Collectors.toList());
    }

    @Mixin(value = TileEntityCropSticks.class, remap = false)
    public abstract static class TileEntityCropSticksMixin {

        @ModifyVariable(method = "harvest", at = @At("HEAD"), argsOnly = true)
        private double adjustDropMultiplier(double dropMultiplier) {
            return dropMultiplier * ModConfig.Rates.ic2CropsYield;
        }

    }

}
