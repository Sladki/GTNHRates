package com.github.sladki.gtnhrates.mixins.late;

import java.util.Arrays;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;

import ic2.core.Ic2Items;
import ic2.core.item.tool.ItemTreetap;

@Mixin(value = ItemTreetap.class, remap = false)
public class IC2TreeTap {

    @Inject(
        method = "ejectHarz",
        at = @At(
            value = "NEW",
            target = "(Lnet/minecraft/world/World;DDDLnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/item/EntityItem;"),
        cancellable = true)
    private static void onEjectHarz(World world, int x, int y, int z, int side, int quantity, CallbackInfo ci) {
        double ejectX = x + 0.5 + (side == 5 ? 1 : (side == 4 ? -1 : 0));
        double ejectY = y + 0.5;
        double ejectZ = z + 0.5 + (side == 3 ? 1 : (side == 2 ? -1 : 0));

        for (ItemStack is : Utils.multiplyItemStacksSize(
            Arrays.asList(Ic2Items.resin.copy()),
            quantity * ModConfig.Rates.ic2RubberTreeResinYield)) {
            EntityItem entityitem = new EntityItem(world, ejectX, ejectY, ejectZ, is);
            entityitem.delayBeforeCanPickup = 10;
            world.spawnEntityInWorld(entityitem);
        }
        ci.cancel();
    }

}
