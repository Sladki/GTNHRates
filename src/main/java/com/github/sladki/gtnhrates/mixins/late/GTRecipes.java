package com.github.sladki.gtnhrates.mixins.late;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;

import gregtech.api.recipe.RecipeMapBackend;
import gregtech.api.util.GTRecipe;

@Mixin(value = RecipeMapBackend.class)
public abstract class GTRecipes {

    // This code have multiple reductions issue, look at EventsHandler for workaround
    // keeping the file just in case
    @Inject(method = "compileRecipe", at = @At(value = "HEAD"), remap = false)
    private void onDoAdd(GTRecipe recipe, CallbackInfoReturnable<GTRecipe> cir) {
        if (recipe.mDuration > 0) {
            recipe.mDuration = applyRate(recipe.mDuration, ModConfig.Rates.gtRecipesEnergyDiscount);
        }
    }
}
