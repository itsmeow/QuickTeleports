package dev.itsmeow.quickteleports.forge;

import dev.itsmeow.quickteleports.QuickTeleportsMod;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fmllegacy.network.FMLNetworkConstants;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;
import org.apache.commons.lang3.tuple.Pair;

@Mod(QuickTeleportsMod.MOD_ID)
@Mod.EventBusSubscriber(modid = QuickTeleportsMod.MOD_ID)
public class QuickTeleportsModForge {

    public static ServerConfig SERVER_CONFIG = null;
    private static ForgeConfigSpec SERVER_CONFIG_SPEC = null;

    public QuickTeleportsModForge() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> FMLNetworkConstants.IGNORESERVERONLY, (s, b) -> true));
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::loadComplete);
    }

    private void loadComplete(final FMLLoadCompleteEvent event) {
        final Pair<ServerConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(ServerConfig::new);
        SERVER_CONFIG_SPEC = specPair.getRight();
        SERVER_CONFIG = specPair.getLeft();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, SERVER_CONFIG_SPEC);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        QuickTeleportsMod.serverTick(ServerLifecycleHooks.getCurrentServer());
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        QuickTeleportsMod.registerCommands(event.getDispatcher());
    }

    public static class ServerConfig {
        public ForgeConfigSpec.Builder builder;
        public final ForgeConfigSpec.IntValue teleportRequestTimeout;

        ServerConfig(ForgeConfigSpec.Builder builder) {
            this.builder = builder;
            this.teleportRequestTimeout = builder.comment(QuickTeleportsMod.CONFIG_FIELD_COMMENT + " Place a copy of this config in the defaultconfigs/ folder in the main server/.minecraft directory (or make the folder if it's not there) to copy this to new worlds.").defineInRange(QuickTeleportsMod.CONFIG_FIELD_NAME, QuickTeleportsMod.CONFIG_FIELD_VALUE, QuickTeleportsMod.CONFIG_FIELD_MIN, QuickTeleportsMod.CONFIG_FIELD_MAX);
            builder.build();
        }
    }

}
