package net.minecraft.util.math;

import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;

import static net.minecraft.realms.RealmsMth.log2;
import static net.minecraft.realms.RealmsMth.smallestEncompassingPowerOfTwo;

public class BlockPos {
    public double x, y, z;

    public BlockPos(double xCoord, double yCoord, double zCoord) {
        x = xCoord;
        y = yCoord;
        z = zCoord;
    }

    public BlockPos(TileEntity e) {
        x = e.xCoord;
        y = e.yCoord;
        z = e.zCoord;
    }

    public BlockPos(Entity e) {
        x = e.posX;
        y = e.posY;
        z = e.posZ;
    }

    public int getX() {
        return (int) x;
    }

    public int getZ() {
        return (int) z;
    }

    public int getY() {
        return (int) y;
    }

    public BlockPos add(double xCoord, double yCoord, double zCoord) {
        x += xCoord;
        y += yCoord;
        z += zCoord;
        return this;
    }

    private static final int NUM_X_BITS = 1 + log2(smallestEncompassingPowerOfTwo(30000000));
    private static final int NUM_Y_BITS = 64 - NUM_X_BITS - NUM_X_BITS;

    public static BlockPos fromLong(long serialized) {
        return new BlockPos((int) (serialized << 64 - (NUM_X_BITS + NUM_Y_BITS) - NUM_X_BITS >> 64 - NUM_X_BITS),
                (int) (serialized << 64 - NUM_X_BITS - NUM_Y_BITS >> 64 - NUM_Y_BITS),
                (int) (serialized << 64 - NUM_X_BITS >> 64 - NUM_X_BITS));
    }

    private static final long X_MASK = (1L << NUM_X_BITS) - 1L;
    private static final long Y_MASK = (1L << NUM_Y_BITS) - 1L;

    public long toLong() {
        return ((long) this.getX() & X_MASK) << (NUM_X_BITS + NUM_Y_BITS) | ((long) this.getY() & Y_MASK) << NUM_X_BITS | ((long) this.getZ() & X_MASK) << 0;
    }
}
