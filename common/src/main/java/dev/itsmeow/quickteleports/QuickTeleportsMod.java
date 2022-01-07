package dev.itsmeow.quickteleports;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dev.architectury.injectables.annotations.ExpectPlatform;
import dev.itsmeow.quickteleports.util.HereTeleport;
import dev.itsmeow.quickteleports.util.Teleport;
import dev.itsmeow.quickteleports.util.ToTeleport;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.GameProfileArgument;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Predicate;

public class QuickTeleportsMod {

    public static final String MOD_ID = "quickteleports";
    public static final String CONFIG_FIELD_NAME = "teleport_request_timeout";
    public static final String CONFIG_FIELD_COMMENT = "Timeout until a teleport request expires, in seconds.";
    public static final int CONFIG_FIELD_VALUE = 30;
    public static final int CONFIG_FIELD_MIN = 0;
    public static final int CONFIG_FIELD_MAX = Integer.MAX_VALUE;

    public static HashMap<Teleport, Integer> tps = new HashMap<>();

    public static class FTC extends TextComponent {

        public FTC(ChatFormatting color, String msg) {
            super(msg);
            this.setStyle(Style.EMPTY.withColor(color));
        }

    }

    public static void registerCommands(CommandDispatcher dispatcher) {
        Predicate<CommandSourceStack> isPlayer = source -> {
            try {
                return source.getPlayerOrException() != null;
            } catch(CommandSyntaxException e) {
                return false;
            }
        };
        // tpa
        dispatcher.register(Commands.literal("tpa").requires(isPlayer).then(Commands.argument("target", GameProfileArgument.gameProfile()).executes(command -> {
            ServerPlayer player = command.getSource().getPlayerOrException();
            MinecraftServer server = player.getServer();
            Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(command, "target");
            if(profiles.size() > 1) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "Specify one player as an argument!"));
                return 0;
            }
            GameProfile profile = getFirstProfile(profiles);
            if(!isGameProfileOnline(server, profile)) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "This player is not online!"));
                return 0;
            }
            if(profile.getId().equals(player.getGameProfile().getId())) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "You cannot teleport to yourself!"));
                return 0;
            }
            String sourceName = player.getName().getString();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayer(profile.getId());
            Teleport remove = QuickTeleportsMod.getRequestTP(sourceName);
            if(remove != null) {
                QuickTeleportsMod.tps.remove(remove);
                QuickTeleportsMod.notifyCanceledTP(server, remove);
            }

            ToTeleport teleport = new ToTeleport(sourceName, targetPlayer.getName().getString());
            QuickTeleportsMod.tps.put(teleport, getTeleportTimeout() * 20);
            sendMessage(targetPlayer.createCommandSourceStack(), true, new FTC(ChatFormatting.GREEN, sourceName), new FTC(ChatFormatting.GOLD, " has requested to teleport to you. Type "), new FTC(ChatFormatting.YELLOW, "/tpaccept"), new FTC(ChatFormatting.GOLD, " to accept."));
            sendMessage(command.getSource(), true, new FTC(ChatFormatting.GOLD, "Requested to teleport to "), new FTC(ChatFormatting.GREEN, targetPlayer.getName().getString()), new FTC(ChatFormatting.GOLD, "."));
            return 1;
        })));

        // tpaccept
        dispatcher.register(Commands.literal("tpaccept").requires(isPlayer).executes(command -> {
            ServerPlayer player = command.getSource().getPlayerOrException();
            MinecraftServer server = player.getServer();
            Teleport tp = QuickTeleportsMod.getSubjectTP(player.getName().getString());

            if(tp == null) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "You have no pending teleport requests!"));
                return 0;
            }

            QuickTeleportsMod.tps.remove(tp);
            ServerPlayer playerRequesting = server.getPlayerList().getPlayerByName(tp.getRequester());
            ServerPlayer playerMoving = server.getPlayerList().getPlayerByName(tp.getSubject());

            if(playerMoving == null) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "The player that is teleporting no longer exists!"));
                return 0;
            }

            if(tp instanceof ToTeleport) {
                ServerPlayer holder = playerMoving;
                playerMoving = playerRequesting;
                playerRequesting = holder;
            }

            sendMessage(playerRequesting.createCommandSourceStack(), true, new FTC(ChatFormatting.GREEN, "Teleport request accepted."));
            sendMessage(playerMoving.createCommandSourceStack(), true, new FTC(ChatFormatting.GREEN, (tp instanceof ToTeleport ? "Your teleport request has been accepted." : "You are now being teleported.")));

            double posX = playerRequesting.getX();
            double posY = playerRequesting.getY();
            double posZ = playerRequesting.getZ();
            playerMoving.teleportTo(playerRequesting.getLevel(), posX, posY, posZ, playerRequesting.yRot, 0F);
            return 1;
        }));

        // tpahere
        dispatcher.register(Commands.literal("tpahere").requires(isPlayer).then(Commands.argument("target", GameProfileArgument.gameProfile()).executes(command -> {
            ServerPlayer player = command.getSource().getPlayerOrException();
            MinecraftServer server = player.getServer();
            Collection<GameProfile> profiles = GameProfileArgument.getGameProfiles(command, "target");
            if(profiles.size() > 1) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "Specify one player as an argument!"));
                return 0;
            }
            GameProfile profile = getFirstProfile(profiles);
            if(!isGameProfileOnline(server, profile)) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "This player is not online!"));
                return 0;
            }
            if(profile.getId().equals(player.getGameProfile().getId())) {
                sendMessage(command.getSource(), false, new FTC(ChatFormatting.RED, "You cannot send a teleport request to yourself!"));
                return 0;
            }
            String sourceName = player.getName().getString();
            Teleport remove = QuickTeleportsMod.getRequestTP(sourceName);
            if(remove != null) {
                QuickTeleportsMod.tps.remove(remove);
                QuickTeleportsMod.notifyCanceledTP(server, remove);
            }
            ServerPlayer targetPlayer = server.getPlayerList().getPlayer(profile.getId());

            HereTeleport tp = new HereTeleport(sourceName, targetPlayer.getName().getString());
            QuickTeleportsMod.tps.put(tp, getTeleportTimeout() * 20);
            sendMessage(targetPlayer.createCommandSourceStack(), true, new FTC(ChatFormatting.GREEN, sourceName), new FTC(ChatFormatting.GOLD, " has requested that you teleport to them. Type "), new FTC(ChatFormatting.YELLOW, "/tpaccept"), new FTC(ChatFormatting.GOLD, " to accept."));
            sendMessage(command.getSource(), true, new FTC(ChatFormatting.GOLD, "Requested "), new FTC(ChatFormatting.GREEN, targetPlayer.getName().getString()), new FTC(ChatFormatting.GOLD, " to teleport to you."));

            return 1;
        })));
    }

    @ExpectPlatform
    public static int getTeleportTimeout() {
        throw new RuntimeException();
    }

    private static boolean isGameProfileOnline(MinecraftServer server, GameProfile profile) {
        ServerPlayer player = server.getPlayerList().getPlayer(profile.getId());
        if(player != null) {
            if(server.getPlayerList().getPlayers().contains(player)) {
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

    public static void serverTick(MinecraftServer server) {
        HashSet<Teleport> toRemove = new HashSet<>();
        for(Teleport tp : tps.keySet()) {
            int time = tps.get(tp);
            if(time > 0) {
                time--;
                tps.put(tp, time);
            } else if(time <= 0) {
                toRemove.add(tp);
                notifyTimeoutTP(server, tp);
            }
        }

        for(Teleport remove : toRemove) {
            tps.remove(remove);
        }
    }

    public static void notifyTimeoutTP(MinecraftServer server, Teleport tp) {
        ServerPlayer tper = server.getPlayerList().getPlayerByName(tp.getRequester());
        ServerPlayer target = server.getPlayerList().getPlayerByName(tp.getSubject());
        if(target != null) {
            sendMessage(target.createCommandSourceStack(), true, new FTC(ChatFormatting.GOLD, "Teleport request from "), new FTC(ChatFormatting.GREEN, tp.getRequester()), new FTC(ChatFormatting.GOLD, " timed out."));
        }
        if(tper != null) {
            sendMessage(tper.createCommandSourceStack(), true, new FTC(ChatFormatting.GOLD, "Your request to "), new FTC(ChatFormatting.GREEN, tp.getSubject()), new FTC(ChatFormatting.GOLD, " has timed out after not being accepted."));
        }
    }

    public static void notifyCanceledTP(MinecraftServer server, Teleport tp) {
        ServerPlayer tper = server.getPlayerList().getPlayerByName(tp.getRequester());
        ServerPlayer target = server.getPlayerList().getPlayerByName(tp.getSubject());
        if(target != null) {
            sendMessage(target.createCommandSourceStack(), true, new FTC(ChatFormatting.GOLD, "Teleport request from "), new FTC(ChatFormatting.GREEN, tp.getRequester()), new FTC(ChatFormatting.GOLD, " has been cancelled."));
        }
        if(tper != null) {
            sendMessage(tper.createCommandSourceStack(), true, new FTC(ChatFormatting.GOLD, "Your request to "), new FTC(ChatFormatting.GREEN, tp.getSubject()), new FTC(ChatFormatting.GOLD, " has been cancelled."));
        }
    }

    public static void sendMessage(CommandSourceStack source, boolean success, TextComponent... styled) {
        if(styled.length > 0) {
            TextComponent comp = styled[0];
            if(styled.length > 1) {
                for(int i = 1; i < styled.length; i++) {
                    comp.append(styled[i]);
                }
            }
            if(success) {
                source.sendSuccess(comp, false);
            } else {
                source.sendFailure(comp);
            }
        }
    }

}
