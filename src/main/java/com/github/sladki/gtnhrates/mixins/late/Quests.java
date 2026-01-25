package com.github.sladki.gtnhrates.mixins.late;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.command.ICommandSender;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.sladki.gtnhrates.GTNHRates;
import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;
import com.google.gson.JsonObject;

import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.utils.NBTConverter;
import betterquesting.commands.admin.QuestCommandDefaults;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;

public class Quests {

    public static List<String> mixins() {
        return Stream.of("QuestCommandDefaultsMixin")
            .map(s -> "Quests$" + s)
            .collect(Collectors.toList());
    }

    @Mixin(value = QuestCommandDefaults.class, remap = false)
    public abstract static class QuestCommandDefaultsMixin {

        @Inject(
            method = "load",
            at = @At(
                value = "INVOKE",
                target = "Lbetterquesting/questing/QuestDatabase;clear()V",
                shift = At.Shift.AFTER))
        private static void injectQuests(ICommandSender sender, String databaseName, File dataDir,
            boolean loadWorldSettings, CallbackInfo ci) {
            if (!ModConfig.Misc.enableNewQuests) {
                return;
            }

            Map<String, JsonObject> betterQuestingFiles = Utils.betterQuestingFiles();
            Function<JsonObject, NBTTagCompound> readNbt = jsonObject -> NBTConverter
                .JSONtoNBT_Object(jsonObject, new NBTTagCompound(), true);

            // Quest lines
            HashMap<String, HashMap<String, JsonObject>> splitByQuestlines = new HashMap<>();
            betterQuestingFiles.forEach((path, jsonObject) -> {
                if (path.startsWith("assets/betterquesting/questlines/")) {
                    String relativePath = path.substring("assets/betterquesting/questlines/".length());
                    String[] segments = relativePath.split("/");
                    if (segments.length == 2) {
                        splitByQuestlines.computeIfAbsent(segments[0], k -> new HashMap<>())
                            .put(segments[1], jsonObject);
                    }
                }
            });

            splitByQuestlines.values()
                .forEach(map -> {
                    JsonObject questlineJsonEntry = map.get("QuestLine.json");
                    if (questlineJsonEntry != null) {
                        NBTTagCompound questLineTag = readNbt.apply(questlineJsonEntry);
                        UUID questLineId = NBTConverter.UuidValueType.QUEST_LINE.readId(questLineTag);
                        map.forEach((name, zipEntry) -> {
                            if (!name.equals("QuestLine.json")) {
                                NBTTagCompound questLineEntryTag = readNbt.apply(zipEntry);
                                UUID questId = NBTConverter.UuidValueType.QUEST.readId(questLineEntryTag);
                                IQuestLine questline = QuestLineDatabase.INSTANCE.get(questLineId);
                                if (questline != null) {
                                    questline.put(questId, new QuestLineEntry(questLineEntryTag));
                                } else {
                                    GTNHRates.LOG.warn("No such questline {}", questLineId);
                                }
                            }
                        });
                    }
                });

            // Quests
            betterQuestingFiles.forEach((path, jsonObject) -> {
                if (path.startsWith("assets/betterquesting/quests/")) {
                    NBTTagCompound questTag = readNbt.apply(jsonObject);
                    UUID questId = NBTConverter.UuidValueType.QUEST.readId(questTag);

                    IQuest quest = new QuestInstance();
                    quest.readFromNBT(questTag);
                    QuestDatabase.INSTANCE.put(questId, quest);
                }
            });
        }
    }
}
