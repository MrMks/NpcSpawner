package moe.gensoukyo.npcspawner;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * @author SQwatermark
 */
@Mod(
        modid = ModMain.MOD_ID,
        name = ModMain.MOD_NAME,
        version = ModMain.VERSION,
        acceptableRemoteVersions = "*",
        dependencies = "required-after:customnpcs;")
public class ModMain {

    public static final String MOD_ID = "npcspawner";
    public static final String MOD_NAME = "NpcSpawner";
    public static final String VERSION = "1.1.0a";

    public static Logger logger;

    public static File modConfigDi;

    @Deprecated
    public static boolean pauseSpawn = false;
    @Deprecated
    public static boolean debugSpawn = false;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger = event.getModLog();
        modConfigDi = new File(event.getModConfigurationDirectory(), MOD_ID);
        //NpcSpawner.config = NpcSpawnerConfig.instance();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        PermissionAPI.registerNode("npcspawner.noMobSpawn", DefaultPermissionLevel.NONE, "Mobs will not spawn around those who has this permission");
    }

    @Mod.EventHandler
    public void posrInit(FMLPostInitializationEvent event) {
        Registers.init();
    }

    @Mod.EventHandler
    public void serverLoad(FMLServerStartingEvent event) {
        SpawnerStatus status = new SpawnerStatus();
        SpawnerConfig config = new SpawnerConfig().init(modConfigDi);
        event.registerServerCommand(new CommandNpcSpawner(config, status.newWritePort()));
        MinecraftForge.EVENT_BUS.register(new SpawnLogicHandler(config, status.newReadPort()));
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event) {
    }

}
