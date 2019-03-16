package net.fexcraft.mod.states.events;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.fexcraft.mod.states.util.ForcedChunksManager;
import net.fexcraft.mod.states.util.ImageCache;
import net.fexcraft.mod.states.util.StateLogger;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraftforge.event.world.ExplosionEvent;
import net.minecraftforge.event.world.WorldEvent;



@Mod.EventBusSubscriber
public class WorldEvents {
	

	
	@SubscribeEvent
	public static void onWorldLoad(WorldEvent.Load event){
		if(event != null && (event.world.provider.dimensionId != 0 || event.world.isRemote)) return;
		ImageCache.loadQueue();
		ForcedChunksManager.load();
		//event.getWorld().addEventListener(new TestListener());
	}
	
	@SubscribeEvent
	public static void onWorldUnload(WorldEvent.Unload event){
		if(event.world.provider.dimensionId != 0 || event.world.isRemote) return;
		StateUtil.unloadAll(); StateUtil.clearAll(); ChunkEvents.LOADED = false;
		ImageCache.saveQueue();
		StateLogger.log("all", "Unloading World...");
		ForcedChunksManager.unload();
	}
	
	@SubscribeEvent
	public static void onExplosion(ExplosionEvent event){
		try{
			event.explosion.affectedBlockPositions.clear();
			//TODO add config for no-wilderness/all/specific protection
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	//
	/*
	@SubscribeEvent
	public static void onAttachEventTileEntity(AttachCapabilitiesEvent<net.minecraft.tileentity.TileEntity> event){
		if(event.getObject() instanceof TileEntitySign){
			event.addCapability(new ResourceLocation("states", "sign"), new SignTileEntityCapabilityUtil(event.getObject()));
		}
	}
	
	@SubscribeEvent
	public static void onAttachEventEntityPlayer(AttachCapabilitiesEvent<net.minecraft.entity.Entity> event){
		if(event.getObject() instanceof EntityPlayer && !event.getObject().world.isRemote){
			event.addCapability(PlayerCapabilityUtil.REGISTRY_NAME, new PlayerCapabilityUtil((EntityPlayer)event.getObject()));
		}
	}
	
	@SubscribeEvent
	public static void onAttachEventWorld(AttachCapabilitiesEvent<net.minecraft.world.World> event){
		event.addCapability(WorldCapabilityUtil.REGISTRY_NAME, new WorldCapabilityUtil(event.getObject()));
	}
	
	@SubscribeEvent
	public static void onAttachEventChunk(AttachCapabilitiesEvent<net.minecraft.world.chunk.Chunk> event){
            if(event.getObject().getWorld().provider.getDimension() != 0){ return; }
            event.addCapability(ChunkCapabilityUtil.REGISTRY_NAME, new ChunkCapabilityUtil(event.getObject()));
	}*/
	
}
