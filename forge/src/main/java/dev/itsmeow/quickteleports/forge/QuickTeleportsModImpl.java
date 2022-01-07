package dev.itsmeow.quickteleports.forge;

public class QuickTeleportsModImpl {
    public static int getTeleportTimeout() {
        return QuickTeleportsModForge.SERVER_CONFIG.teleportRequestTimeout.get();
    }
}
