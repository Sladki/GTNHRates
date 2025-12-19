package com.github.sladki.gtnhrates.mixins.late;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import codechicken.nei.BookmarkPanel;
import codechicken.nei.ItemPanels;
import codechicken.nei.LayoutManager;
import codechicken.nei.NEIClientConfig;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.bookmark.BookmarkGrid;
import codechicken.nei.bookmark.BookmarkGridGenerator;
import codechicken.nei.bookmark.BookmarkGroup;
import codechicken.nei.bookmark.BookmarkItem;
import codechicken.nei.bookmark.BookmarkStorage;
import codechicken.nei.bookmark.BookmarksGridSlot;
import codechicken.nei.bookmark.GroupingItem;
import codechicken.nei.bookmark.SortableGroup;
import codechicken.nei.bookmark.SortableItem;
import cpw.mods.fml.common.registry.GameRegistry;

public class NEIBookmarksContents {

    public static final int neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES = -228;
    public static final int neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH = -229;

    private static final Map<BookmarkItem, Integer> bookmarkToNamespaceIndexMap = new HashMap<>();
    private static final Map<BookmarkItem, String> bookmarkToSearchStringMap = new HashMap<>();

    private static File bookmarkFile;

    private static BookmarkGrid contentsGrid;

    public static List<String> mixins() {
        if (!ModConfig.Rates.enableNEIBookmarksContents) {
            return Collections.emptyList();
        }
        return Stream
            .of(
                "BookmarkPanelAccessor",
                "BookmarkGridAccessor",
                "BookmarkStorageAccessor",
                "BookmarkGridGeneratorMixin",
                "BookmarkGridMixin",
                "BookmarkPanelMixin",
                "BookmarkStorageMixin")
            .map(s -> "NEIBookmarksContents$" + s)
            .collect(Collectors.toList());
    }

    public static Map<BookmarkItem, Integer> getBookmarkToNamespaceIndexMap() {
        return bookmarkToNamespaceIndexMap;
    }

    public static Map<BookmarkItem, String> getBookmarkToSearchStringMap() {
        return bookmarkToSearchStringMap;
    }

    public static BookmarkGrid getContentsGrid() {
        if (contentsGrid == null) {
            contentsGrid = new BookmarkGrid();
        }
        return contentsGrid;
    }

    public static boolean isContentsOpen() {
        BookmarkStorage storage = ((BookmarkPanelAccessor) ItemPanels.bookmarkPanel).neiBookmarksContents$getStorage();
        return storage.getActiveGrid() == getContentsGrid();
    }

    public static void recreateBookmarkGroup(BookmarkGrid grid, int groupId) {
        grid.removeGroup(groupId);
        ((BookmarkGridAccessor) grid).neiBookmarksContents$getGroups()
            .put(groupId, new BookmarkGroup(BookmarkPanel.BookmarkViewMode.DEFAULT));
    }

    public static Map<ItemStack, String> loadSearchBookmarks(File neiBookmarkFile) {
        bookmarkFile = new File(neiBookmarkFile.getParentFile(), "search_bookmarks.ini");
        final Map<ItemStack, String> resultMap = new LinkedHashMap<>();

        try {
            if (bookmarkFile.createNewFile()) {
                FileWriter writer = new FileWriter(bookmarkFile);
                writer.write(
                    "[{\"modid\":\"TConstruct\",\"name\":\"heartCanister\",\"meta\":1,\"searchString\":\"%favorites\"}]");
                writer.close();
            }
        } catch (IOException e) {
            NEIClientConfig.logger.error("Failed to create search bookmarks file {}", bookmarkFile, e);
        }

        if (!bookmarkFile.exists()) {
            return resultMap;
        }

        try {
            FileReader reader = new FileReader(bookmarkFile);

            JsonParser parser = new JsonParser();
            JsonElement rootElement = parser.parse(reader);

            if (rootElement.isJsonArray()) {
                JsonArray rootArray = rootElement.getAsJsonArray();

                for (JsonElement element : rootArray) {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();

                        if (obj.has("modid") && obj.has("name") && obj.has("searchString")) {
                            String modId = obj.get("modid")
                                .getAsString();
                            String name = obj.get("name")
                                .getAsString();
                            int meta = obj.has("meta") ? obj.get("meta")
                                .getAsInt() : 0;
                            String searchString = obj.get("searchString")
                                .getAsString();

                            Item item = GameRegistry.findItem(modId, name);
                            if (item != null) {
                                resultMap.put(new ItemStack(item, 1, meta), searchString);
                            }
                        }
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            NEIClientConfig.logger.error("Failed reading search bookmarks file {}", bookmarkFile, e);
        }

        return resultMap;
    }

    public static void saveSearchBookmarks(List<BookmarkItem> bookmarkItems) {
        if (!bookmarkFile.exists()) {
            return;
        }

        final Gson gson = new GsonBuilder().setPrettyPrinting()
            .create();
        JsonArray rootArray = new JsonArray();

        for (BookmarkItem bookmarkItem : bookmarkItems) {
            Map.Entry<BookmarkItem, String> match = bookmarkToSearchStringMap.entrySet()
                .stream()
                .filter(e -> e.getKey() == bookmarkItem)
                .findFirst()
                .orElse(null);

            if (match != null) {
                ItemStack itemStack = match.getKey().itemStack;
                String searchString = match.getValue();

                if (itemStack != null && itemStack.getItem() != null) {
                    GameRegistry.UniqueIdentifier uid = GameRegistry.findUniqueIdentifierFor(itemStack.getItem());
                    if (uid != null) {
                        JsonObject searchBookmarkObj = new JsonObject();

                        searchBookmarkObj.addProperty("modid", uid.modId);
                        searchBookmarkObj.addProperty("name", uid.name);
                        searchBookmarkObj.addProperty("meta", itemStack.getItemDamage());
                        searchBookmarkObj.addProperty("searchString", searchString);

                        rootArray.add(searchBookmarkObj);
                    }
                }
            }
        }

        try {
            FileWriter writer = new FileWriter(bookmarkFile);
            gson.toJson(rootArray, writer);
            writer.close();
        } catch (IOException e) {
            NEIClientConfig.logger.error("Failed saving search bookmarks file {}", bookmarkFile, e);
        }
    }

    @Mixin(value = BookmarkPanel.class, remap = false)
    public interface BookmarkPanelAccessor {

        @Accessor("storage")
        BookmarkStorage neiBookmarksContents$getStorage();
    }

    @Mixin(value = BookmarkGrid.class, remap = false)
    public interface BookmarkGridAccessor {

        @Accessor("bookmarkItems")
        List<BookmarkItem> neiBookmarksContents$getBookmarkItems();

        @Accessor("groups")
        Map<Integer, BookmarkGroup> neiBookmarksContents$getGroups();
    }

    @Mixin(value = BookmarkStorage.class, remap = false)
    public interface BookmarkStorageAccessor {

        @Accessor("namespaces")
        List<BookmarkGrid> neiBookmarksContents$getNamespaces();

        @Invoker("setNamespace")
        void neiBookmarksContents$setNamespace(int i);
    }

    @Mixin(value = BookmarkGridGenerator.class, remap = false)
    public abstract static class BookmarkGridGeneratorMixin {

        @Shadow
        @Final
        public Map<Integer, Integer> itemToSlot;

        @Shadow
        public List<BookmarksGridSlot> gridMask;

        @Redirect(
            method = "nextSlotIndex",
            at = @At(value = "INVOKE", target = "Lcodechicken/nei/bookmark/BookmarkGrid;isInvalidSlot(I)Z"))
        private boolean reserveControlSlotsIndices(BookmarkGrid grid, int index) {
            if (index < 1 && !isContentsOpen()) {
                return true;
            }
            return grid.isInvalidSlot(index);
        }

        @Inject(method = "generateGroups", at = @At(value = "TAIL"))
        private void addControlItems(CallbackInfo ci) {
            if (isContentsOpen()) {
                return;
            }
            itemToSlot.put(-99, -99);
            gridMask.add(
                new BookmarksGridSlot(
                    0,
                    -99,
                    0,
                    0,
                    0,
                    BookmarkItem.of(-1, new ItemStack(Items.book)),
                    null,
                    new BookmarkGroup(BookmarkPanel.BookmarkViewMode.DEFAULT)));
        }
    }

    @Mixin(value = BookmarkGrid.class, remap = false)
    public abstract static class BookmarkGridMixin {

        @ModifyVariable(method = "drawShadowGroup", at = @At(value = "HEAD"), ordinal = 0)
        private int skipDrawingControlShadowGroup(int rowIndexStart) {
            return rowIndexStart >= 1 || isContentsOpen() ? rowIndexStart : 1;
        }

        @ModifyArg(method = "drawGroup", at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"), index = 1)
        private int skipDrawingControlGroup(int rowIndexStart) {
            return rowIndexStart >= 1 || isContentsOpen() ? rowIndexStart : 1;
        }

        @Inject(method = "shiftItemAmount", at = @At(value = "HEAD"), cancellable = true)
        private void disableShiftForControlItems(int targetItemIndex, long shift, CallbackInfo ci) {
            if (targetItemIndex == -99) {
                ci.cancel();
            }
        }

        // required for control item on the last page
        @Inject(method = "isEmpty", at = @At(value = "HEAD"), cancellable = true)
        private void neverEmpty(CallbackInfoReturnable<Boolean> cir) {
            cir.setReturnValue(false);
            cir.cancel();
        }

        @Inject(method = "addItem", at = @At(value = "HEAD"))
        private void rememberSearchBookmark(BookmarkItem item, boolean animate, CallbackInfo ci) {
            if (item.groupId != neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH || item.recipeId != null
                || !isContentsOpen()) {
                return;
            }

            if (!getBookmarkToSearchStringMap().containsKey(item)) {
                String s = LayoutManager.searchField.text();
                getBookmarkToSearchStringMap().keySet()
                    .removeIf(k -> k.itemStack.isItemEqual(item.itemStack));
                getBookmarkToSearchStringMap().put(item, s);
            }
        }

    }

    @Mixin(value = BookmarkPanel.class, remap = false)
    public abstract static class BookmarkPanelMixin {

        @Shadow
        public SortableItem sortableItem;

        @Shadow
        public SortableGroup sortableGroup;

        @Shadow
        public GroupingItem groupingItem;

        @Shadow
        public abstract BookmarksGridSlot getSlotMouseOver(int mousex, int mousey);

        @ModifyVariable(
            method = "addItem(Lnet/minecraft/item/ItemStack;Lcodechicken/nei/recipe/Recipe$RecipeId;I)Z",
            at = @At("HEAD"))
        private int properGroupForSearchItem(int groupId) {
            return isContentsOpen() ? neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH : groupId;
        }

        @Inject(method = "mouseDragged", at = @At(value = "HEAD"), cancellable = true)
        private void disableGroupingForContents(int mousex, int mousey, int button, long heldTime, CallbackInfo ci) {
            if (isContentsOpen() && groupingItem != null) {
                ci.cancel();
            }
        }

        @Inject(method = "removeGroup", at = @At(value = "HEAD"), cancellable = true)
        private void disableRemovingGroupsForContents(int groupId, CallbackInfoReturnable<Boolean> cir) {
            if (isContentsOpen() && (groupId == neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES
                || groupId == neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH)) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }

        @Inject(method = "removeSlot", at = @At(value = "HEAD"), cancellable = true)
        private void disableRemovingItemsForContents(int mousex, int mousey, boolean removeFullRecipe,
            CallbackInfoReturnable<Boolean> cir) {
            if (isContentsOpen()) {
                final BookmarksGridSlot slot = getSlotMouseOver(mousex, mousey);
                if (slot != null && slot.getGroupId() == neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES) {
                    cir.setReturnValue(false); // true to disable adding as a search item
                    cir.cancel();
                }
            }
        }

        @Inject(method = "mouseUp", at = @At(value = "HEAD"), cancellable = true)
        private void handleClick(int mousex, int mousey, int button, CallbackInfo ci) {
            final BookmarksGridSlot slot = ItemPanels.bookmarkPanel.getGrid()
                .getSlotMouseOver(mousex, mousey);
            if (slot == null) {
                return;
            }

            BookmarkStorage storage = ((BookmarkPanelAccessor) ItemPanels.bookmarkPanel)
                .neiBookmarksContents$getStorage();

            if (!isContentsOpen()) {
                // back to contents table button
                if (slot.slotIndex != 0) {
                    return;
                }
                ((BookmarkStorageAccessor) storage).neiBookmarksContents$setNamespace(0);
                NEIClientUtils.playClickSound();
            } else {
                // search items
                if (sortableItem == null) {
                    if (slot.getGroupId() == neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES) {
                        int namespaceIndex = getBookmarkToNamespaceIndexMap().getOrDefault(slot.getBookmarkItem(), 0);
                        if (namespaceIndex > 0) {
                            ((BookmarkStorageAccessor) storage).neiBookmarksContents$setNamespace(namespaceIndex);
                        }
                        NEIClientUtils.playClickSound();
                    } else if (slot.getGroupId() == neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH) {
                        String s = getBookmarkToSearchStringMap().get(slot.getBookmarkItem());
                        if (s != null) {
                            LayoutManager.searchField.setText(s);
                            NEIClientUtils.playClickSound();
                        }
                    }
                } else {
                    // jump to namespace
                    List<BookmarkGrid> namespaces = ((BookmarkStorageAccessor) storage)
                        .neiBookmarksContents$getNamespaces();
                    int originalIndex = getBookmarkToNamespaceIndexMap().getOrDefault(slot.getBookmarkItem(), 0);
                    if (originalIndex > 0) {
                        int newIndex = -1;
                        List<BookmarkItem> bookmarkItems = ((BookmarkGridAccessor) sortableItem.grid)
                            .neiBookmarksContents$getBookmarkItems();
                        for (int i = 0; i < bookmarkItems.size(); i++) {
                            if (bookmarkItems.get(i).groupId == neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES) {
                                newIndex = sortableItem.getItemIndex() - i + 1;
                                break;
                            }
                        }
                        BookmarkGrid namespace = namespaces.get(originalIndex);
                        namespaces.remove(originalIndex);
                        namespaces.add(newIndex, namespace);
                        ((BookmarkStorageAccessor) storage).neiBookmarksContents$setNamespace(0);
                    }
                }
            }
            sortableItem = null;
            sortableGroup = null;
            groupingItem = null;
            ((BookmarkPanel) (Object) this).mouseDownSlot = -1;

            ci.cancel();
        }

    }

    @Mixin(value = BookmarkStorage.class, remap = false)
    public abstract static class BookmarkStorageMixin {

        @Shadow
        protected List<BookmarkGrid> namespaces;

        @Shadow
        protected int activeNamespaceIndex;

        @Inject(method = "load", at = @At(value = "TAIL"))
        private void loadContentsNamespace(File bookmarkFile, CallbackInfo ci) {
            try {
                this.namespaces.add(0, getContentsGrid());

                // setNamespace(0) in target method did it already
                if (activeNamespaceIndex != 0) {
                    recreateBookmarkGroup(getContentsGrid(), neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES);
                    getBookmarkToNamespaceIndexMap().clear();
                }

                getBookmarkToSearchStringMap().clear();
                recreateBookmarkGroup(getContentsGrid(), neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH);

                for (Map.Entry<ItemStack, String> entry : loadSearchBookmarks(bookmarkFile).entrySet()) {
                    BookmarkItem bookmarkItem = BookmarkItem
                        .of(neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH, entry.getKey());
                    bookmarkItem.amount = 0;
                    getContentsGrid().addItem(bookmarkItem, true);
                    getBookmarkToSearchStringMap().put(bookmarkItem, entry.getValue());
                }
            } catch (Exception e) {
                NEIClientConfig.logger.error("Error in loading NEI search bookmarks (post)", e);
            }
        }

        @Inject(method = "removeEmptyNamespaces", at = @At(value = "HEAD"), cancellable = true)
        private void preserveContentsNamespace(CallbackInfoReturnable<Boolean> cir) {
            if (activeNamespaceIndex == 0) {
                cir.setReturnValue(false);
                cir.cancel();
            }
        }

        @Inject(method = "setNamespace", at = @At("HEAD"))
        private void refreshContentsNamespace(int namespaceIndex, CallbackInfo ci) {
            if (namespaceIndex != 0) {
                return;
            }

            recreateBookmarkGroup(getContentsGrid(), neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES);

            Map<BookmarkItem, Integer> updatedMap = new LinkedHashMap<>();
            int contentsGridOffset = namespaces.get(0) == getContentsGrid() ? 1 : 0;
            for (int i = contentsGridOffset; i < namespaces.size(); i++) {
                BookmarkItem namespaceItem = namespaces.get(i)
                    .getBookmarkItem(0);
                if (namespaceItem == null) {
                    continue;
                }
                BookmarkItem bookmarkItem = getBookmarkToNamespaceIndexMap().entrySet()
                    .stream()
                    .filter(e -> e.getKey().itemStack.isItemEqual(namespaceItem.itemStack))
                    .findFirst()
                    .map(Map.Entry::getKey)
                    .orElseGet(() -> {
                        BookmarkItem item = namespaceItem.copy();
                        item.groupId = neiBookmarksContents$BOOKMARK_GROUP_ID_NAMESPACES;
                        item.recipeId = null;
                        return item;
                    });

                updatedMap.put(bookmarkItem, i + 1 - contentsGridOffset);
                getContentsGrid().addItem(bookmarkItem, true);
            }

            // put namespaces group to the top
            if (getContentsGrid().size() > updatedMap.size()) {
                List<BookmarkItem> bookmarkItems = ((BookmarkGridAccessor) getContentsGrid())
                    .neiBookmarksContents$getBookmarkItems();
                bookmarkItems.removeAll(updatedMap.keySet());
                bookmarkItems.addAll(0, updatedMap.keySet());
            }

            getBookmarkToNamespaceIndexMap().clear();
            getBookmarkToNamespaceIndexMap().putAll(updatedMap);
        }

        @Inject(method = "save", at = @At(value = "HEAD"))
        private void saveRemoveContentsNamespaceTemporarily(CallbackInfo ci) {
            try {
                namespaces.remove(getContentsGrid());
            } catch (Exception e) {
                NEIClientConfig.logger.error("Error in saving NEI search bookmarks (pre)", e);
            }
        }

        @Inject(method = "save", at = @At(value = "TAIL"))
        private void saveAndRestoreContentsNamespace(CallbackInfo ci) {
            try {
                namespaces.add(0, getContentsGrid());
                saveSearchBookmarks(
                    ((BookmarkGridAccessor) getContentsGrid()).neiBookmarksContents$getBookmarkItems()
                        .stream()
                        .filter(bi -> bi.groupId == neiBookmarksContents$BOOKMARK_GROUP_ID_SEARCH)
                        .collect(Collectors.toList()));
            } catch (Exception e) {
                NEIClientConfig.logger.error("Error in saving NEI search bookmarks (post)", e);
            }
        }
    }
}
