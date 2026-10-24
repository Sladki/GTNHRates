package com.github.sladki.gtnhrates;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import java.util.Arrays;
import java.util.HashSet;

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
            printCategories();
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

    private void printCategories() {
        Arrays.stream(ModConfig.Rates.gtRecipesPerCategoryEnergyDiscount)
            .findFirst()
            .filter("print"::equalsIgnoreCase)
            .ifPresent((s) -> {
                GTNHRates.LOG
                    .info("ModConfig.Rates.gtRecipesPerCategoryEnergyDiscount[0] is 'print', recipe categories found:");
                RecipeMap.ALL_RECIPE_MAPS.keySet()
                    .stream()
                    .forEach(GTNHRates.LOG::info);
            });
    }

    private void modifyRecipesDuration() {
        RecipeMap.ALL_RECIPE_MAPS.entrySet()
            .stream()
            .forEach((rm) -> {
                rm.getValue()
                    .getAllRecipes()
                    .stream()
                    .filter(r -> r.mDuration > 0)
                    .forEach(r -> r.mDuration = applyRate(r.mDuration, Utils.recipeCategoryDiscount(rm.getKey())));
            });

        // Assembly Line recipes have special needs, stick to recipe categories to avoid mismatch with NEI
        for (GTRecipe.RecipeAssemblyLine recipe : GTRecipe.RecipeAssemblyLine.sAssemblylineRecipes) {
            if (recipe.mDuration > 0) {
                recipe.mDuration = applyRate(
                    recipe.mDuration,
                    Utils.recipeCategoryDiscount("gt.recipe.fakeAssemblylineProcess"));
            }
            if (recipe.mResearchTime > 0) {
                recipe.mResearchTime = applyRate(
                    recipe.mResearchTime,
                    Utils.recipeCategoryDiscount("gt.recipe.scanner"));
            }
        }
    }

}
