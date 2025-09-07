package com.github.sladki.gtnhrates.mixins.late;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.sladki.gtnhrates.ModConfig;

import gregtech.api.items.MetaGeneratedTool;

@Mixin(value = MetaGeneratedTool.class, remap = false)
public abstract class GTMetaTools {

    @Shadow
    public abstract boolean doDamage(ItemStack aStack, long aAmount);

    @Redirect(
        method = "getContainerItem*",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/items/MetaGeneratedTool;doDamage(Lnet/minecraft/item/ItemStack;J)Z"))
    private boolean onDamagingCraftingTool(MetaGeneratedTool instance, ItemStack tNewDamage, long tStats) {
        return doDamage(tNewDamage, (int) Math.ceil(tStats / ModConfig.Rates.gtToolsCraftingDurability));
    }
}
