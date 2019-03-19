package its_meow.quickteleports;

import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GREEN;

import java.util.HashMap;
import java.util.HashSet;

import javax.annotation.Nullable;

import its_meow.quickteleports.command.CommandTpAccept;
import its_meow.quickteleports.command.CommandTpHere;
import its_meow.quickteleports.command.CommandTpa;
import its_meow.quickteleports.util.Teleport;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ServerTickEvent;

@Mod(modid = Ref.MOD_ID, name = Ref.NAME, version = Ref.VERSION, acceptedMinecraftVersions = Ref.acceptedMCV, acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public class QuickTeleportsMod {

	@Instance(Ref.MOD_ID) 
	public static QuickTeleportsMod mod;

	public static HashMap<Teleport, Integer> tps = new HashMap<Teleport, Integer>(); 

	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTpAccept());
		event.registerServerCommand(new CommandTpa());
		event.registerServerCommand(new CommandTpHere());
	}

	@Nullable
	public static Teleport getSubjectTP(String name) {
		for(Teleport pair : QuickTeleportsMod.tps.keySet()) {
			if(pair.getSubject().equals(name)) {
				return pair;
			}
		}
		return null;
	}
	
	@Nullable
	public static Teleport getRequestTP(String name) {
		for(Teleport pair : QuickTeleportsMod.tps.keySet()) {
			if(pair.getRequester().equals(name)) {
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
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		EntityPlayerMP tper = server.getPlayerList().getPlayerByUsername(tp.getRequester());
		EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(tp.getSubject());
		if(target != null) {
			target.sendMessage(new TextComponentString(GOLD + "Teleport request from " + GREEN + tp.getRequester() + GOLD + " timed out."));
		}
		if(tper != null) {
			tper.sendMessage(new TextComponentString(GOLD + "Your request to " + GREEN + tp.getSubject() + GOLD + " has timed out after not being accepted."));
		}
	}
	
	public static void notifyCanceledTP(Teleport tp) {
		MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
		EntityPlayerMP tper = server.getPlayerList().getPlayerByUsername(tp.getRequester());
		EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(tp.getSubject());
		if(target != null) {
			target.sendMessage(new TextComponentString(GOLD + "Teleport request from " + GREEN + tp.getRequester() + GOLD + " has been cancelled."));
		}
		if(tper != null) {
			tper.sendMessage(new TextComponentString(GOLD + "Your request to " + GREEN + tp.getSubject() + GOLD + " has been cancelled."));
		}
	}

}
