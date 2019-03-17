package net.fexcraft.mod.states.impl;

import net.fexcraft.lib.mc.capabilities.sign.SignCapability;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.events.PlayerEvents;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.StatesPermissions;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSign;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class SignShop implements SignCapability.Listener {
	
	private static final ResourceLocation REGNAME = new ResourceLocation("states:shop");
	private long price;
	private boolean active, server;
	private ItemStack itemtype;
	private ResourceLocation account;

	@Override
	public ResourceLocation getId(){
		return REGNAME;
	}

	@Override
	public boolean isActive(){
		return active;
	}

	@Override
	public boolean onPlayerInteract(SignCapability cap, PlayerInteractEvent event, Block state, TileEntitySign tileentity){
		if(event.world.isRemote){ return false; }
		if(!active){
			if(tileentity.signText[0].toLowerCase().equals("[st-shop]")){
				if(!(tileentity.signText[3].toLowerCase().equals("buy") || tileentity.signText[3].toLowerCase().equals("sell"))){
					Print.chat(event.entityPlayer, "Invalid type on line 4.");
					return false;
				}
				tileentity.signText[0] = Formatter.format("&0[&3St&8-&3Shop&0]");
				TileEntity te = event.world.getTileEntity(getPosAtBack(state, tileentity));
				EnumFacing facing = state instanceof BlockSign ? EnumFacing.getFront(tileentity.getBlockMetadata()) : null;
				if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing) && PlayerEvents.checkAccess(te.getWorldObj(), te.getPos(), te.getWorld().getBlockState(te.getPos()), event.entityPlayer)){
					itemtype = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing).getStackInSlot(0).copy();
					if(!tileentity.signText[1].equals("")){
						Chunk chunk = StateUtil.getChunk(te.xCoord,te.zCoord);
						switch(tileentity.signText[1].toLowerCase()){
							case "district":
							case "municipality":{
								if(StatesPermissions.hasPermission(event.entityPlayer, "shop.create.municipality", chunk)){
									account = new ResourceLocation("municipality:" + chunk.getMunicipality().getId());
								}
								else{
									Print.chat(event.entityPlayer, "&9No permission to Create Municipality Shops.");
									return true;
								}
								break;
							}
							case "state":{
								if(StatesPermissions.hasPermission(event.entityPlayer, "shop.create.state", chunk)){
									account = new ResourceLocation("state:" + chunk.getMunicipality().getId());
								}
								else{
									Print.chat(event.entityPlayer, "&9No permission to Create State Shops.");
									return true;
								}
								break;
							}
							case "admin":
							case "server":{
								if(StatesPermissions.hasPermission(event.entityPlayer, "shop.create.server", chunk)){
									account = States.SERVERACCOUNT.getAsResourceLocation();
									server = true;
								}
								else{
									Print.chat(event.entityPlayer, "&9No permission to Create Server Shops.");
									return true;
								}
								break;
							}
							case "player":{
								account = null;
								break;
							}
						}
					}
					if(account == null){
						account = event.entityPlayer.getCapability(StatesCapabilities.PLAYER, null).getAccount().getAsResourceLocation();
					}
					tileentity.signText[1] = Formatter.format(itemtype.getDisplayName());
					try{
						long leng = Long.parseLong(tileentity.signText[2]);
						tileentity.signText[2] = Formatter.format(Config.getWorthAsString(leng, true, leng < 10));
						price = leng;
					}
					catch(Exception e){
						e.printStackTrace();
						Print.chat(event.entityPlayer, "Invalid Price. (1000 == 1" + Config.CURRENCY_SIGN + "!)");
						return false;
					}
					cap.setActive();
					active = true;
					this.sendUpdate(tileentity);
					return true;
				}
				else{
					event.entityPlayer.addChatComponentMessage(new ChatComponentText(Formatter.format("No ItemStack Container found.")));
				}
			}
		}
		else{
			TileEntity te = event.world.getTileEntity(getPosAtBack(state, tileentity));
			EnumFacing facing = state.getBlock() instanceof BlockSign ? EnumFacing.getFront(tileentity.getBlockMetadata()) : null;
			if(te != null && te.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing)){
				if(event.entityPlayer.getHeldItem() ==null){
					Account shop = DataManager.getAccount(account.toString(), true, false);
					if(shop == null){
						Print.chat(event.entityPlayer, "Shop Account couldn't be loaded.");
						return true;
					}
					Account playeracc = event.entityPlayer.getCapability(StatesCapabilities.PLAYER, null).getAccount();
					Bank playerbank = event.entityPlayer.getCapability(StatesCapabilities.PLAYER, null).getBank();
					IItemHandler te_handler = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, facing);
					IItemHandler pl_handler = event.entityPlayer.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
					if(tileentity.signText[3].toLowerCase().startsWith("buy")){
						if(hasStack(event.entityPlayer, te_handler, false)){
							if(playerbank.processAction(Bank.Action.TRANSFER, event.entityPlayer, playeracc, price, shop)){
								event.entityPlayer.inventory.addItemStackToInventory(getStackIfPossible(te_handler, false));
								event.entityPlayer.addChatComponentMessage(new ChatComponentText(Formatter.format("Items bought.")));
							}
						}
					}
					else if(tileentity.signText[3].toLowerCase().startsWith("sell")){
						if(hasStack(event.entityPlayer, pl_handler, true) && hasSpace(event.entityPlayer, te_handler)){
							if(DataManager.getBank(shop.getBankId(), true, false).processAction(Bank.Action.TRANSFER, event.entityPlayer, shop, price, playeracc)){
								addStack(te_handler, getStackIfPossible(pl_handler, true));
								event.entityPlayer.addChatComponentMessage(new ChatComponentText(Formatter.format("Items sold.")));
							}
						}
					}
					else{
						Print.chat(event.entityPlayer, "Invalid Mode at line 4.");
					}
				}
				else{
					Print.chat(event.entityPlayer, "&9Shop Owner: &7" + account.toString());
					Print.chat(event.entityPlayer, "&9Item: &7" + itemtype.getDisplayName());
					Print.chat(event.entityPlayer, "&9Reg: &7" + itemtype.getItem().delegate.name());
					if(itemtype.getItemDamage() > 0){
						Print.chat(event.entityPlayer, "&9Meta: &8" + itemtype.getItemDamage());
					}
					Print.chat(event.entityPlayer, "&9Amount: &6" + itemtype.stackSize);
					if(itemtype.hasTagCompound()){
						Print.chat(event.entityPlayer, "&9NBT: &8" + itemtype.getTagCompound().toString());
					}
				}
				return true;
			}
			else{
				event.entityPlayer.addChatComponentMessage(new ChatComponentText(Formatter.format("No ItemStack Container linked.")));
			}
		}
		return false;
	}

	private void addStack(IItemHandler handler, ItemStack stack){
		if(server){ return; }
		for(int i = 0; i < handler.getSlots(); i++){
			if((stack = handler.insertItem(i, stack, false)) ==null){
				return;
			}
		}
	}

	private boolean hasSpace(EntityPlayer player, IItemHandler handler){
		if(server){ return true; }
		for(int i = 0; i < handler.getSlots(); i++){
			if(handler.getStackInSlot(i) ==null || isEqualOrValid(handler.getStackInSlot(i), true)){
				return true;
			}
		}
		player.addChatComponentMessage(new ChatComponentText(Formatter.format("Not enough space in Container.")));
		return false;
	}

	private boolean hasStack(EntityPlayer player, IItemHandler handler, boolean plinv){
		if(server && !plinv){ return true; }
		for(int i = 0; i < handler.getSlots(); i++){
			if(isEqualOrValid(handler.getStackInSlot(i), false)){
				return true;
			}
		}
		player.addChatComponentMessage(new ChatComponentText(Formatter.format( "Not enough Items in " + (plinv ? "Inventory" : "Container") + ".")));
		return false;
	}

	private boolean isEqualOrValid(ItemStack stack, boolean reversecheck){
		if(reversecheck ? stack.stackSize + itemtype.stackSize > stack.getMaxStackSize() : stack.stackSize < itemtype.stackSize){
            return false;
        }
        else if(stack.getItem() != itemtype.getItem()){
            return false;
        }
        else if(stack.getItemDamage() != itemtype.getItemDamage()){
            return false;
        }
        else if(stack.getTagCompound() == null && itemtype.getTagCompound() != null){
            return false;
        }
        else{
            return (stack.getTagCompound() == null || stack.getTagCompound().equals(itemtype.getTagCompound())) && stack.areCapsCompatible(itemtype);
        }
	}

	private ItemStack getStackIfPossible(IItemHandler handler, boolean player){
		if(server && !player){
			return itemtype.copy();
		}
		for(int i = 0; i < handler.getSlots(); i++){
			if(isEqualOrValid(handler.getStackInSlot(i), false)){
				return handler.extractItem(i, itemtype.stackSize, false);
			}
		}
		return null;
	}

	@Override
	public NBTBase writeToNBT(Capability<SignCapability> capability, EnumFacing side){
		if(!active){
			return null;
		}
		NBTTagCompound compound = itemtype.writeToNBT(new NBTTagCompound());
		compound.setLong("sign:price", price);
		compound.setBoolean("sign:active", active);
		compound.setString("sign:account", account.toString());
		if(server){
			compound.setBoolean("sign:server", server);
		}
		return compound;
	}

	@Override
	public void readNBT(Capability<SignCapability> capability, EnumFacing side, NBTBase nbt){
		if(nbt == null || !(nbt instanceof NBTTagCompound)){
			active = false;
			return;
		}
		NBTTagCompound compound = (NBTTagCompound)nbt;
		try{
			itemtype = ItemStack.loadItemStackFromNBT(compound);
			price = compound.getInteger("sign:price");
			active = compound.getBoolean("sign:active");
			account = new ResourceLocation(compound.getString("sign:account"));
			server = compound.hasKey("sign:server") && compound.getBoolean("sign:server");
		}
		catch(Exception e){
			e.printStackTrace();
			active = false;
		}
	}
	
}