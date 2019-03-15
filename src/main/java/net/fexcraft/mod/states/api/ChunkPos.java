package net.fexcraft.mod.states.api;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class ChunkPos extends ChunkCoordinates {

	public ChunkPos(BlockPos pos){ super(pos.getX(),pos.getY(),pos.getZ()); }

	public ChunkPos(Chunk pos){ super(pos.xPosition,0,pos.zPosition); }
	
	public ChunkPos(int x, int y, int z){ super(x, y, z); }
	
	public ChunkPos(String[] arr){ super(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]),Integer.parseInt(arr[2])); }
	
	@Override
	public int compareTo(ChunkCoordinates o){
		if(this.equals(o)){ return 0; }
        int result = ((this.posX - o.posX) * (this.posX + o.posX)) + ((this.posZ - o.posZ) * (this.posZ + o.posZ));
        if(result != 0){ return result; }
        return this.posX < 0 ? (o.posX < 0 ? o.posZ - this.posZ : -1) : (o.posX < 0 ? 1 : this.posZ - o.posZ);
	}
	
}