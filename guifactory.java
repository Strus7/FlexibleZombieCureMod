package com.example.flexiblezombiecure;

import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.lang.reflect.Field;

@Mod(
    modid = FlexibleZombieCure.MODID, 
    name = FlexibleZombieCure.NAME, 
    version = FlexibleZombieCure.VERSION,
    guiFactory = "com.example.flexiblezombiecure.GuiFactory"
)
public class FlexibleZombieCure {
    public static final String MODID = "flexiblezombiecure";
    public static final String NAME = "Flexible Zombie Cure";
    public static final String VERSION = "1.0";

    private static Logger logger;
    
    private static int cureTimeMultiplier = 1;
    private static boolean instantCure = true;
    
    public static Configuration config;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        
        File configFile = new File(event.getModConfigurationDirectory(), MODID + ".cfg");
        config = new Configuration(configFile);
        loadConfig();
        
        MinecraftForge.EVENT_BUS.register(this);
        
        logger.info("FlexibleZombieCure preinitialized");
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("FlexibleZombieCure initialized with cure time multiplier: " + cureTimeMultiplier);
    }
    
    private static void loadConfig() {
        config.load();
        
        cureTimeMultiplier = config.getInt("Mnożnik czasu leczenia", Configuration.CATEGORY_GENERAL, 
                                         1, 1, 100, 
                                         "");
        
        instantCure = config.getBoolean("Natychmiastowe leczenie", Configuration.CATEGORY_GENERAL, 
                                    true,
                                    "");
        
        if (config.hasChanged()) {
            config.save();
        }
    }
    
    public static void reloadConfig() {
        loadConfig();
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntity() instanceof EntityZombieVillager) {
            EntityZombieVillager zombieVillager = (EntityZombieVillager) event.getEntity();
            
            if (instantCure) {
                try {
                    Field conversionTimeField = null;
                    try {
                        conversionTimeField = EntityZombieVillager.class.getDeclaredField("conversionTime");
                    } catch (NoSuchFieldException e) {
                        try {
                            conversionTimeField = EntityZombieVillager.class.getDeclaredField("field_191086_cq");
                        } catch (NoSuchFieldException e2) {
                            for (Field field : EntityZombieVillager.class.getDeclaredFields()) {
                                field.setAccessible(true);
                                if (field.getType() == int.class) {
                                    conversionTimeField = field;
                                    int originalValue = field.getInt(zombieVillager);
                                    if (originalValue > 0) {
                                        logger.info("Found potential conversion time field: " + field.getName());
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (conversionTimeField != null) {
                        conversionTimeField.setAccessible(true);
                        int conversionTime = conversionTimeField.getInt(zombieVillager);
                        
                        if (conversionTime > 0) {
                            conversionTimeField.setInt(zombieVillager, 1);
                            logger.info("Accelerating zombie villager cure to instant: " + zombieVillager);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error manipulating conversion time:", e);
                }
            } 
            else if (cureTimeMultiplier < 100) {
                try {
                    Field conversionTimeField = null;
                    try {
                        conversionTimeField = EntityZombieVillager.class.getDeclaredField("conversionTime");
                    } catch (NoSuchFieldException e) {
                        try {
                            conversionTimeField = EntityZombieVillager.class.getDeclaredField("field_191086_cq");
                        } catch (NoSuchFieldException e2) {
                            for (Field field : EntityZombieVillager.class.getDeclaredFields()) {
                                field.setAccessible(true);
                                if (field.getType() == int.class) {
                                    int value = field.getInt(zombieVillager);
                                    if (value > 0) {
                                        conversionTimeField = field;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    
                    if (conversionTimeField != null) {
                        conversionTimeField.setAccessible(true);
                        int conversionTime = conversionTimeField.getInt(zombieVillager);
                        
                        if (conversionTime > 0) {
                            int newTime = conversionTime - (100 - cureTimeMultiplier);
                            if (newTime < 1) newTime = 1;
                            
                            conversionTimeField.setInt(zombieVillager, newTime);
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error accessing conversionTime field:", e);
                }
            }
        }
    }
}