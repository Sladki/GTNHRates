package com.github.sladki.gtnhrates;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

public abstract class Utils {

    public static ArrayList<ItemStack> multiplyItemStacksSize(List<ItemStack> itemStacks, float mult,
        boolean splitStacks) {
        ArrayList<ItemStack> newItemStacks = new ArrayList<>(itemStacks.size() * 2);
        for (ItemStack is : itemStacks) {
            if (is == null) {
                continue;
            }
            is.stackSize = (int) (is.stackSize * mult);
            newItemStacks.add(is);
            // In case of stack size > max
            while (is.stackSize > (splitStacks ? 1 : is.getMaxStackSize())) {
                newItemStacks.add(is.splitStack(is.getMaxStackSize()));
            }
        }
        return newItemStacks;
    }

    public static ArrayList<ItemStack> multiplyItemStacksSize(List<ItemStack> itemStacks, float mult) {
        return multiplyItemStacksSize(itemStacks, mult, false);
    }

    public static int applyRate(int old, float rate) {
        return (int) Math.max(1, Math.ceil(old / rate));
    }
}
