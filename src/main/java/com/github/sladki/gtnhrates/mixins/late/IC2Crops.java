package com.github.sladki.gtnhrates.mixins.late;

import java.util.Arrays;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import ic2.core.crop.TileEntityCrop;

@Mixin(value = TileEntityCrop.class, remap = false)
public abstract class IC2Crops {

    @ModifyReturnValue(method = "harvest_automated", at = @At(value = "RETURN"))
    private ItemStack[] onHarvestAutomated(ItemStack[] original) {
        if (original != null) {
            return Utils.multiplyItemStacksSize(Arrays.asList(original), ModConfig.Rates.ic2CropsYield)
                .toArray(new ItemStack[0]);
        }
        return original;
    }
}
