package com.github.sladki.gtnhrates.mixins.extras;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

import org.lwjgl.opengl.GL11;

import codechicken.nei.FavoriteRecipes;
import codechicken.nei.drawable.DrawableBuilder;
import codechicken.nei.drawable.DrawableResource;
import codechicken.nei.recipe.GuiFavoriteButton;
import codechicken.nei.recipe.GuiRecipeButton;
import codechicken.nei.recipe.Recipe;
import codechicken.nei.recipe.StackInfo;

public class GuiBlacklistFavoriteButton extends GuiRecipeButton {

    public static final int BUTTON_ID = -228;

    protected static final DrawableResource ICON_STATE_OFF = new DrawableBuilder(
        "gtnhrates:textures/nei_sprites.png",
        0,
        0,
        8,
        10).setTextureSize(32, 32)
            .build();
    protected static final DrawableResource ICON_STATE_ON = new DrawableBuilder(
        "gtnhrates:textures/nei_sprites.png",
        9,
        0,
        8,
        10).setTextureSize(32, 32)
            .build();

    private final GuiFavoriteButton favoriteButton;
    private final ItemStack recipeResult;
    public boolean lastIsFavorite = false;
    public boolean filtered = false;
    public List<String> tooltip = Collections.singletonList("");

    public GuiBlacklistFavoriteButton(int x, int y, GuiFavoriteButton favoriteButton) {
        super(favoriteButton.handlerRef, x, y, BUTTON_ID, "F");
        this.favoriteButton = favoriteButton;
        this.visible = false;

        ItemStack recipeResult = null;
        Recipe recipe = Recipe.of(favoriteButton.handlerRef);
        ItemStack stack = FavoriteRecipes.getFavorite(recipe.getRecipeId());
        if (stack == null) {
            stack = recipe.getResult();
        }
        for (Recipe.RecipeIngredient result : recipe.getResults()) {
            if (StackInfo.equalItemAndNBT(result.getItemStack(), stack, true)) {
                recipeResult = result.getItemStack();
                break;
            }
        }
        this.recipeResult = recipeResult;
        updateButton();
    }

    public void updateButton() {
        Optional<String> ruleOpt = NEIRecipeExpansionFilter.FILTER()
            .decisiveRule(recipeResult);
        tooltip = new ArrayList<>();

        if (!ruleOpt.isPresent()) {
            filtered = false;
            tooltip.add("This recipe will be included in recipe trees");
            tooltip.add(EnumChatFormatting.GRAY + "Click to exclude this recipe");
        } else {
            String rule = ruleOpt.get();
            boolean isForceInclude = rule.startsWith("!");
            filtered = !isForceInclude;

            tooltip.add(
                isForceInclude ? "This recipe will be included in recipe trees"
                    : "This recipe will be excluded from recipe trees");
            tooltip.add(EnumChatFormatting.GRAY + "Rule: " + rule);

            String action = filtered ? "include" : "exclude";
            tooltip.add(EnumChatFormatting.GRAY + "Click to " + action + " this recipe");
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        NEIRecipeExpansionFilter.FILTER()
            .toggleRestriction(recipeResult);
        updateButton();
    }

    @Override
    public void update() {
        final boolean isFavorite = favoriteButton.isFavorite();
        if (lastIsFavorite != isFavorite) {
            lastIsFavorite = isFavorite;
            if (favoriteButton.visible) {
                visible = isFavorite;
                updateButton();
            }
        }
    }

    @Override
    protected void drawContent(Minecraft minecraft, int y, int x, boolean mouseOver) {
        final DrawableResource icon = filtered ? ICON_STATE_ON : ICON_STATE_OFF;
        final int iconX = this.xPosition + (this.width - icon.width - 1) / 2;
        final int iconY = this.yPosition + (this.height - icon.height) / 2;

        GL11.glColor4f(1, 1, 1, this.enabled ? 1 : 0.5f);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        icon.draw(iconX, iconY);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glColor4f(1, 1, 1, 1);
    }

    @Override
    public List<String> handleTooltip(List<String> currenttip) {
        return visible ? tooltip : null;
    }

    @Override
    public Map<String, String> handleHotkeys(int mousex, int mousey, Map<String, String> hotkeys) {
        return new HashMap<>();
    }

    @Override
    public void lastKeyTyped(char keyChar, int keyID) {

    }

    @Override
    public void drawItemOverlay() {

    }
}
