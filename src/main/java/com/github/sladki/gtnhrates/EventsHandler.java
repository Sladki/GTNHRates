package com.github.sladki.gtnhrates;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        // Some preparations
        boolean toPrintCategory = Arrays.stream(ModConfig.Rates.gtRecipesPerCategoryEnergyDiscount)
            .findFirst()
            .filter("print"::equalsIgnoreCase)
            .isPresent();
        if (toPrintCategory) {
            GTNHRates.LOG
                .info("ModConfig.Rates.gtRecipesPerCategoryEnergyDiscount[0] is 'print', recipe categories found:");
        }

        Map<String, Float> categoriesDiscountMap = new HashMap<>();
        for (String s : ModConfig.Rates.gtRecipesPerCategoryEnergyDiscount) {
            if (s == null) continue;
            int idx = s.indexOf(":");
            if (idx < 1) continue;
            try {
                float val = Float.parseFloat(s.substring(idx + 1));
                if (val >= 0.1f && val <= 64.0f) {
                    categoriesDiscountMap.put(s.substring(0, idx), val);
                }
            } catch (NumberFormatException ignored) {}
        }

        // Do the work
        for (Map.Entry<String, RecipeMap<?>> entry : RecipeMap.ALL_RECIPE_MAPS.entrySet()) {
            String category = entry.getKey();
            if (toPrintCategory) {
                GTNHRates.LOG.info(category);
            }

            // exact
            Float discount = categoriesDiscountMap.get(category);
            // suffix
            if (discount == null) {
                int lastDot = category.lastIndexOf('.');
                if (lastDot != -1 && lastDot < category.length() - 1) {
                    String suffix = category.substring(lastDot + 1);
                    discount = categoriesDiscountMap.get(suffix);
                }
            }

            discount = discount != null ? discount : ModConfig.Rates.gtRecipesEnergyDiscount;
            for (GTRecipe recipe : entry.getValue()
                .getAllRecipes()) {
                if (recipe.mDuration > 0) {
                    recipe.mDuration = applyRate(recipe.mDuration, discount);
                }
            }
        }
    }

}
