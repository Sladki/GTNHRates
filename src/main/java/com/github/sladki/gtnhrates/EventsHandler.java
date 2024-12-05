package com.github.sladki.gtnhrates;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTRecipe;

public class EventsHandler {

    private boolean serverTickedOnce = false;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (!serverTickedOnce) {
            serverTickedOnce = true;
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
