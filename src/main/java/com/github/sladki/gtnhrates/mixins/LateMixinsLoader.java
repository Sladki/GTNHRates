package com.github.sladki.gtnhrates.mixins;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.github.sladki.gtnhrates.mixins.late.NEIBookmarksContents;
import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;

@LateMixin
public class LateMixinsLoader implements ILateMixinLoader {

    @Override
    public String getMixinConfig() {
        return "mixins.gtnhrates.late.json";
    }

    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        List<String> mixinsToLoad = new ArrayList<>(Arrays.asList(
                "TileEntityOresAccessor",
                "NaturaCrops",
                "IC2Crops",
                "GTOres",
                "GTOres$Bartworks",
                "GTOilDrill",
                "GTMiner",
                // "GTRecipes", // Look at EventsHandler
                "GTFurnaces",
                "GTFurnaces$BronzeFurnace",
                "GTFurnaces$SteelFurnace",
                "RailcraftCokeOvenRecipes",
                "RailcraftSteamOven",
                "GTMetaTools",
                "ForestryBees",
                "IC2TreeTap",
                "GTHammerProspecting",
                "GTItemHolderCover"
        ));
        mixinsToLoad.addAll(NEIBookmarksContents.mixins());
        if (loadedMods.contains("HungerOverhaul")) {
            mixinsToLoad.add("HungerOverhaulCrops");
        }
        return mixinsToLoad;
    }
}
