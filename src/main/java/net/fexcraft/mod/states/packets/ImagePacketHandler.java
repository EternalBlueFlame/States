package net.fexcraft.mod.states.packets;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import net.fexcraft.mod.states.guis.AreaView;
import net.fexcraft.mod.states.util.ImageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;

public class ImagePacketHandler {
	
	public static class Server implements IMessageHandler<ImagePacket, IMessage> {
		@Override
		public IMessage onMessage(final ImagePacket packet, final MessageContext ctx) {
			IThreadListener ls = FMLCommonHandler.instance().getMinecraftServerInstance();
			ls.addScheduledTask(new Runnable(){
				@Override
				public void run(){
					//
				}
			});
			return null;
		}
	}
	
	public static class Client implements IMessageHandler<ImagePacket, IMessage> {
		@Override
		public IMessage onMessage(final ImagePacket packet, final MessageContext ctx) {
			IThreadListener ls = Minecraft.getMinecraft();
			ls.addScheduledTask(new Runnable(){
				@Override
				public void run(){
					switch(packet.target){
						case "area_view":{
							ImageUtil.load(AreaView.map_texture, packet.image);
						}
					}
				}
			});
			return null;
		}
	}
	
}