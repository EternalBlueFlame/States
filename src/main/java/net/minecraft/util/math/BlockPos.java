package net.minecraft.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class BlockPos {
    public double x,y,z;

    public BlockPos(double xCoord, double yCoord, double zCoord){
        x=xCoord;y=yCoord;z=zCoord;
    }

    public BlockPos(TileEntity e){
        x=e.xCoord;y=e.yCoord;z=e.zCoord;
    }
    public BlockPos(Entity e){
        x=e.posX;y=e.posY;z=e.posZ;
    }

    public int getX(){
        return (int)x;
    }

    public int getZ(){
        return (int)z;
    }

    public int getY(){
        return (int)y;
    }
}
