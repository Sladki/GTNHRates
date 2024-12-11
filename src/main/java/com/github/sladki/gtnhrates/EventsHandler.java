package com.github.sladki.gtnhrates;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTRecipe;

public class EventsHandler {

    private boolean modifiedRecipes = false;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (!modifiedRecipes) {
            modifiedRecipes = true;
            modifyRecipesDuration();
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!modifiedRecipes) {
            modifiedRecipes = true;
            modifyRecipesDuration();
        }
    }

    private void modifyRecipesDuration() {
        for (RecipeMap<?> recipeMap : RecipeMap.ALL_RECIPE_MAPS.values()) {
            for (GTRecipe recipe : recipeMap.getAllRecipes()) {
                if (recipe.mDuration > 0) {
                    recipe.mDuration = applyRate(recipe.mDuration, ModConfig.Rates.gtRecipesEnergyDiscount);
                }
            }
        }
    }

}
