package net.fexcraft.mod.states.events;

import java.io.File;


import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.ChunkPos;
import net.fexcraft.mod.states.impl.GenericChunk;
import net.fexcraft.mod.states.impl.GenericDistrict;
import net.fexcraft.mod.states.impl.GenericMunicipality;
import net.fexcraft.mod.states.impl.GenericState;
import net.fexcraft.mod.states.impl.capabilities.SignTileEntityCapabilityUtil;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.event.world.ChunkEvent;

public class ChunkEvents {
	
	private static int[] def_st = new int[]{ -1, 0 };
	private static int[] def_mun = new int[]{ -1, 0 };
	private static int[] def_dis = new int[]{ -2, -1, 0 };
	public static boolean LOADED = false;
    
    @SubscribeEvent
    public static void onLoad(ChunkEvent.Load event){
		if(!LOADED){
			States.updateSaveDirectory(event.world);
			StateLogger.log("all", "Loading World...");
			for(int i : def_st){
				if(!States.STATES.containsKey(i)){
					States.STATES.put(i, new GenericState(i));
					States.STATES.get(i).save();
				}
			}
			for(int i : def_mun){
				if(!States.MUNICIPALITIES.containsKey(i)){
					States.MUNICIPALITIES.put(i, new GenericMunicipality(i));
					States.MUNICIPALITIES.get(i).save();
				}
			}
			for(int i : def_dis){
				if(!States.DISTRICTS.containsKey(i)){
					States.DISTRICTS.put(i, new GenericDistrict(i));
					States.DISTRICTS.get(i).save();
				}
			} LOADED = true;
		}
        if(event.world.provider.dimensionId != 0) return;
        if(event.world.isRemote){
    		//net.fexcraft.mod.states.guis.Minimap.loadChunk(event.getChunk());
    		return;
        } /*if(!WorldEvents.LOADED){ WorldEvents.onWorldLoad(null); WorldEvents.LOADED = true; }*/
        ChunkPos pos = new ChunkPos(event.getChunk().xPosition,0, event.getChunk().zPosition);
        if(!States.CHUNKS.containsKey(pos)){
            States.CHUNKS.put(pos, new GenericChunk(event.world, pos));
        }
    }
    
    @SubscribeEvent
    public static void onUnload(ChunkEvent.Unload event){
        if(event.world.provider.dimensionId != 0) return;
    	if(event.world.isRemote){
    		//net.fexcraft.mod.states.guis.Minimap.unloadChunk(event.getChunk());
    		return;
    	}
        Chunk chunk = StateUtil.getChunk(event.getChunk());
        //Chunk chunk = event.getChunk().getCapability(StatesCapabilities.CHUNK, null).getStatesChunk(true);
        if(!(chunk == null)){
        	if(!States.CHUNKS.entrySet().removeIf(pre -> pre.getValue().xCoord() == chunk.xCoord() && pre.getValue().zCoord() == chunk.zCoord())){
        		Print.log("Error while trying to remove chunkdata for " + StateLogger.chunk(chunk) + " from main storaging Map.");
        	}
            File file = chunk.getChunkFile();
            boolean matches = file.exists() && JsonUtil.get(file).get("changed").getAsLong() == chunk.getChanged();
            chunk.save();
            if(!matches || chunk.getEdited() > chunk.getChanged()){
            	ImageCache.update(event.world, event.getChunk());
            }
        }
        event.getChunk().chunkTileEntityMap.keySet().forEach(key -> {
            SignTileEntityCapabilityUtil.TILEENTITIES.remove(key);
        });
    }

}
