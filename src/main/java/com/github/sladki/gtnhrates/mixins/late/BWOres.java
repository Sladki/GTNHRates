package com.github.sladki.gtnhrates.mixins.late;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;

import gregtech.common.ores.BWOreAdapter;
import gregtech.common.ores.OreInfo;

@Mixin(value = BWOreAdapter.class, remap = false)
public abstract class BWOres {

    @Inject(method = "getOreDrops", at = @At(value = "RETURN"))
    private void applyDropsRate(Random random, OreInfo<?> info2, boolean silktouch, int fortune,
        CallbackInfoReturnable<ArrayList<ItemStack>> cir) {
        if (!silktouch) Utils.multiplyItemStacksSize(cir.getReturnValue(), ModConfig.Rates.gtOresDrops);
    }
}
