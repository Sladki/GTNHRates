package com.github.sladki.gtnhrates;

import net.minecraft.client.gui.GuiScreen;

import com.gtnewhorizon.gtnhlib.config.Config;
import com.gtnewhorizon.gtnhlib.config.ConfigException;
import com.gtnewhorizon.gtnhlib.config.ConfigurationManager;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiConfig;
import com.gtnewhorizon.gtnhlib.config.SimpleGuiFactory;

public class ModConfig {

    protected static Class<?>[] configClasses = { Rates.class };

    @Config(modid = "gtnhrates")
    static public class Rates {

        @Config.Comment("Automatically open Item Holder covers inventories all at once, and disable shift clicking")
        @Config.DefaultBoolean(true)
        public static boolean gtItemHolderCoverOpenAuto = true;

        @Config.Comment("GT Hammer ore prospecting overhaul: smaller radius (adjustable), but scans all blocks in the radius")
        @Config.DefaultBoolean(true)
        public static boolean gtHammerOreProspectingOverhaul = true;

        @Config.Comment("GT Hammer ore prospecting radius")
        @Config.RangeInt(min = 1, max = 228)
        public static int gtHammerOreProspectingRadius = 2;

        @Config.Comment("Vanilla (and derivatives) and Natura crops growth overhaul. Makes crops growth dependent on time and less random")
        @Config.DefaultBoolean(true)
        public static boolean cropsGrowthOverhaul = true;

        @Config.Comment("Growth overhaul minimum time to mature (in seconds)")
        @Config.RangeInt(min = 1)
        public static int cropsTimeToMature = 5 * 60;

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

    static public class GuiConfig extends SimpleGuiConfig {

        public GuiConfig(GuiScreen parent) throws ConfigException {
            super(parent, "gtnhrates", "GTNH Rates", configClasses);
        }
    }

    public static class GUIFactory implements SimpleGuiFactory {

        @Override
        public Class<? extends GuiScreen> mainConfigGuiClass() {
            return GuiConfig.class;
        }
    }
}
