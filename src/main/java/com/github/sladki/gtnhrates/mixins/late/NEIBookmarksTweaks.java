package com.github.sladki.gtnhrates.mixins.late;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.sladki.gtnhrates.ModConfig;

import codechicken.nei.SearchField;
import codechicken.nei.recipe.GuiFavoriteButton;
import codechicken.nei.recipe.Recipe;

public class NEIBookmarksTweaks {

    // Store to check if config changed
    private static String[] blacklistedIngredientsReference;
    private static Set<String> blacklistedIngredients;

    public static List<String> mixins() {
        return Stream.of("GuiFavoriteButtonMixin")
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

}
