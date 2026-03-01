package com.github.sladki.gtnhrates.mixins.late;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.oredict.OreDictionary;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.mixins.extras.AdjacentInventorySlot;

import codechicken.nei.SearchField;
import codechicken.nei.recipe.GuiFavoriteButton;
import codechicken.nei.recipe.Recipe;
import tconstruct.plugins.nei.CraftingStationOverlayHandler;
import tconstruct.tools.inventory.CraftingStationContainer;
import tconstruct.tools.logic.CraftingStationLogic;
import tconstruct.util.config.PHConstruct;

public class NEIBookmarksTweaks {

    // Store to check if config changed
    private static String[] blacklistedIngredientsReference;
    private static Set<String> blacklistedIngredients;

    public static List<String> mixins() {
        return Stream
            .of("GuiFavoriteButtonMixin", "CraftingStationContainerMixin", "CraftingStationOverlayHandlerMixin")
            .map(s -> "NEIBookmarksTweaks" + "$" + s)
            .collect(Collectors.toList());
    }

    public static Set<String> blacklistedIngredientsRecipes() {
        if (blacklistedIngredientsReference != ModConfig.NEI.ingredientsRecipesBlacklist) {
            blacklistedIngredientsReference = ModConfig.NEI.ingredientsRecipesBlacklist;
            blacklistedIngredients = Arrays.stream(blacklistedIngredientsReference)
                .map(s -> s.toLowerCase(Locale.ROOT))
                .collect(Collectors.toSet());
        }
        return blacklistedIngredients;
    }

    public static String itemStackOredictName(ItemStack itemStack) {
        StringBuilder builder = new StringBuilder();

        for (int id : OreDictionary.getOreIDs(itemStack)) {
            String oreDictionaryName = OreDictionary.getOreName(id);
            if (!"Unknown".equals(oreDictionaryName)) {
                builder.append(oreDictionaryName)
                    .append(",");
            }
        }

        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        return builder.toString();
    }

    public static String itemStackID(ItemStack itemStack) {
        return itemStack.getItem().delegate.name()
            + (itemStack.getItemDamage() != 0 ? "/" + itemStack.getItemDamage() : "");
    }

    @Mixin(value = GuiFavoriteButton.class, remap = false)
    public abstract static class GuiFavoriteButtonMixin {

        @Redirect(
            method = "getRecipesTree",
            at = @At(
                value = "INVOKE",
                target = "Lcodechicken/nei/recipe/Recipe;of(Lcodechicken/nei/recipe/Recipe$RecipeId;)Lcodechicken/nei/recipe/Recipe;"))
        private Recipe excludeBlacklistedIngredientsRecipes(Recipe.RecipeId recipeId) {
            if (recipeId != null && ModConfig.NEI.enableIngredientsRecipesBlacklist) {
                ItemStack itemStack = recipeId.getResult();
                if (blacklistedIngredientsRecipes().stream()
                    .anyMatch(
                        s -> itemStackID(itemStack).toLowerCase(Locale.ROOT)
                            .contains(s))
                    || blacklistedIngredientsRecipes()
                        .contains(itemStackOredictName(itemStack).toLowerCase(Locale.ROOT))
                    || blacklistedIngredientsRecipes().contains(
                        SearchField.getEscapedSearchText(itemStack)
                            .toLowerCase(Locale.ROOT))) {
                    return null;
                }
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
