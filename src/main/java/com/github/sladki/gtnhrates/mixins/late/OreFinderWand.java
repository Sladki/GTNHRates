package com.github.sladki.gtnhrates.mixins.late;

import java.util.WeakHashMap;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.encraft.dz.inventory.InventoryBuildingKit;
import com.encraft.dz.items.ItemOreFinderTool;
import com.github.sladki.gtnhrates.ModConfig;
import com.llamalad7.mixinextras.sugar.Local;

import forestry.storage.inventory.ItemInventoryBackpack;
import forestry.storage.items.ItemBackpack;

@Mixin(value = ItemOreFinderTool.class)
public abstract class OreFinderWand {

    @Unique
    public final WeakHashMap<ItemStack, Integer> GTNHRates$backpackSlotIndexMap = new WeakHashMap<>(4);

    @Redirect(
        method = "onUpdate",
        at = @At(
            value = "INVOKE",
            target = "Lcom/encraft/dz/inventory/InventoryBuildingKit;getStackInSlot(I)Lnet/minecraft/item/ItemStack;"))
    private ItemStack nextSampleFromBackpackOr(InventoryBuildingKit instance, int slot,
        @Local(argsOnly = true) Entity entity) {
        final ItemStack backpackItemStack = instance.getStackInSlot(slot);
        if (backpackItemStack == null) return null;
        if (!ModConfig.Misc.enableOreFinderWandBackpacksInteraction) return backpackItemStack;

        if (backpackItemStack.getItem() instanceof ItemBackpack backpack) {
            if (backpack.getDefinition()
                .getKey()
                .equals("miner")) {

                final IInventory inv = new ItemInventoryBackpack(
                    (EntityPlayer) entity,
                    backpack.getBackpackSize(),
                    backpackItemStack);
                final int slotIndexStart = GTNHRates$backpackSlotIndexMap.getOrDefault(backpackItemStack, 0);
                final int invSize = inv.getSizeInventory();

                for (int i = 1; i <= invSize; i++) {
                    final int slotIndex = (slotIndexStart + i) % invSize;
                    ItemStack is = inv.getStackInSlot(slotIndex);
                    if (is != null) {
                        GTNHRates$backpackSlotIndexMap.put(backpackItemStack, slotIndex);
                        return is;
                    }
                }
            }
        }
        return backpackItemStack;
    }

}
