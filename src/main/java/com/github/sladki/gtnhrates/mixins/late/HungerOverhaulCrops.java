package com.github.sladki.gtnhrates.mixins.late;

import java.util.List;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;

import iguanaman.hungeroverhaul.util.BlockHelper;

// There are no crops in HO, but it modifies drop behavior
@Mixin(value = BlockHelper.class, remap = false)
public abstract class HungerOverhaulCrops {

    @ModifyReturnValue(method = "modifyCropDrops", at = @At(value = "RETURN"))
    private static List<ItemStack> onModifyCropDrops(List<ItemStack> original) {
        return Utils.multiplyItemStacksSize(original, ModConfig.Rates.cropsYield);
    }

}
