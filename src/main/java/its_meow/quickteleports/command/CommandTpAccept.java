package its_meow.quickteleports.command;

import static net.minecraft.util.text.TextFormatting.GREEN;
import static net.minecraft.util.text.TextFormatting.RED;

import its_meow.quickteleports.BasicTeleporter;
import its_meow.quickteleports.QuickTeleportsMod;
import its_meow.quickteleports.util.Teleport;
import its_meow.quickteleports.util.ToTeleport;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

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

		Teleport tp = QuickTeleportsMod.getSubjectTP(sender.getName());

		if(tp == null) {
			throw new CommandException(RED + "You have no pending teleport requests!");
		}

		QuickTeleportsMod.tps.remove(tp);
		EntityPlayerMP playerRequesting = server.getPlayerList().getPlayerByUsername(tp.getRequester());
		EntityPlayerMP playerMoving = server.getPlayerList().getPlayerByUsername(tp.getSubject());

		if(playerMoving == null) {
			throw new CommandException(RED + "The player that is teleporting no longer exists!");
		}
		
		if(tp instanceof ToTeleport) {
			EntityPlayerMP holder = playerMoving;
			playerMoving = playerRequesting;
			playerRequesting = holder;
		}

		playerRequesting.sendMessage(new TextComponentString(GREEN + "Teleport request accepted."));
		playerMoving.sendMessage(new TextComponentString(GREEN + (tp instanceof ToTeleport ? "Your teleport request has been accepted." : "You are now being teleported.")));

		int destID = playerRequesting.getEntityWorld().provider.getDimension();
		moveIfDifferentID(server, playerMoving, destID);

		double posX = playerRequesting.posX;
		double posY = playerRequesting.posY;
		double posZ = playerRequesting.posZ;
		float yaw = playerRequesting.rotationYaw;

		playerMoving.setLocationAndAngles(posX, posY, posZ, yaw, 0.0F);
		playerMoving.setPositionAndUpdate(posX, posY, posZ);
		playerMoving.setRotationYawHead(yaw);
		playerMoving.motionX = 0.0D;
		playerMoving.motionY = 0.0D;
		playerMoving.motionZ = 0.0D;
	}
	
	private static void moveIfDifferentID(MinecraftServer server, EntityPlayerMP moved, int destination) {
		int current = moved.getEntityWorld().provider.getDimension();
		if(destination != current) {
			server.getPlayerList().transferPlayerToDimension(moved, destination, new BasicTeleporter(server.getWorld(destination)));
		}
	}

}
