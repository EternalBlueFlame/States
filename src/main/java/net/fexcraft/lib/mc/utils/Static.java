package net.fexcraft.lib.mc.utils;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class Static {
    public static List<EntityPlayerMP> getPlayers(){
        return MinecraftServer.getServer().getConfigurationManager().playerEntityList;
    }


    public static @Nullable EntityPlayer getPlayerByUUID(UUID uuid){
        //we could directly add it to the loop, but that's unsafe during concurrent edits...
        List<EntityPlayer> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
        for(EntityPlayer p : players){
            if(p.getUniqueID().equals(uuid)){
                return p;
            }
        }
        return null;
    }

    public static @Nullable EntityPlayer getPlayerByUUID(String uuid){
        return getPlayerByUUID(UUID.fromString(uuid));
    }

    public static String getPlayerNameByUUID(String player){
        return MinecraftServer.getServer().func_152358_ax().func_152652_a(UUID.fromString(player)).getName();
    }
    public static String getPlayerNameByUUID(UUID player){
        return MinecraftServer.getServer().func_152358_ax().func_152652_a(player).getName();
    }
}
