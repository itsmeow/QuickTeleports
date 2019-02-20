package its_meow.quickteleports;

import java.util.HashMap;
import java.util.HashSet;

import org.apache.commons.lang3.tuple.Pair;

import its_meow.quickteleports.command.CommandTpAccept;
import its_meow.quickteleports.command.CommandTpa;
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
import static net.minecraft.util.text.TextFormatting.*;

@Mod(modid = Ref.MOD_ID, name = Ref.NAME, version = Ref.VERSION, acceptedMinecraftVersions = Ref.acceptedMCV, acceptableRemoteVersions = "*")
@Mod.EventBusSubscriber(modid = Ref.MOD_ID)
public class QuickTeleportsMod {

	@Instance(Ref.MOD_ID) 
	public static QuickTeleportsMod mod;
	
	// Left: TPer
	// Right: Target
	public static HashMap<Pair<String, String>, Integer> tps = new HashMap<Pair<String, String>, Integer>(); 
	
	@EventHandler
	public void onServerStarting(FMLServerStartingEvent event) {
		event.registerServerCommand(new CommandTpAccept());
		event.registerServerCommand(new CommandTpa());
	}
	
	@SubscribeEvent
	public static void onTick(ServerTickEvent event) {
		HashSet<Pair<String, String>> toRemove = new HashSet<Pair<String, String>>();
		for(Pair<String, String> names : tps.keySet()) {
			int time = tps.get(names);
			//System.out.println(names.getRight() + " - " + time);
			if(time > 0) {
				time--;
				tps.put(names, time);
			} else if(time <= 0) {
				toRemove.add(names);
				MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
				EntityPlayerMP tper = server.getPlayerList().getPlayerByUsername(names.getLeft());
				EntityPlayerMP target = server.getPlayerList().getPlayerByUsername(names.getRight());
				if(target != null) {
					target.sendMessage(new TextComponentString(GOLD + "Teleport request from " + GREEN + names.getLeft() + GOLD + " timed out."));
				}
				if(tper != null) {
					tper.sendMessage(new TextComponentString(GOLD + "Your teleport to " + GREEN + names.getRight() + GOLD + " has timed out after not being accepted."));
				}
			}
		}
		
		for(Pair<String, String> remove : toRemove) {
			tps.remove(remove);
		}
	}
	
}
