package dev.itsmeow.quickteleports.forge;

import dev.itsmeow.quickteleports.QuickTeleportsModForge;

public class QuickTeleportsModImpl {
    public static int getTeleportTimeout() {
        return QuickTeleportsModForge.SERVER_CONFIG.teleportRequestTimeout.get();
    }
}
