package com.github.sladki.gtnhrates.mixins.extras;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import com.github.sladki.gtnhrates.ModConfig;

import codechicken.nei.SearchField;

public class NEIRecipeExpansionFilter {

    private static String[] recipeExpansionFilterRulesReference; // Store to check if the config changed
    private static Set<String> exclusionPatterns;
    private static Set<String> inclusionOverrides;

    public NEIRecipeExpansionFilter() {
        updateRules();
    }

    public static NEIRecipeExpansionFilter FILTER() {
        return new NEIRecipeExpansionFilter();
    }

    public Optional<String> decisiveRule(ItemStack itemStack) {
        String pattern;
        pattern = itemStackNEIName(itemStack).toLowerCase(Locale.ROOT);
        if (inclusionOverrides.contains(pattern)) return Optional.of("!" + pattern);

        pattern = itemStackNEIName(itemStack).toLowerCase(Locale.ROOT);
        if (exclusionPatterns.contains(pattern)) return Optional.of("exact: " + pattern);

        pattern = itemStackOredictName(itemStack).toLowerCase(Locale.ROOT);
        if (exclusionPatterns.contains(pattern)) return Optional.of("oredict: " + pattern);

        return exclusionPatterns.stream()
            .filter(
                s -> itemStackID(itemStack).toLowerCase(Locale.ROOT)
                    .contains(s))
            .findFirst()
            .map(s -> "id: " + s);
    }

    public void toggleRestriction(ItemStack itemStack) {
        Optional<String> rule = decisiveRule(itemStack);
        if (!rule.isPresent()) {
            exclusionPatterns.add(itemStackNEIName(itemStack).toLowerCase(Locale.ROOT));
        } else {
            String s = rule.get();
            if (s.startsWith("!")) {
                inclusionOverrides.remove(s.substring(1));
            } else {
                String sr = s.substring(s.indexOf(": ") + 2);
                if (s.startsWith("exact: ")) {
                    exclusionPatterns.remove(sr);
                } else {
                    inclusionOverrides.add(itemStackNEIName(itemStack).toLowerCase(Locale.ROOT));
                }
            }
        }
        saveRules();
    }

    public boolean isExcluded(ItemStack itemStack) {
        Optional<String> rule = decisiveRule(itemStack);
        return rule.isPresent() && !rule.get()
            .startsWith("!");
    }

    private void updateRules() {
        if (recipeExpansionFilterRulesReference == ModConfig.NEI.recipeExpansionFilterRules) {
            return;
        }
        recipeExpansionFilterRulesReference = ModConfig.NEI.recipeExpansionFilterRules;

        exclusionPatterns = Arrays.stream(recipeExpansionFilterRulesReference)
            .filter(s -> !s.startsWith("!"))
            .filter(s -> !s.isEmpty())
            .map(s -> s.toLowerCase(Locale.ROOT))
            .collect(Collectors.toCollection(LinkedHashSet::new));

        inclusionOverrides = Arrays.stream(recipeExpansionFilterRulesReference)
            .filter(s -> s.startsWith("!"))
            .filter(s -> s.length() > 1)
            .map(
                s -> s.substring(1)
                    .toLowerCase(Locale.ROOT))
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void saveRules() {
        ModConfig.getConfigElement(ModConfig.NEI.class, "recipeExpansionFilterRules")
            .set(
                Stream.concat(
                    exclusionPatterns.stream(),
                    inclusionOverrides.stream()
                        .map(s -> "!" + s))
                    .toArray(String[]::new));
        // to prevent reading the config again
        recipeExpansionFilterRulesReference = ModConfig.NEI.recipeExpansionFilterRules;
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

    public static String itemStackNEIName(ItemStack itemStack) {
        return SearchField.getEscapedSearchText(itemStack);
    }

}
