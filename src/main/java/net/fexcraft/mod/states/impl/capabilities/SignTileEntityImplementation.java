package net.fexcraft.mod.states.impl.capabilities;

import java.util.UUID;

import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkType;
import net.fexcraft.mod.states.api.capabilities.SignTileEntityCapability;
import net.fexcraft.mod.states.guis.Listener;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;

public class SignTileEntityImplementation implements SignTileEntityCapability {
	
	private TileEntitySign tileentity;
	private boolean isStatesSign = false;
	private String mode;
	private long lastupdate = 0;

	@Override
	public void setup(Chunk chunk){
		isStatesSign = true;
		mode = tileentity.signText[1].toLowerCase();
		update(chunk, null, true);
	}

	@Override
	public void setTileEntity(TileEntitySign tileentity){
		this.tileentity = tileentity;
	}

	@Override
	public TileEntitySign getTileEntity(){
		return tileentity;
	}

	@Override
	public void update(Chunk chunk, String task, boolean send){
		if(!isStatesSign || chunk == null || (task != null && !task.equals(mode))){
			return;
		}
		//
		switch(mode){
			case "chunk":{
				if(chunk.getPrice() > 0){
					tileentity.signText[1] = Formatter.format("&2For Sale!");
					tileentity.signText[2] = Config.getWorthAsString(chunk.getPrice());
					//tileentity.signText[3] = Formatter.format("&2" + (chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(UUID.fromString(chunk.getOwner())) : chunk.getOwner()));
				}
				else{
					switch(chunk.getType()){
						case PRIVATE:{
							tileentity.signText[1] = Formatter.format("&cPrivate Property");
							tileentity.signText[2] = Formatter.format("&c- - - -");
							break;
						}
						case PUBLIC:{
							tileentity.signText[1] = Formatter.format("&cPublic Access");
							tileentity.signText[2] = Formatter.format("&cProperty");
							break;
						}
						default:
							tileentity.signText[1] = Formatter.format("&9Managed");
							tileentity.signText[2] = Formatter.format("&9Property");
							break;
					}
				}
				if(chunk.getDistrict().getMunicipality().getState().getId() >= 0){
					tileentity.signText[3] = Formatter.format("&2" + (chunk.getType() == ChunkType.PRIVATE ? Static.getPlayerNameByUUID(UUID.fromString(chunk.getOwner())) : chunk.getOwner()));
				}
				else{
					tileentity.signText[3] = Formatter.format("&2Wilderness");
				}
				tileentity.signText[0] = Formatter.format("&0[&9States&0]&2> &8Chunk");
				break;
			}
			case "district":{
				tileentity.signText[1] = Formatter.format("&9" + chunk.getDistrict().getName());
				tileentity.signText[2] = Formatter.format("&6" + chunk.getDistrict().getType().name().toLowerCase());
				tileentity.signText[3] = Formatter.format(chunk.getDistrict().getManager() == null ? "&cno manager" : "&2" + Static.getPlayerNameByUUID(chunk.getDistrict().getManager()));
				//
				tileentity.signText[0] = Formatter.format("&0[&9States&0]&2> &8District");
				break;
			}
			case "municipality":{
				tileentity.signText[1] = Formatter.format("&9" + chunk.getDistrict().getMunicipality().getName());
				tileentity.signText[2] = Formatter.format("&6" + chunk.getDistrict().getMunicipality().getType().getTitle());
				tileentity.signText[3] = Formatter.format(chunk.getDistrict().getMunicipality().getMayor() == null ? "&cno mayor" : "&2" + Static.getPlayerNameByUUID(chunk.getDistrict().getMunicipality().getMayor()));
				//
				tileentity.signText[0] = Formatter.format("&0[&9St&0]&2> &8Municipality");
				break;
			}
			case "state":{
				tileentity.signText[1] = Formatter.format("&9" + chunk.getDistrict().getMunicipality().getState().getName());
				tileentity.signText[2] = Formatter.format("&6 - - - ");
				tileentity.signText[3] = Formatter.format(chunk.getDistrict().getMunicipality().getState().getLeader() == null ? "&cno mayor" : "&2" + Static.getPlayerNameByUUID(chunk.getDistrict().getMunicipality().getState().getLeader()));
				//
				tileentity.signText[0] = Formatter.format("&0[&9States&0]&2> &8State");
				break;
			}
			case "map":{
				tileentity.signText[0] = Formatter.format("&0[&9States&0]&2> &8Map");
				String str = tileentity.signText[2].toLowerCase();
				boolean found = false;
				for(String string : Listener.MAP_VIEW_MODES){
					if(str.equals(string)){
						found = true;
						break;
					}
				}
				if(!found){
					str = "surface";
				}
				tileentity.signText[1] = str;
				tileentity.signText[2] = Formatter.format("&7" + chunk.xCoord() + "x");
				tileentity.signText[3] = Formatter.format("&7" + chunk.zCoord() + "z");
				break;
			}
			default:{
				//Invalid mode, thus let's unmark this;
				this.isStatesSign = false;
				break;
			}
		}
		lastupdate = chunk.getChanged();
		//
		if(send){
			Static.getPlayers().forEach(player -> {
				player.playerNetServerHandler.sendPacket(tileentity.getDescriptionPacket());
			});
		}
	}

	@Override
	public NBTBase writeToNBT(Capability<SignTileEntityCapability> capability, EnumFacing side){
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("StatesSign", isStatesSign);
		if(isStatesSign){
			compound.setString("Mode", mode);
			compound.setLong("LastUpdate", lastupdate);
		}
		Print.debug("W: " + compound);
		return compound;
	}

	@Override
	public void readNBT(Capability<SignTileEntityCapability> capability, EnumFacing side, NBTBase nbt){
		NBTTagCompound compound = (NBTTagCompound)nbt;
		if(isStatesSign = compound.getBoolean("StatesSign")){
			mode = compound.getString("Mode");
			lastupdate = compound.getLong("LastUpdate");
		}
		Print.debug("R:" + compound);
	}

	@Override
	public boolean isStatesSign(){
		return isStatesSign;
	}

	@Override
	public void onPlayerInteract(Chunk chunk, EntityPlayer player){
		if(chunk.getChanged() != this.lastupdate){
			Print.chat(player, "&7Sign Updating...");
			update(chunk, null, true);
			return;
		}
		Print.debug(player.getDisplayName(), chunk.toJsonObject().getAsString());
		switch(mode){
			case "chunk":{
				MinecraftServer.getServer().getCommandManager().executeCommand(player, chunk.getPrice() > 0 ? ("ck buy via-sign " + new BlockPos(tileentity).toLong()) : "ck info");
				break;
			}
			case "district":{
				MinecraftServer.getServer().getCommandManager().executeCommand(player, "dis info");
				break;
			}
			case "municipality":{
				MinecraftServer.getServer().getCommandManager().executeCommand(player, "mun info");
				break;
			}
			case "state":{
				MinecraftServer.getServer().getCommandManager().executeCommand(player, "st info");
				break;
			}
			case "map":{
				String str = tileentity.signText[1].toLowerCase().replace("s.", "surface_");
				int j = -1;
				for(int i = 0; i < Listener.MAP_VIEW_MODES.length; i++){
					if(Listener.MAP_VIEW_MODES[i].equals(str)){
						j = i;
						break;
					}
				}
				if(j < 0){ j = 0; }
				player.openGui(States.INSTANCE, 1, player.worldObj, j, 0, 0);
				break;
			}
			default:{
				//Invalid mode, thus let's unmark this;
				this.isStatesSign = false;
				break;
			}
		}
	}

}
