package com.github.sladki.gtnhrates;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.event.entity.player.FillBucketEvent;

import com.github.sladki.gtnhrates.mixins.extras.PersistentBlocks;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.recipe.RecipeMap;
import gregtech.api.util.GTRecipe;

public class EventsHandler {

    public static class PersistentBlocksEvents {

        private static final HashSet<MovingObjectPosition> waterBlocks = new HashSet<>();

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public void onPreBucketFill(FillBucketEvent event) {
            final MovingObjectPosition pos = event.target;
            if (event.world.getBlock(pos.blockX, pos.blockY, pos.blockZ) == Blocks.water) {
                if (PersistentBlocks
                    .tryReduceDurability(event.entityPlayer, Blocks.water, pos.blockX, pos.blockY, pos.blockZ)) {
                    waterBlocks.add(pos);
                }
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public void onPostBucketFill(FillBucketEvent event) {
            final MovingObjectPosition pos = event.target;
            if (waterBlocks.contains(pos)) {
                waterBlocks.remove(pos);
                if (!event.isCanceled() && event.world.isAirBlock(pos.blockX, pos.blockY, pos.blockZ)) {
                    event.world.setBlock(pos.blockX, pos.blockY, pos.blockZ, Blocks.water, 0, 3);
                }
            }
        }
    }

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

    public float discountForCategory(Map<String, Float> categoriesDiscountMap, String category) {
        // exact category string
        Float discount = categoriesDiscountMap.get(category);
        // suffix
        if (discount == null) {
            int lastDot = category.lastIndexOf('.');
            if (lastDot != -1 && lastDot < category.length() - 1) {
                String suffix = category.substring(lastDot + 1);
                discount = categoriesDiscountMap.get(suffix);
            }
        }
        return discount != null ? discount : ModConfig.Rates.gtRecipesEnergyDiscount;
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

            float discount = discountForCategory(categoriesDiscountMap, category);
            for (GTRecipe recipe : entry.getValue()
                .getAllRecipes()) {
                if (recipe.mDuration > 0) {
                    recipe.mDuration = applyRate(recipe.mDuration, discount);
                }
            }
        }

        // Assembly Line recipes have special needs, stick to recipe categories to avoid mismatch with NEI
        float processDiscount = discountForCategory(categoriesDiscountMap, "gt.recipe.fakeAssemblylineProcess");
        float researchDiscount = discountForCategory(categoriesDiscountMap, "gt.recipe.scanner");
        for (GTRecipe.RecipeAssemblyLine recipe : GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes) {
            if (recipe.mDuration > 0) {
                recipe.mDuration = applyRate(recipe.mDuration, processDiscount);
            }
            if (recipe.mResearchTime > 0) {
                recipe.mResearchTime = applyRate(recipe.mResearchTime, researchDiscount);
            }
        }
    }

}
