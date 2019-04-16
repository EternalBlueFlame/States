package net.fexcraft.lib.fcl;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import cpw.mods.fml.relauncher.Side;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.IPacketListener;
import net.minecraft.client.Minecraft;

import java.util.HashMap;

public class NBTTagCompoundPacketHandler {

    private static HashMap<String, IPacketListener<PacketNBTTagCompound>> sls = new HashMap<String, IPacketListener<PacketNBTTagCompound>>();
    private static HashMap<String, IPacketListener<PacketNBTTagCompound>> cls = new HashMap<String, IPacketListener<PacketNBTTagCompound>>();

    public static class Server implements IMessageHandler<PacketNBTTagCompound, IMessage> {
        @Override
        public IMessage onMessage(final PacketNBTTagCompound packet, final MessageContext ctx) {
            /*IThreadListener ls = FMLCommonHandler.instance().getMinecraftServerInstance();
            ls.addScheduledTask(new Runnable(){
                @Override
                public void run(){*/
                    if(!packet.nbt.hasKey("target_listener")){
                        Print.log("[FCL] Received NBT Packet, but had no target listener, ignoring!");
                        Print.log("[NBT] " + packet.nbt.toString());
                        return null;
                    }
                    IPacketListener<PacketNBTTagCompound> listener = sls.get(packet.nbt.getString("target_listener"));
                    if(listener != null){
                        listener.process(packet, new Object[]{ctx.getServerHandler().playerEntity});
                    }
                //}
            //});
            return null;
        }
    }

    public static class Client implements IMessageHandler<PacketNBTTagCompound, IMessage> {
        @Override
        public IMessage onMessage(final PacketNBTTagCompound packet, final MessageContext ctx) {
           /* IThreadListener ls = Minecraft.getMinecraft();
            ls.addScheduledTask(new Runnable(){
                @Override
                public void run(){*/
                    if(!packet.nbt.hasKey("target_listener")){
                        Print.log("[FCL] Received NBT Packet, but had no target listener, ignoring!");
                        Print.log("[NBT] " + packet.nbt.toString());
                        return null;
                    }
                    IPacketListener<PacketNBTTagCompound> listener = cls.get(packet.nbt.getString("target_listener"));
                    if(listener != null){
                        listener.process(packet, new Object[]{Minecraft.getMinecraft().thePlayer});
                    }
               // }
           // });
            return null;
        }
    }

    public static void addListener(Side side, IPacketListener<PacketNBTTagCompound> listener) {
        if(side.isClient()){
            cls.put(listener.getId(), listener);
        }
        else{
            sls.put(listener.getId(), listener);
        }
    }
}
