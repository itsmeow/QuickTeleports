package its_meow.quickteleports.command;

import org.apache.commons.lang3.tuple.Pair;

import its_meow.quickteleports.BasicTeleporter;
import its_meow.quickteleports.QuickTeleportsMod;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import static net.minecraft.util.text.TextFormatting.*;

public class CommandTpAccept extends CommandBase {

	@Override
	public String getName() {
		return "tpaccept";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/tpaccept";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length > 0) {
			throw new WrongUsageException(this.getUsage(sender));
		}

		Pair<String, String> pair = null;
		for(Pair<String, String> pairI : QuickTeleportsMod.tps.keySet()) {
			if(pairI.getRight().equalsIgnoreCase(sender.getName())) {
				pair = pairI;
			}
		}
		
		if(pair == null) {
			throw new CommandException(RED + "You have no pending teleport requests!");
		}
		
		String tper = pair.getLeft();
		QuickTeleportsMod.tps.remove(pair);
		EntityPlayer playerTPDTO = (EntityPlayer) sender;
		EntityPlayerMP playerTPER = server.getPlayerList().getPlayerByUsername(tper);
		
		if(playerTPER == null) {
			throw new CommandException(RED + "The player that requested teleport no longer exists!");
		}
		
		sender.sendMessage(new TextComponentString(GREEN + "Teleport request accepted."));
		playerTPER.sendMessage(new TextComponentString(GREEN + "Your teleport request has been accepted."));
		
		int destID = playerTPDTO.getEntityWorld().provider.getDimension();
		int oldID = playerTPER.getEntityWorld().provider.getDimension();
		if(destID != oldID) {
			server.getPlayerList().transferPlayerToDimension(playerTPER, destID, new BasicTeleporter(server.getWorld(destID)));
		}
		
		double posX = playerTPDTO.posX;
		double posY = playerTPDTO.posY;
		double posZ = playerTPDTO.posZ;
		float yaw = playerTPDTO.rotationYaw;
		
		playerTPER.setLocationAndAngles(posX, posY, posZ, yaw, 0.0F);
		playerTPER.setPositionAndUpdate(posX, posY, posZ);
		playerTPER.setRotationYawHead(yaw);
		playerTPER.motionX = 0.0D;
		playerTPER.motionY = 0.0D;
		playerTPER.motionZ = 0.0D;

	}

}
