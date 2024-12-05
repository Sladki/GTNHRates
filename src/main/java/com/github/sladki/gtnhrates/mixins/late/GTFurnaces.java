package com.github.sladki.gtnhrates.mixins.late;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;

import gregtech.api.interfaces.ITexture;
import gregtech.api.metatileentity.implementations.MTEBasicMachineBronze;
import gregtech.api.recipe.maps.FurnaceBackend;
import gregtech.api.util.GTRecipe;
import gregtech.common.tileentities.machines.steam.MTESteamFurnaceBronze;
import gregtech.common.tileentities.machines.steam.MTESteamFurnaceSteel;

@Mixin(value = FurnaceBackend.class, remap = false)
public abstract class GTFurnaces {

    @Inject(method = "overwriteFindRecipe", at = @At("TAIL"))
    private void onOverwriteFindRecipe(ItemStack[] items, FluidStack[] fluids, ItemStack specialSlot,
        GTRecipe cachedRecipe, CallbackInfoReturnable<GTRecipe> cir) {
        if (cir.getReturnValue() != null) {
            cir.getReturnValue().mDuration = applyRate(
                cir.getReturnValue().mDuration,
                ModConfig.Rates.gtRecipesEnergyDiscount);
        }
    }

    @Mixin(value = MTESteamFurnaceBronze.class, remap = false)
    public abstract static class BronzeFurnace extends MTEBasicMachineBronze {

        @Inject(method = "checkRecipe", at = @At("RETURN"))
        private void onCheckRecipe(CallbackInfoReturnable<Integer> cir) {
            this.mMaxProgresstime = applyRate(this.mMaxProgresstime, ModConfig.Rates.gtRecipesEnergyDiscount);
        }

        public BronzeFurnace(String aName, String[] aDescription, ITexture[][][] aTextures, int aInputSlotCount,
            int aOutputSlotCount, boolean aHighPressure) {
            super(aName, aDescription, aTextures, aInputSlotCount, aOutputSlotCount, aHighPressure);
        }
    }

    @Mixin(value = MTESteamFurnaceSteel.class, remap = false)
    public abstract static class SteelFurnace extends MTEBasicMachineBronze {

        @Inject(method = "checkRecipe", at = @At("RETURN"))
        private void onCheckRecipe(CallbackInfoReturnable<Integer> cir) {
            this.mMaxProgresstime = applyRate(this.mMaxProgresstime, ModConfig.Rates.gtRecipesEnergyDiscount);
        }

        public SteelFurnace(String aName, String[] aDescription, ITexture[][][] aTextures, int aInputSlotCount,
            int aOutputSlotCount, boolean aHighPressure) {
            super(aName, aDescription, aTextures, aInputSlotCount, aOutputSlotCount, aHighPressure);
        }
    }
}
