package its_meow.quickteleports;

import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GREEN;
import static net.minecraft.util.text.TextFormatting.RED;
import static net.minecraft.util.text.TextFormatting.YELLOW;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import its_meow.quickteleports.util.FTC;
import its_meow.quickteleports.util.HereTeleport;
import its_meow.quickteleports.util.Teleport;
import its_meow.quickteleports.util.ToTeleport;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.GameProfileArgument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Util;
import net.minecraft.util.text.TextComponent;
import net.minecraftforge.event.TickEvent.ServerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

@Mod(QuickTeleportsMod.MOD_ID)
@Mod.EventBusSubscriber(modid = QuickTeleportsMod.MOD_ID)
public class QuickTeleportsMod {

    public static final String MOD_ID = "quickteleports";

    public static HashMap<Teleport, Integer> tps = new HashMap<Teleport, Integer>();

    public QuickTeleportsMod() {
        TpConfig.setupConfig();
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, TpConfig.SERVER_CONFIG);
    }

    @SubscribeEvent
    public static void onServerStarting(FMLServerStartingEvent event) {
        CommandDispatcher<CommandSource> d = event.getServer().getCommandManager().getDispatcher();

        // tpa
        d.register(Commands.literal("tpa").requires(source -> {
            try {
                return source.asPlayer() != null;
            } catch(CommandSyntaxException e) {
                return false;
            }
        }).then(Commands.argument("target", GameProfileArgument.gameProfile()).executes(command -> {
            Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(command, "target");
            if(profiles.size() > 1) {
                sendMessage(command.getSource(), new FTC(RED, "Specify one player as an argument!"));
                return 0;
            }
            GameProfile profile = getFirstProfile(profiles);
            if(!isGameProfileOnline(profile)) {
                sendMessage(command.getSource(), new FTC(RED, "This player is not online!"));
                return 0;
            }
            if(profile.getId().equals(command.getSource().asPlayer().getGameProfile().getId())) {
                sendMessage(command.getSource(), new FTC(RED, "You cannot teleport to yourself!"));
                return 0;
            }
            String sourceName = command.getSource().asPlayer().getName().getString();
            ServerPlayerEntity targetPlayer = event.getServer().getPlayerList().getPlayerByUUID(profile.getId());
            Teleport remove = QuickTeleportsMod.getRequestTP(sourceName);
            if(remove != null) {
                QuickTeleportsMod.tps.remove(remove);
                QuickTeleportsMod.notifyCanceledTP(remove);
            }

            ToTeleport teleport = new ToTeleport(sourceName, targetPlayer.getName().getString());
            QuickTeleportsMod.tps.put(teleport, TpConfig.CONFIG.timeout.get() * 20);
            sendMessage(targetPlayer, new FTC(GREEN, sourceName), new FTC(GOLD, " has requested to teleport to you. Type "), new FTC(YELLOW, "/tpaccept"), new FTC(GOLD, " to accept."));
            sendMessage(command.getSource(), new FTC(GOLD, "Requested to teleport to "), new FTC(GREEN, targetPlayer.getName().getString()), new FTC(GOLD, "."));
            return 1;
        })));

        // tpaccept
        d.register(Commands.literal("tpaccept").requires(source -> {
            try {
                return source.asPlayer() != null;
            } catch(CommandSyntaxException e) {
                return false;
            }
        }).executes(command -> {

            Teleport tp = QuickTeleportsMod.getSubjectTP(command.getSource().asPlayer().getName().getString());

            if(tp == null) {
                sendMessage(command.getSource(), new FTC(RED, "You have no pending teleport requests!"));
                return 0;
            }

            QuickTeleportsMod.tps.remove(tp);
            ServerPlayerEntity playerRequesting = event.getServer().getPlayerList().getPlayerByUsername(tp.getRequester());
            ServerPlayerEntity playerMoving = event.getServer().getPlayerList().getPlayerByUsername(tp.getSubject());

            if(playerMoving == null) {
                sendMessage(command.getSource(), new FTC(RED, "The player that is teleporting no longer exists!"));
                return 0;
            }

            if(tp instanceof ToTeleport) {
                ServerPlayerEntity holder = playerMoving;
                playerMoving = playerRequesting;
                playerRequesting = holder;
            }

            sendMessage(playerRequesting, new FTC(GREEN, "Teleport request accepted."));
            sendMessage(playerMoving, new FTC(GREEN, (tp instanceof ToTeleport ? "Your teleport request has been accepted." : "You are now being teleported.")));

            double posX = playerRequesting.getX();
            double posY = playerRequesting.getY();
            double posZ = playerRequesting.getZ();
            playerMoving.teleport(playerRequesting.getServerWorld(), posX, posY, posZ, playerRequesting.rotationYaw, 0F);
            return 1;
        }));

        // tpahere
        d.register(Commands.literal("tpahere").requires(source -> {
            try {
                return source.asPlayer() != null;
            } catch(CommandSyntaxException e) {
                return false;
            }
        }).then(Commands.argument("target", GameProfileArgument.gameProfile()).executes(command -> {
            Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(command, "target");
            if(profiles.size() > 1) {
                sendMessage(command.getSource(), new FTC(RED, "Specify one player as an argument!"));
                return 0;
            }
            GameProfile profile = getFirstProfile(profiles);
            if(!isGameProfileOnline(profile)) {
                sendMessage(command.getSource(), new FTC(RED, "This player is not online!"));
                return 0;
            }
            if(profile.getId().equals(command.getSource().asPlayer().getGameProfile().getId())) {
                sendMessage(command.getSource(), new FTC(RED, "You cannot send a teleport request to yourself!"));
                return 0;
            }
            String sourceName = command.getSource().asPlayer().getName().getString();
            Teleport remove = QuickTeleportsMod.getRequestTP(sourceName);
            if(remove != null) {
                QuickTeleportsMod.tps.remove(remove);
                QuickTeleportsMod.notifyCanceledTP(remove);
            }
            ServerPlayerEntity targetPlayer = event.getServer().getPlayerList().getPlayerByUUID(profile.getId());

            HereTeleport tp = new HereTeleport(sourceName, targetPlayer.getName().getString());
            QuickTeleportsMod.tps.put(tp, TpConfig.CONFIG.timeout.get() * 20);
            sendMessage(targetPlayer, new FTC(GREEN, sourceName), new FTC(GOLD, " has requested that you teleport to them. Type "), new FTC(YELLOW, "/tpaccept"), new FTC(GOLD, " to accept."));
            sendMessage(command.getSource(), new FTC(GOLD, "Requested "), new FTC(GREEN, targetPlayer.getName().getString()), new FTC(GOLD, " to teleport to you."));

            return 1;
        })));
    }

    private static boolean isGameProfileOnline(GameProfile profile) {
        ServerPlayerEntity player = ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayerByUUID(profile.getId());
        if(player != null) {
            if(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().contains(player)) {
                return true;
            }
        }
        return false;
    }

    private static GameProfile getFirstProfile(Collection<GameProfile> profiles) {
        for(GameProfile profile : profiles) {
            return profile;
        }
        return null;
    }

    @Nullable
    public static Teleport getSubjectTP(String name) {
        for(Teleport pair : QuickTeleportsMod.tps.keySet()) {
            if(pair.getSubject().equalsIgnoreCase(name)) {
                return pair;
            }
        }
        return null;
    }

    @Nullable
    public static Teleport getRequestTP(String name) {
        for(Teleport pair : QuickTeleportsMod.tps.keySet()) {
            if(pair.getRequester().equalsIgnoreCase(name)) {
                return pair;
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onTick(ServerTickEvent event) {
        HashSet<Teleport> toRemove = new HashSet<Teleport>();
        for(Teleport tp : tps.keySet()) {
            int time = tps.get(tp);
            if(time > 0) {
                time--;
                tps.put(tp, time);
            } else if(time <= 0) {
                toRemove.add(tp);
                notifyTimeoutTP(tp);
            }
        }

        for(Teleport remove : toRemove) {
            tps.remove(remove);
        }
    }

    public static void notifyTimeoutTP(Teleport tp) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity tper = server.getPlayerList().getPlayerByUsername(tp.getRequester());
        ServerPlayerEntity target = server.getPlayerList().getPlayerByUsername(tp.getSubject());
        if(target != null) {
            sendMessage(target, new FTC(GOLD, "Teleport request from "), new FTC(GREEN, tp.getRequester()), new FTC(GOLD, " timed out."));
        }
        if(tper != null) {
            sendMessage(tper, new FTC(GOLD, "Your request to "), new FTC(GREEN, tp.getSubject()), new FTC(GOLD, " has timed out after not being accepted."));
        }
    }

    public static void notifyCanceledTP(Teleport tp) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerPlayerEntity tper = server.getPlayerList().getPlayerByUsername(tp.getRequester());
        ServerPlayerEntity target = server.getPlayerList().getPlayerByUsername(tp.getSubject());
        if(target != null) {
            sendMessage(target, new FTC(GOLD, "Teleport request from "), new FTC(GREEN, tp.getRequester()), new FTC(GOLD, " has been cancelled."));
        }
        if(tper != null) {
            sendMessage(tper, new FTC(GOLD, "Your request to "), new FTC(GREEN, tp.getSubject()), new FTC(GOLD, " has been cancelled."));
        }
    }

    public static void sendMessage(CommandSource source, TextComponent... styled) throws CommandSyntaxException {
        sendMessage(source.asPlayer(), styled);
    }

    public static void sendMessage(PlayerEntity source, TextComponent... styled) {
        if(styled.length > 0) {
            TextComponent comp = styled[0];
            if(styled.length > 1) {
                for(int i = 1; i < styled.length; i++) {
                    comp.append(styled[i]);
                }
            }
            source.sendMessage(comp, Util.NIL_UUID);
        }
    }

}