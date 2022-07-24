package its_meow.quickteleports.command;

import static net.minecraft.util.text.TextFormatting.GOLD;
import static net.minecraft.util.text.TextFormatting.GREEN;
import static net.minecraft.util.text.TextFormatting.RED;
import static net.minecraft.util.text.TextFormatting.YELLOW;

import java.util.Arrays;
import java.util.List;

import its_meow.quickteleports.QuickTeleportsMod;
import its_meow.quickteleports.TpConfig;
import its_meow.quickteleports.util.Teleport;
import its_meow.quickteleports.util.ToTeleport;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

public class CommandTpa extends CommandBase {

	@Override
	public String getName() {
		return "tpa";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/tpa (username)";
	}

	@Override
	public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
		return sender instanceof EntityPlayer;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if(args.length != 1) {
			throw new CommandException(RED + "Incorrect argument count! Usage: " + this.getUsage(sender));
		}
		
		if(args[0].equalsIgnoreCase(sender.getName())) {
			throw new CommandException(RED + "You cannot teleport to yourself!");
		}
		
		EntityPlayerMP targetPlayer = server.getPlayerList().getPlayerByUsername(args[0]);
		if(targetPlayer == null) {
			throw new CommandException(GOLD + "Player " + GREEN + args[0] + GOLD + " is not on the server!");
		}
		
		Teleport remove = QuickTeleportsMod.getRequestTP(sender.getName());
		if(remove != null) {
			QuickTeleportsMod.tps.remove(remove);
			QuickTeleportsMod.notifyCanceledTP(remove);
		}
		
		ToTeleport teleport = new ToTeleport(sender.getName(), args[0]);
		QuickTeleportsMod.tps.put(teleport, TpConfig.tp_request_timeout * 20);
		targetPlayer.sendMessage(new TextComponentString(GREEN + sender.getName() + GOLD + " has requested to teleport to you. Type " + YELLOW + "/tpaccept" + GOLD +" to accept."));
		sender.sendMessage(new TextComponentString(GOLD + "Requested to teleport to " + GREEN + args[0] + GOLD + "."));
	}
	
	@Override
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args,
			BlockPos targetPos) {
		        if(args.length > 0) {
            return Arrays.stream(server.getOnlinePlayerNames()).filter(playerName -> playerName.startsWith(args[0])).collect(Collectors.toList());
        }
		return Arrays.<String>asList(server.getPlayerList().getOnlinePlayerNames());
	}

	@Override
	public boolean isUsernameIndex(String[] args, int index) {
		return true;
	}

}
