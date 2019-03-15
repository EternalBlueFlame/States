package net.fexcraft.mod.states.api.root;


import net.minecraft.tileentity.TileEntity;

public interface MailReceiver {
	
	public int[] getMailbox();
	
	public void setMailbox(int[] pos);

	public void setMailbox(TileEntity pos);
}