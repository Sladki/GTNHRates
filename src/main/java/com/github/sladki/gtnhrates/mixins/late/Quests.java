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

import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.sladki.gtnhrates.GTNHRates;
import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;
import com.google.gson.JsonObject;

import betterquesting.api.misc.ICallback;
import betterquesting.api.questing.IQuest;
import betterquesting.api.questing.IQuestLine;
import betterquesting.api.utils.NBTConverter;
import betterquesting.api2.client.gui.controls.PanelButtonStorage;
import betterquesting.api2.utils.Tuple2;
import betterquesting.client.gui2.GuiQuestLines;
import betterquesting.commands.admin.QuestCommandDefaults;
import betterquesting.questing.QuestDatabase;
import betterquesting.questing.QuestInstance;
import betterquesting.questing.QuestLineDatabase;
import betterquesting.questing.QuestLineEntry;
import cpw.mods.fml.client.config.IConfigElement;

public class Quests {

    public static List<String> mixins() {
        return Stream.of("GuiQuestLinesAccessor", "QuestCommandDefaultsMixin", "GuiQuestLinesMixin")
            .map(s -> "Quests$" + s)
            .collect(Collectors.toList());
    }

    @Mixin(value = GuiQuestLines.class, remap = false)
    public interface GuiQuestLinesAccessor {

        @Accessor("visChapters")
        List<Tuple2<Map.Entry<UUID, IQuestLine>, Integer>> guiQuestLines$getVisChapters();
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

    private static Map<UUID, String> questsToMove;

    public static Map<UUID, String> getQuestsToMoveMap() {
        if (questsToMove == null) {
            questsToMove = new HashMap<>(30);
            for (String s : ModConfig.Misc.movedQuestLines) {
                int idx = s.indexOf(":");
                if (idx < 1) continue;
                try {
                    String uuidString = s.substring(0, idx);
                    questsToMove.put(UUID.fromString(uuidString), uuidString + ":" + 1);
                } catch (NumberFormatException ignored) {}
            }
        }
        return questsToMove;
    }

    @Mixin(value = GuiQuestLines.class, remap = false)
    public abstract static class GuiQuestLinesMixin {

        @Shadow
        public abstract void refreshGui();

        @Inject(
            method = "refreshChapterVisibility",
            at = @At(
                value = "INVOKE",
                target = "Lbetterquesting/api2/client/gui/panels/lists/CanvasHoverTray;isTrayOpen()Z"))
        private void moveLinesDown(CallbackInfo ci) {
            if (!ModConfig.Misc.enableMovingQuestLines) {
                return;
            }
            List<Tuple2<Map.Entry<UUID, IQuestLine>, Integer>> visChapters = ((GuiQuestLinesAccessor) this)
                .guiQuestLines$getVisChapters();
            List<Tuple2<Map.Entry<UUID, IQuestLine>, Integer>> extractedChapters = visChapters.stream()
                .filter(
                    c -> getQuestsToMoveMap().containsKey(
                        c.getFirst()
                            .getKey()))
                .collect(Collectors.toList());

            visChapters.removeAll(extractedChapters);
            visChapters.addAll(extractedChapters);
        }

        @Redirect(
            method = "buildChapterList",
            at = @At(
                value = "INVOKE",
                target = "Lbetterquesting/api2/client/gui/controls/PanelButtonStorage;setCallback(Lbetterquesting/api/misc/ICallback;)Lbetterquesting/api2/client/gui/controls/PanelButtonStorage;"))
        private PanelButtonStorage<?> setCallbackOpenOrMoveDown(
            PanelButtonStorage<Map.Entry<UUID, IQuestLine>> instance, ICallback<Map.Entry<UUID, IQuestLine>> callback) {
            if (!ModConfig.Misc.enableMovingQuestLines) {
                return instance.setCallback(callback);
            }
            instance.setCallback(q -> {
                IConfigElement<String> config = ModConfig.getConfigElement(ModConfig.Misc.class, "movedQuestLines");
                if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                    if (getQuestsToMoveMap().containsKey(q.getKey())) {
                        getQuestsToMoveMap().remove(q.getKey());
                    } else {
                        getQuestsToMoveMap().put(
                            q.getKey(),
                            q.getKey()
                                .toString() + ":"
                                + 1);
                    }
                    config.set(
                        getQuestsToMoveMap().values()
                            .toArray(new String[0]));
                    this.refreshGui();
                } else {
                    callback.setValue(q);
                }
            });
            return instance;
        }
    }
}
