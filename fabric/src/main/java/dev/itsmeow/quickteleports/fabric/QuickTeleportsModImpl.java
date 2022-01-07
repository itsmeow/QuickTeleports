package dev.itsmeow.quickteleports.fabric;

import dev.itsmeow.quickteleports.QuickTeleportsModFabric;

public class QuickTeleportsModImpl {
    public static int getTeleportTimeout() {
        return QuickTeleportsModFabric.teleportRequestTimeout.getValue();
    }
}
