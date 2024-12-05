package com.github.sladki.gtnhrates;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(
    modid = GTNHRates.MODID,
    version = Tags.VERSION,
    name = "GTNH Rates",
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:gtnhlib",
    guiFactory = "com.github.sladki.gtnhrates.ModConfig")
public class GTNHRates {

    public static final String MODID = "gtnhrates";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        ModConfig.registerConfigClasses();
        FMLCommonHandler.instance()
            .bus()
            .register(new EventsHandler());
    }

}
