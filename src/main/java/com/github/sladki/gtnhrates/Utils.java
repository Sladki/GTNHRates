package com.github.sladki.gtnhrates;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public abstract class Utils {

    public static class Sided<T> {

        private final Supplier<T> supplier;

        private T client;
        private T server;

        public Sided(Supplier<T> supplier) {
            this.supplier = supplier;
        }

        public T get(World world) {
            if (world.isRemote) {
                client = client == null ? supplier.get() : client;
                return client;
            } else {
                server = server == null ? supplier.get() : server;
                return server;
            }
        }
    }

    private static HashMap<String, ZipEntry> zipEntries(ZipFile zipFile, String filterPath) {
        HashMap<String, ZipEntry> result = new HashMap<>();
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry zipEntry = entries.nextElement();
            String name = zipEntry.getName();
            if (!zipEntry.isDirectory() && name.startsWith(filterPath)) {
                result.put(name, zipEntry);
            }
        }
        return result;
    }

    private static JsonObject jsonFromZip(ZipFile zipFile, JsonParser jsonParser, ZipEntry zipEntry) {
        try (InputStream is = zipFile.getInputStream(zipEntry);
            InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            return jsonParser.parse(reader)
                .getAsJsonObject();
        } catch (IOException e) {
            GTNHRates.LOG.warn("Error reading {}", zipEntry.getName(), e);
            throw new RuntimeException(e);
        }
    }

    public static Map<String, JsonObject> betterQuestingFiles() {
        HashMap<String, JsonObject> result = new HashMap<>();
        try {
            File source = Loader.instance()
                .getModList()
                .stream()
                .filter(
                    m -> m.getModId()
                        .equals(GTNHRates.MODID))
                .findFirst()
                .map(ModContainer::getSource)
                .get();

            try (ZipFile zip = new ZipFile(source)) {
                return zipEntries(zip, "assets/betterquesting/").entrySet()
                    .stream()
                    .collect(
                        Collectors.toMap(Map.Entry::getKey, e -> jsonFromZip(zip, new JsonParser(), e.getValue())));
            } catch (Exception e) {
                GTNHRates.LOG.warn("Error reading the addon's .jar", e);
            }
        } catch (Exception e) {
            GTNHRates.LOG.warn("Error locating the addon's .jar", e);
        }
        return result;
    }

    public static ArrayList<ItemStack> multiplyItemStacksSize(List<ItemStack> itemStacks, float mult,
        boolean splitStacks) {
        ArrayList<ItemStack> newItemStacks = new ArrayList<>(itemStacks.size() * 2);
        for (ItemStack is : itemStacks) {
            if (is == null) {
                continue;
            }
            is.stackSize = (int) (is.stackSize * mult);
            newItemStacks.add(is);
            // In case of stack size > max
            while (is.stackSize > (splitStacks ? 1 : is.getMaxStackSize())) {
                newItemStacks.add(is.splitStack(is.getMaxStackSize()));
            }
        }
        return newItemStacks;
    }

    public static ArrayList<ItemStack> multiplyItemStacksSize(List<ItemStack> itemStacks, float mult) {
        return multiplyItemStacksSize(itemStacks, mult, false);
    }

    public static int applyRate(int old, float rate) {
        return (int) Math.max(1, Math.ceil(old / rate));
    }

    private static final Map<String, Float> categoriesDiscountMap = new HashMap<>();

    public static float recipeCategoryDiscount(String category) {
        if (categoriesDiscountMap.isEmpty()) {
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
        }

        // exact category string - gt.foo
        Float discount = categoriesDiscountMap.get(category);
        // suffix - foo
        if (discount == null) {
            int lastDot = category.lastIndexOf('.');
            if (lastDot != -1 && lastDot < category.length() - 1) {
                String suffix = category.substring(lastDot + 1);
                discount = categoriesDiscountMap.get(suffix);
            }
        }

        return discount != null ? discount : ModConfig.Rates.gtRecipesEnergyDiscount;
    }

}
