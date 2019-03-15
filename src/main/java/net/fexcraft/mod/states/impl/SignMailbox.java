package net.fexcraft.mod.states.impl;

import java.util.UUID;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.Mailbox.MailType;
import net.fexcraft.mod.states.api.Mailbox.RecipientType;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.events.PlayerEvents;
import net.fexcraft.mod.states.objects.MailItem;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.StatesPermissions;
import net.minecraft.block.BlockWallSign;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandSender;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SignMailbox implements SignCapability.Listener {
	
	public static final ResourceLocation RESLOC = new ResourceLocation("states:mailbox");
	private boolean active;
	private UUID recipient;
	private String type;

	@Override
	public ResourceLocation getId(){
		return RESLOC;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, IBlockState state, TileEntitySign tileentity){
		if(event.world.isRemote){ return false; }
		if(!active){
			if(tileentity.signText[0].toLowerCase().equals("[st-mailbox]")){
				TileEntity te = event.world.getTileEntity(getPosAtBack(state, tileentity));
				if(te == null){ Print.chat(event.entityPlayer, "Not a valid mailbox position."); return false; }
				EnumFacing facing = state.getBlock() instanceof BlockWallSign ? EnumFacing.getFront(tileentity.getBlockMetadata()) : null;
				if(!te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)){
					Print.chat(event.entityPlayer, "Block/TileEntity cannot store items."); return false;
				}
				if(!PlayerEvents.checkAccess(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().getBlockState(te.getPos()), event.entityPlayer)){
					Print.chat(event.entityPlayer, "Block/TileEntity cannot be accessed."); return false;
				}
				Chunk chunk = StateUtil.getChunk(tileentity);
				String type = tileentity.signText[1].toLowerCase();
				switch(type){
					case "state":{
						if(!StatesPermissions.hasPermission(event.entityPlayer, "state.set.mailbox", chunk.getState())){
							Print.chat(event.entityPlayer, "No permission to set the State Mailbox."); return false;
						}
					}
					case "municipality":{
						if(!StatesPermissions.hasPermission(event.entityPlayer, "municipality.set.mailbox", chunk.getMunicipality())){
							Print.chat(event.entityPlayer, "No permission to set the Municipality Mailbox."); return false;
						}
					}
					case "district":{
						if(!StatesPermissions.hasPermission(event.entityPlayer, "district.set.mailbox", chunk.getDistrict())){
							Print.chat(event.entityPlayer, "No permission to set the District Mailbox."); return false;
						}
					}
					case "company": break;//TODO
					case "player":{
						String rec = tileentity.signText[2].toLowerCase();
						com.mojang.authlib.GameProfile prof = Static.getServer().getPlayerProfileCache().getGameProfileForUsername(rec);
						if(prof == null){
							Print.chat(event.entityPlayer, "Couldn't find player UUID in cache.");
							return false;
						}
						if(prof.getId().equals(event.entityPlayer.getGameProfile().getId()) || StatesPermissions.hasPermission(event.entityPlayer, "admin", null)){
							this.recipient = prof.getId();
							tileentity.signText[1] = Formatter.newTextComponentString(prof.getName());
							tileentity.signText[2] = "";
						}//TODO municipality check
						else{
							Print.chat(event.entityPlayer, "No permission to set mailbox of that player.");
						}
						break;
					}
					case "center": case "central": case "fallback":{
						if(!StatesPermissions.hasPermission(event.entityPlayer, "admin", chunk)){
							Print.chat(event.entityPlayer, "No permission to set the Central/Fallback Mailbox."); return false;
						}
						break;
					}
					default:{
						Print.chat(event.entityPlayer, "Invalid mailbox type.");
						return false;
					}
				}
				tileentity.signText[0] = Formatter.newTextComponentString("&0[&3Mailbox&0]");
				try{
					switch(type){
						case "state": chunk.getState().setMailbox(tileentity); break;
						case "municipality": chunk.getMunicipality().setMailbox(tileentity); break;
						case "district": chunk.getDistrict().setMailbox(tileentity); break;
						case "companry": break;//TODO
						case "player":{
							if(event.entityPlayer.getGameProfile().getId().equals(recipient)){
								event.entityPlayer.getCapability(StatesCapabilities.PLAYER, null).setMailbox(tileentity);
							}
							else{
								StateUtil.getPlayer(recipient, true).setMailbox(tileentity);
							}
							break;
						}
						/*case "center":*/ case "central": case "fallback":{
							StateUtil.getState(-1).setMailbox(tileentity);
							break;
						}
					}
					if(te instanceof TileEntityChest){
						((TileEntityChest)te).setCustomName(Formatter.format("&9Mailbox&0: &5" + (type.equals("player") ? Static.getPlayerNameByUUID(recipient) : type)));
					}
					this.type = type; cap.setActive(); this.active = true;
					this.sendUpdate(tileentity);
				}
				catch(Exception e){
					e.printStackTrace();
					Print.chat(event.entityPlayer, "Error occured, check log for info.");
				}
				return true;
			}
			else return false;
		}
		else{
			Print.chat(event.entityPlayer, "&k!000-000!000-000!");
		}
		return false;
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		if(!active){ return null; }
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("sign:active", active);
		compound.setString("sign:type", type);
		if(recipient != null) compound.setString("sign:recipient", recipient.toString());
		return compound;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		if(nbt == null || !(nbt instanceof NBTTagCompound)){ active = false; return; }
		NBTTagCompound compound = (NBTTagCompound)nbt;
		try{
			active = compound.getBoolean("sign:active");
			type = compound.getString("sign:type");
			recipient = compound.hasKey("sign:recipient") ? UUID.fromString(compound.getString("sign:recipient")) : null;
		}
		catch(Exception e){
			e.printStackTrace();
			active = false;
		}
	}

	public boolean accepts(BlockPos pos, RecipientType rtype, String receiver){
		Chunk chunk = StateUtil.getChunk(pos);
		switch(rtype){
			case COMPANY: return false;//TODO
			case DISTRICT: return type.equals("district") && receiver.equals(chunk.getDistrict().getId() + "");
			case MUNICIPALITY: return type.equals("municipality") && receiver.equals(chunk.getMunicipality().getId() + "");
			case PLAYER: return type.equals("player") && receiver.equals(recipient.toString());
			case STATE:{
				if(type.equals("central") || type.equals("fallback")) return true;
				return type.equals("state") && receiver.equals(chunk.getState().getId() + "");
			}
			default: return false;
		}
	}

	public final boolean insert(ICommandSender ics, TileEntitySign tile, RecipientType rectype, String receiver, String sender, String message, MailType type, long expiry, NBTTagCompound compound){
		ItemStack stack = new ItemStack(MailItem.INSTANCE, 1, type.toMetadata());
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("Receiver", rectype.name().toLowerCase() + ":" + receiver);
		nbt.setString("Sender", sender);
		nbt.setString("Message", message);
		nbt.setString("Type", type.name());
		nbt.setString("Content", message);
		if(compound != null) nbt.setTag("StatesData", compound);
		if(expiry > 0) nbt.setLong("Expiry", Time.getDate() + expiry);
		stack.setTagCompound(nbt);
		EnumFacing facing = tile.getWorld().getBlockState(tile.getPos()).getBlock() instanceof BlockWallSign ? EnumFacing.getFront(tile.getBlockMetadata()) : null;
		TileEntity te = tile.getWorld().getTileEntity(getPosAtBack(tile.getWorld().getBlockState(tile.getPos()), tile));
		IItemHandler handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
		for(int i = 0; i < handler.getSlots(); i++){
			if(handler.insertItem(i, stack, true).isEmpty()){
				stack = handler.insertItem(i, stack, false);
				if(stack == null || stack.isEmpty()) break;
			}
		}
		if(stack == null || !stack.isEmpty()){
			if(ics != null) Print.chat(ics, "Failed to send mail, mailbox of recipient may be full!");
			Print.log("Failed to insert mail! Probably no space in target mailbox!");
			Print.log(tile + " || " + rectype + " || " + receiver + " || " + sender + " || " + message + " || " + type + " || " + expiry + " || " + compound);
			return false;
		}
		return true;
	}
	
}