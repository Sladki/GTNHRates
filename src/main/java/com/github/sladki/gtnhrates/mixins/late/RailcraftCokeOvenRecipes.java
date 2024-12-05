package com.github.sladki.gtnhrates.mixins.late;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.sladki.gtnhrates.ModConfig;

import mods.railcraft.common.util.crafting.CokeOvenCraftingManager;

@Mixin(value = CokeOvenCraftingManager.class, remap = false)
public abstract class RailcraftCokeOvenRecipes {

    @Unique
    private boolean modified = false; // just lazy to modify args or something

    @Shadow(remap = false)
    public abstract void addRecipe(ItemStack input, boolean matchDamage, boolean matchNBT, ItemStack output,
        FluidStack fluidOutput, int cookTime);

    @Inject(method = "addRecipe", at = @At(value = "HEAD"), cancellable = true)
    private void onAddRecipe(ItemStack input, boolean matchDamage, boolean matchNBT, ItemStack output,
        FluidStack fluidOutput, int cookTime, CallbackInfo ci) {
        if (!modified) {
            modified = true;
            addRecipe(
                input,
                matchDamage,
                matchNBT,
                output,
                fluidOutput,
                applyRate(cookTime, ModConfig.Rates.gtRecipesEnergyDiscount));
            ci.cancel();
            return;
        }
        modified = false;
    }
}
