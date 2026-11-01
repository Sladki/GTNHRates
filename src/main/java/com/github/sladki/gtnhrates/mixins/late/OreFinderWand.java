package com.github.sladki.gtnhrates.mixins.late;

import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.encraft.dz.gui.GuiOreFinder;
import com.encraft.dz.inventory.InventoryOreFinder;
import com.encraft.dz.items.ItemOreFinderTool;
import com.encraft.dz.items.OreFinderSearch;
import com.github.sladki.gtnhrates.ModConfig;
import com.llamalad7.mixinextras.sugar.Local;

import forestry.storage.inventory.ItemInventoryBackpack;
import forestry.storage.items.ItemBackpack;

public class OreFinderWand {

    public static List<String> mixins() {
        return Stream.of("OreFinderLogic", "ItemFilter", "GUI")
            .map(s -> "OreFinderWand$" + s)
            .collect(Collectors.toList());
    }

    public static final WeakHashMap<ItemStack, Integer> backpackSlotIndexMap = new WeakHashMap<>(4);
    public static final AtomicReference<ItemStack> backpackLastSample = new AtomicReference<>();

    @Mixin(value = ItemOreFinderTool.class)
    public abstract static class OreFinderLogic {

        @Redirect(
            method = "onUpdate",
            at = @At(
                value = "INVOKE",
                target = "Lcom/encraft/dz/inventory/InventoryOreFinder;getStackInSlot(I)Lnet/minecraft/item/ItemStack;"))
        private ItemStack nextSampleFromBackpackOr(InventoryOreFinder instance, int slot,
            @Local(argsOnly = true, name = "entity") Entity entity) {
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
                    final int slotIndexStart = backpackSlotIndexMap.getOrDefault(backpackItemStack, 0);
                    final int invSize = inv.getSizeInventory();

                    for (int i = 1; i <= invSize; i++) {
                        final int slotIndex = (slotIndexStart + i) % invSize;
                        ItemStack is = inv.getStackInSlot(slotIndex);
                        if (is != null) {
                            backpackSlotIndexMap.put(backpackItemStack, slotIndex);
                            backpackLastSample.set(is);
                            return is;
                        }
                    }
                    backpackLastSample.set(null);
                }
            }
            return backpackItemStack;
        }
    }

    @Mixin(value = OreFinderSearch.class, remap = false)
    public abstract static class ItemFilter {

        @Inject(method = "isAcceptableFilter", at = @At(value = "RETURN"), cancellable = true)
        private static void allowBackpacks(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.Misc.enableOreFinderWandBackpacksInteraction) {
                if (stack.getItem() instanceof ItemBackpack) {
                    cir.setReturnValue(true);
                }
            }
        }
    }

    @Mixin(value = GuiOreFinder.class)
    public abstract static class GUI {

        @Redirect(
            method = "drawGuiContainerForegroundLayer",
            at = @At(
                value = "INVOKE",
                target = "Lcom/encraft/dz/inventory/InventoryOreFinder;getStackInSlot(I)Lnet/minecraft/item/ItemStack;"))
        private ItemStack currentSampleFromBackpackOr(InventoryOreFinder instance, int slot) {
            final ItemStack backpackItemStack = instance.getStackInSlot(slot);
            if (backpackItemStack == null) return null;
            if (!ModConfig.Misc.enableOreFinderWandBackpacksInteraction) return backpackItemStack;

            if (backpackItemStack.getItem() instanceof ItemBackpack) {
                return backpackLastSample.get();
            }

            return backpackItemStack;
        }
    }

}
