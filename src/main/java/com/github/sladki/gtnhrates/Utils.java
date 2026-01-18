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
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.item.ItemStack;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

public abstract class Utils {

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
}
