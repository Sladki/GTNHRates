package com.github.sladki.gtnhrates.mixins.late;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.mixins.extras.AdjacentInventorySlot;
import com.github.sladki.gtnhrates.mixins.extras.GuiBlacklistFavoriteButton;
import com.github.sladki.gtnhrates.mixins.extras.NEIRecipeExpansionFilter;

import codechicken.nei.recipe.GuiFavoriteButton;
import codechicken.nei.recipe.GuiRecipeButton;
import codechicken.nei.recipe.NEIRecipeWidget;
import codechicken.nei.recipe.Recipe;
import tconstruct.plugins.nei.CraftingStationOverlayHandler;
import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.tools.logic.CraftingStationLogic;
import tconstruct.util.config.PHConstruct;

public class NEIBookmarksTweaks {

    public static List<String> mixins() {
        return Stream
            .of(
                "GuiFavoriteButtonMixin",
                "CraftingStationContainerMixin",
                "CraftingStationOverlayHandlerMixin",
                "NEIRecipeWidgetMixin")
            .map(s -> "NEIBookmarksTweaks" + "$" + s)
            .collect(Collectors.toList());
    }

    @Mixin(value = NEIRecipeWidget.class, remap = false)
    public abstract static class NEIRecipeWidgetMixin {

        @Inject(method = "getDefatulButtons", at = @At(value = "RETURN"))
        private void addBlacklistFavoriteButton(CallbackInfoReturnable<List<GuiRecipeButton>> cir) {
            if (!ModConfig.NEI.enableRecipeExpansionFilter) {
                return;
            }
            for (GuiRecipeButton button : cir.getReturnValue()) {
                if (button instanceof GuiFavoriteButton favButton) {
                    cir.getReturnValue()
                        .add(
                            new GuiBlacklistFavoriteButton(
                                favButton.xPosition,
                                favButton.yPosition - GuiRecipeButton.BUTTON_HEIGHT - 1,
                                favButton));
                    return;
                }
            }
        }

    }

    @Mixin(value = GuiFavoriteButton.class, remap = false)
    public abstract static class GuiFavoriteButtonMixin {

        @Redirect(
            method = "getRecipesTree",
            at = @At(
                value = "INVOKE",
                target = "Lcodechicken/nei/recipe/Recipe;of(Lcodechicken/nei/recipe/Recipe$RecipeId;)Lcodechicken/nei/recipe/Recipe;"))
        private Recipe excludeBlacklistedIngredientsRecipes(Recipe.RecipeId recipeId) {
            if (recipeId != null && ModConfig.NEI.enableRecipeExpansionFilter) {
                ItemStack itemStack = recipeId.getResult();
                if (NEIRecipeExpansionFilter.FILTER()
                    .isExcluded(itemStack)) return null;
            }
            return Recipe.of(recipeId);
        }
    }

    @Mixin(value = CraftingStationContainer.class, remap = false)
    public abstract static class CraftingStationContainerMixin extends Container {

        @Inject(method = "<init>", at = @At(value = "TAIL"))
        private void addExtraSlotsFromNearbyContainers(InventoryPlayer inventoryplayer, CraftingStationLogic logic,
            int x, int y, int z, CallbackInfo ci) {
            if (!ModConfig.NEI.enableTCCraftingStationAdjacentInventoriesSearch) {
                return;
            }

            final int radius = ModConfig.NEI.enableTCCraftingStationAdjacentInventoriesSearchRadius;
            for (int zz = -radius; zz <= radius; zz++) {
                for (int yy = -radius; yy <= radius; yy++) {
                    for (int xx = -radius; xx <= radius; xx++) {
                        if (zz == 0 && yy == 0 && xx == 0) continue;

                        final int xPos = x + xx, yPos = y + yy, zPos = z + zz;
                        final TileEntity tile = logic.getWorldObj()
                            .getTileEntity(xPos, yPos, zPos);
                        if (!(tile instanceof IInventory inv) || (tile instanceof CraftingStationLogic)
                            || gtnhRates$isBlacklisted(tile.getClass())) {
                            continue;
                        }

                        if (inv == logic.getFirstInventory() || inv == logic.getSecondInventory()
                            || !inv.isUseableByPlayer(inventoryplayer.player)) {
                            continue;
                        }

                        // dirty hack to workaround bogo sorter messing with read only empty slots
                        boolean uniformSlots = true;
                        ItemStack is = new ItemStack(Blocks.stone);
                        for (int i = 0; i < inv.getSizeInventory(); i++) {
                            if (!inv.isItemValidForSlot(i, is)) {
                                uniformSlots = false;
                                break;
                            }
                        }
                        if (!uniformSlots) continue;

                        TileEntity te = (TileEntity) inv;
                        for (int i = 0; i < inv.getSizeInventory(); i++) {
                            this.addSlotToContainer(new AdjacentInventorySlot(te, inv, i, -9999, -9999));
                        }
                    }
                }
            }
        }

        @Unique
        private boolean gtnhRates$isBlacklisted(Class<? extends TileEntity> aClass) {
            return PHConstruct.craftingStationBlacklist.contains(aClass.getName());
        }

    }

    @Mixin(value = CraftingStationOverlayHandler.class, remap = false)
    public abstract static class CraftingStationOverlayHandlerMixin {

        @Inject(method = "canMoveFrom", at = @At(value = "TAIL"), cancellable = true)
        private void allowToMoveFromAdjacentInventories(Slot slot, GuiContainer gui,
            CallbackInfoReturnable<Boolean> cir) {
            if (ModConfig.NEI.enableTCCraftingStationAdjacentInventoriesSearch
                && slot instanceof AdjacentInventorySlot) {
                cir.setReturnValue(true);
                cir.cancel();
            }
        }

    }

}
