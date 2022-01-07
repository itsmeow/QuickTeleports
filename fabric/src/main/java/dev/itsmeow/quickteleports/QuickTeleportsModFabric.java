package dev.itsmeow.quickteleports;

import io.github.fablabsmc.fablabs.api.fiber.v1.builder.ConfigTreeBuilder;
import io.github.fablabsmc.fablabs.api.fiber.v1.exception.ValueDeserializationException;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.ConfigTypes;
import io.github.fablabsmc.fablabs.api.fiber.v1.schema.type.derived.NumberConfigType;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.FiberSerialization;
import io.github.fablabsmc.fablabs.api.fiber.v1.serialization.JanksonValueSerializer;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigBranch;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.ConfigTree;
import io.github.fablabsmc.fablabs.api.fiber.v1.tree.PropertyMirror;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class QuickTeleportsModFabric implements ModInitializer {

    private static final NumberConfigType<Integer> TYPE = ConfigTypes.INTEGER.withValidRange(QuickTeleportsMod.CONFIG_FIELD_MIN, QuickTeleportsMod.CONFIG_FIELD_MAX, 1);
    public static final PropertyMirror<Integer> teleportRequestTimeout = PropertyMirror.create(TYPE);
    protected static final JanksonValueSerializer JANKSON_VALUE_SERIALIZER = new JanksonValueSerializer(false);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((commandDispatcher, dedicated) -> QuickTeleportsMod.registerCommands(commandDispatcher));
        ServerTickEvents.START_SERVER_TICK.register(QuickTeleportsMod::serverTick);
        ServerLifecycleEvents.SERVER_STARTING.register(state -> {
            ConfigTreeBuilder builder = ConfigTree.builder().withName(QuickTeleportsMod.MOD_ID).beginValue(QuickTeleportsMod.CONFIG_FIELD_NAME, TYPE, QuickTeleportsMod.CONFIG_FIELD_VALUE).withComment(QuickTeleportsMod.CONFIG_FIELD_COMMENT).finishValue(teleportRequestTimeout::mirror);
            ConfigBranch branch = builder.build();
            File configFile = new File(FabricLoader.getInstance().getConfigDir().toFile(), QuickTeleportsMod.MOD_ID + ".json5");
            boolean recreate = false;
            while (true) {
                try {
                    if (!configFile.exists() || recreate) {
                        FiberSerialization.serialize(branch, Files.newOutputStream(configFile.toPath()), JANKSON_VALUE_SERIALIZER);
                        break;
                    } else {
                        try {
                            FiberSerialization.deserialize(branch, Files.newInputStream(configFile.toPath()), JANKSON_VALUE_SERIALIZER);
                            FiberSerialization.serialize(branch, Files.newOutputStream(configFile.toPath()), JANKSON_VALUE_SERIALIZER);
                            break;
                        } catch (ValueDeserializationException e) {
                            String fileName = (QuickTeleportsMod.MOD_ID + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")) + ".json5");
                            configFile.renameTo(new File(configFile.getParent(), fileName));
                            recreate = true;
                            e.printStackTrace();
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        });
    }
}
