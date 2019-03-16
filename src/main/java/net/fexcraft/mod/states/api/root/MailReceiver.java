package net.fexcraft.mod.states.api.root;


import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public interface MailReceiver {
	
	public BlockPos getMailbox();
	
	public void setMailbox(BlockPos pos);

	public void setMailbox(TileEntity pos);
}