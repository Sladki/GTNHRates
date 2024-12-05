package com.github.sladki.gtnhrates.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.github.sladki.gtnhrates.ModConfig;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import gregtech.api.items.MetaGeneratedTool;

@Mixin(value = MetaGeneratedTool.class, remap = false)
public abstract class GTMetaTools {

    @ModifyExpressionValue(
        method = "getContainerItem(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/item/ItemStack;",
        at = @At(value = "INVOKE", target = "Lgregtech/api/interfaces/IToolStats;getToolDamagePerContainerCraft()I"))
    private int onGetToolDamagePerContainerCraft(int original) {
        return (int) Math.ceil(original / ModConfig.Rates.gtToolsCraftingDurability);
    }
}
