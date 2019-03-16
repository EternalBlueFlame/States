package net.fexcraft.mod.states.cmds;

import net.fexcraft.lib.mc.api.registry.fCommand;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.states.States;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

@fCommand
public class GuiCmd extends CommandBase {

	@Override
	public String getCommandName(){
		return "st-gui";
	}

	@Override
	public String getCommandUsage(ICommandSender sender){
		return "/st-gui <args>";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		if(sender instanceof EntityPlayer == false){
			Print.chat(sender, "&7Only available Ingame.");
			return;
		}
		((EntityPlayer)sender).openGui(States.INSTANCE, 0, sender.getEntityWorld(), sender.getPosition().getX(), sender.getPosition().getY(), sender.getPosition().getZ());
	}

}
