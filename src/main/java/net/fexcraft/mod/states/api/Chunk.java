package net.fexcraft.mod.states.api;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonObject;

import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.root.BuyableType;
import net.fexcraft.mod.states.api.root.Taxable;
import net.minecraft.util.ResourceLocation;

public interface Chunk extends BuyableType, Taxable {
	
	public int xCoord();
	
	public int zCoord();
	
	public default File getChunkFile(){
		return new File(States.getSaveDirectory(), "chunks/" + getChunkRegion() + "/" + this.xCoord() + "_" + this.zCoord() + ".json");
	}

	public default String getChunkRegion(){
		return (int)Math.floor(this.xCoord() / 32.0) + "_" + (int)Math.floor(this.zCoord() / 32.0);
	}

	public void save();
	
	public JsonObject toJsonObject();
	
	public District getDistrict();
	
	public void setDistrict(District dis);
	
	public long getCreated();
	
	public UUID getClaimer();

	public void setClaimer(UUID id);
	
	public long getChanged();
	
	public void setChanged(long new_change);
	
	public long getEdited();
	
	public void setEdited(long new_change);
	
	public List<ResourceLocation> getLinkedChunks();
	
	public ChunkType getType();
	
	public void setType(ChunkType type);

	public String getOwner();
	
	public void setOwner(String str);
	
	public ChunkPos getLink();
	
	/** Set to null to reset. */
	public void setLink(ChunkPos pos);
	
	public List<UUID> getPlayerWhitelist();
	
	public List<Integer> getCompanyWhitelist();
	
	public default Municipality getMunicipality(){
		return this.getDistrict().getMunicipality();
	}
	
	public default State getState(){
		return this.getDistrict().getMunicipality().getState();
	}
	
	public boolean isForceLoaded();

	public ChunkPos getChunkPos();

}
