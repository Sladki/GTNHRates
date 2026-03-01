package com.github.sladki.gtnhrates;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;

public class ModConfig {

    protected static Class<?>[] configClasses = { Rates.class, NEI.class, Misc.class };

    @Config(modid = GTNHRates.MODID, category = "nei")
    public static class NEI {

        @Config.Comment("[Requires restart] NEI bookmarks table of content: lists bookmarks namespaces on the first namespace and allows to alias searches")
        @Config.DefaultBoolean(true)
        public static boolean enableNEIBookmarksContents;

        @Config.Comment("Prevents listed items recipes to be included in recipe trees")
        @Config.DefaultBoolean(true)
        public static boolean enableIngredientsRecipesBlacklist;

        @Config.Comment("Prevents listed items recipes to be included in recipe trees. Checks for id (substring) -> oredict (exact) -> name (exact) as collected with NEI hotkeys")
        @Config.DefaultStringList({ "logWood", "Cobblestone", "gregtech:gt.metatool.01" })
        public static String[] ingredientsRecipesBlacklist;

        @Config.Comment("Allows autocrafting in Crafting Station to use nearby inventories (slots are hidden).")
        @Config.DefaultBoolean(true)
        public static boolean enableTCCraftingStationAdjacentInventoriesSearch;

        @Config.Comment("The 'nearby' inventories distance")
        @Config.RangeInt(min = 1, max = 228)
        public static int enableTCCraftingStationAdjacentInventoriesSearchRadius = 2;

    }

    @Config(modid = GTNHRates.MODID, category = "misc")
    public static class Misc {

        @Config.Comment("Automatically open Item Holder covers inventories all at once, and disable shift clicking")
        @Config.DefaultBoolean(true)
        public static boolean gtItemHolderCoverOpenAuto;

        @Config.Comment("GT Hammer ore prospecting overhaul: smaller radius (adjustable), but scans all blocks in the radius")
        @Config.DefaultBoolean(true)
        public static boolean gtHammerOreProspectingOverhaul;

        @Config.Comment("GT Hammer ore prospecting radius")
        @Config.RangeInt(min = 1, max = 228)
        public static int gtHammerOreProspectingRadius = 2;

        @Config.Comment("Vanilla (and derivatives) and Natura crops growth overhaul. Makes crops growth dependent on time and less random")
        @Config.DefaultBoolean(true)
        public static boolean cropsGrowthOverhaul;

        @Config.Comment("Growth overhaul minimum time to mature (in seconds)")
        @Config.RangeInt(min = 1)
        public static int cropsTimeToMature = 5 * 60;

        @Config.Comment("New quests")
        @Config.DefaultBoolean(true)
        public static boolean enableNewQuests;

        @Config.Comment("Singleblock Miners stacking up to 4 blocks")
        @Config.DefaultBoolean(true)
        public static boolean enableMinerStacking;
    }

    @Config(modid = "gtnhrates")
    static public class Rates {

        @Config.Comment("Vanilla (and derivatives) and Natura crops yield rate")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float cropsYield = 4F;

        @Config.Comment("IC2 crops yield rate")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float ic2CropsYield = 4F;

        @Config.Comment("GT ores drop rate")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float gtOresDrops = 4F;

        @Config.Comment("GT coal \"ore\" drop rate")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float gtCoalOreDrops = 16F;

        @Config.Comment("GT Underground Fluids pump rate, affects only output")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float gtPumpOutput = 4F;

        @Config.Comment("GT machines (and Railcraft Coke and Steam Ovens) recipes required energy/time discount rate")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float gtRecipesEnergyDiscount = 4F;

        @Config.Comment("GT tools crafting durability rate")
        @Config.RangeFloat(min = 0.1F, max = 100F)
        public static float gtToolsCraftingDurability = 8F;

        @Config.Comment("Bees yield rate")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float beesYield = 4F;

        @Config.Comment("GT single block miners energy discount, affects energy consumption only")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float gtSimpleMinersEnergyDiscount = 1F;

        @Config.Comment("IC2 Tree Tap Resin extraction rate")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float ic2RubberTreeResinYield = 4F;

        @Config.Comment("The list of 'recipe category:value' pairs for more control. Pairs with invalid or outside of [0.1;64] range values are ignored. Exact categories strings are checked first, then substring after the last '.'. Set the first entry to 'print' to print all categories to the log (after loading to the main menu).")
        @Config.DefaultStringList({ "!print", "primitiveblastfurnace:228", "gt.recipe.lathe:0" })
        public static String[] gtRecipesPerCategoryEnergyDiscount;

        @Config.Comment("IC2 Rubber Tree saplings drop chance (1/35 by default) multiplier")
        @Config.RangeFloat(min = 0.1F, max = 64F)
        public static float ic2RubberTreeSaplingsDropChanceMultiplier = 2F;
    }

    protected static void registerConfigClasses() {
        try {
            for (Class<?> c : configClasses) {
                ConfigurationManager.registerConfig(c);
            }
        } catch (ConfigException e) {
            throw new RuntimeException(e);
        }
    }

    public class GuiConfig extends SimpleGuiConfig {

        public GuiConfig(GuiScreen parent) throws ConfigException {
            super(parent, "gtnhrates", "GTNH Rates", configClasses);
        }
    }

    public class GUIFactory implements SimpleGuiFactory {

        @Override
        public Class<? extends GuiScreen> mainConfigGuiClass() {
            return GuiConfig.class;
        }
    }
}
