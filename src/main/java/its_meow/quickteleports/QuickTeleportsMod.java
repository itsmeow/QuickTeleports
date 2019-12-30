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
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerAbilitiesPacket;
import net.minecraft.network.play.server.SRespawnPacket;
import net.minecraft.network.play.server.SServerDifficultyPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.WorldInfo;
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
        CommandDispatcher<CommandSource> d = event.getCommandDispatcher();
        
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
            
            int dim = playerRequesting.getEntityWorld().getDimension().getType().getId();
            double posX = playerRequesting.func_226277_ct_();
            double posY = playerRequesting.func_226278_cu_();
            double posZ = playerRequesting.func_226281_cx_();
            if(dim != playerMoving.getServerWorld().getDimension().getType().getId()){
                teleport(playerMoving, DimensionType.getById(dim));
            }
            playerMoving.setLocationAndAngles(posX, posY, posZ, playerRequesting.rotationYaw, 0);
            playerMoving.setPositionAndUpdate(posX, posY, posZ);
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
        @SuppressWarnings("resource")
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
        @SuppressWarnings("resource")
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
    
    public static void sendMessage(CommandSource source, ITextComponent... styled) throws CommandSyntaxException {
        sendMessage(source.asPlayer(), styled);
    }
    
    public static void sendMessage(PlayerEntity source, ITextComponent... styled) {
        if(styled.length > 0) {
            ITextComponent comp = styled[0];
            if(styled.length > 1) {
                for(int i = 1; i < styled.length; i++) {
                    comp.appendSibling(styled[i]);
                }
            }
            source.sendMessage(comp);
        }
    }
    
    @SuppressWarnings("resource")
    public static Entity teleport(Entity entityIn, DimensionType dimensionTo) {
        if(!net.minecraftforge.common.ForgeHooks.onTravelToDimension(entityIn, dimensionTo)) {
            return null;
        }
        if(!entityIn.getEntityWorld().isRemote && entityIn.isAlive()) {
            final ServerWorld worldFrom = entityIn.getServer().getWorld(entityIn.dimension);
            final ServerWorld worldTo = entityIn.getServer().getWorld(dimensionTo);
            entityIn.dimension = dimensionTo;

            if(entityIn instanceof ServerPlayerEntity) {
                final ServerPlayerEntity entityPlayer = (ServerPlayerEntity) entityIn;
                // Access Transformer exposes this field
                entityPlayer.invulnerableDimensionChange = true;
                // End Access Transformer
                WorldInfo worldinfo = entityPlayer.world.getWorldInfo();
                entityPlayer.connection.sendPacket(new SRespawnPacket(dimensionTo, WorldInfo.func_227498_c_(worldinfo.getSeed()), worldinfo.getGenerator(),
                entityPlayer.interactionManager.getGameType()));
                entityPlayer.connection.sendPacket(
                new SServerDifficultyPacket(worldinfo.getDifficulty(), worldinfo.isDifficultyLocked()));
                PlayerList playerlist = entityPlayer.world.getServer().getPlayerList();
                playerlist.updatePermissionLevel(entityPlayer);
                worldFrom.removeEntity(entityPlayer, true); // Forge: the player entity is moved to the new world, NOT cloned. So keep the data alive with no matching invalidate call.
                entityPlayer.revive();
                entityPlayer.setWorld(worldTo);
                worldTo.func_217447_b(entityPlayer);
                // entityPlayer.func_213846_b(worldFrom);
                entityPlayer.interactionManager.setWorld(worldTo);
                entityPlayer.connection.sendPacket(new SPlayerAbilitiesPacket(entityPlayer.abilities));
                playerlist.sendWorldInfo(entityPlayer, worldTo);
                playerlist.sendInventory(entityPlayer);

                net.minecraftforge.fml.hooks.BasicEventHooks.firePlayerChangedDimensionEvent(entityPlayer, entityPlayer.dimension, dimensionTo);
                // entityPlayer.clearInvulnerableDimensionChange();
                return entityPlayer;
            }

            entityIn.detach();
            Entity copy = entityIn.getType().create(worldTo);
            if(copy != null) {
                copy.copyDataFromOld(entityIn);
                copy.setMotion(entityIn.getMotion().mul(Vec3d.fromPitchYaw(entityIn.rotationPitch, entityIn.rotationYaw).normalize()));
                // used to unnaturally add entities to world
                worldTo.func_217460_e(copy);
            }
            // update world
            worldFrom.resetUpdateEntityTick();
            worldTo.resetUpdateEntityTick();
            // remove old entity
            entityIn.remove(false);
            return copy;
        }
        return null;
    }

}