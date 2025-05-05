package com.example.flexiblezombiecure;

import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = FlexibleZombieCure.MODID)
public class ConfigHandler {
    
    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if (event.getModID().equals(FlexibleZombieCure.MODID)) {
            if (FlexibleZombieCure.config.hasChanged()) {
                FlexibleZombieCure.config.save();
            }
            
            FlexibleZombieCure.reloadConfig();
        }
    }
}