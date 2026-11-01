package com.github.sladki.gtnhrates.mixins.extras;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public class AdjacentInventorySlot extends Slot {

    public final TileEntity tileEntity;

    public AdjacentInventorySlot(TileEntity tileEntity, IInventory p_i1824_1_, int p_i1824_2_, int p_i1824_3_,
        int p_i1824_4_) {
        super(p_i1824_1_, p_i1824_2_, p_i1824_3_, p_i1824_4_);
        this.tileEntity = tileEntity;
    }

    public final boolean isValid() {
        return !tileEntity.isInvalid();
    }

    @Override
    public ItemStack getStack() {
        return isValid() ? super.getStack() : null;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return isValid() && super.isItemValid(stack);
    }
}
