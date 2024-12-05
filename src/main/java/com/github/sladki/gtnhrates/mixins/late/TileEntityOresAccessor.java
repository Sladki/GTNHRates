package com.github.sladki.gtnhrates.mixins.late;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import gregtech.common.blocks.TileEntityOres;

@Mixin(value = TileEntityOres.class, remap = false)
public interface TileEntityOresAccessor {

    @Accessor("shouldSilkTouch")
    public static boolean getShouldSilkTouch() {
        return false;
    }
}
