package net.fexcraft.lib.mc.network.packet;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import net.fexcraft.lib.mc.api.packet.IPacket;
import net.minecraft.nbt.NBTTagCompound;

public class PacketNBTTagCompound implements IPacket, IMessage {

    public NBTTagCompound nbt;

    public PacketNBTTagCompound(){}

    public PacketNBTTagCompound(NBTTagCompound obj){
        this.nbt = obj;
    }

    @Override
    public void toBytes(ByteBuf buf){
        ByteBufUtils.writeTag(buf, nbt);
    }

    @Override
    public void fromBytes(ByteBuf buf){
        nbt = ByteBufUtils.readTag(buf);
    }

}
