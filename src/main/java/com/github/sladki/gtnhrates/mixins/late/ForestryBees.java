package com.github.sladki.gtnhrates.mixins.late;

import java.util.Arrays;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import forestry.apiculture.genetics.Bee;

@Mixin(value = Bee.class, remap = false)
public abstract class ForestryBees {

    @ModifyReturnValue(method = "produceStacks", at = @At(value = "RETURN"))
    private ItemStack[] onProduceStacks(ItemStack[] original) {
        return Utils.multiplyItemStacksSize(Arrays.asList(original), ModConfig.Rates.beesYield)
            .toArray(new ItemStack[0]);
    }

}
