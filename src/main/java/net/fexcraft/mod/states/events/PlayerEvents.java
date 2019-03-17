package net.fexcraft.mod.states.events;

import java.util.Arrays;
import java.util.List;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.fexcraft.mod.lib.fcl.PacketHandler;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.Chunk;
import net.fexcraft.mod.states.api.capabilities.PlayerCapability;
import net.fexcraft.mod.states.api.capabilities.SignTileEntityCapability;
import net.fexcraft.mod.states.api.capabilities.StatesCapabilities;
import net.fexcraft.mod.states.util.Config;
import net.fexcraft.mod.states.util.MessageSender;
import net.fexcraft.mod.states.util.StateUtil;
import net.fexcraft.mod.states.util.TaxSystem;
import net.fexcraft.mod.states.util.UpdateHandler;
import net.minecraft.block.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

@Mod.EventBusSubscriber
public class PlayerEvents {
	
	@SubscribeEvent
	public static void onLogin(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event){
		PlayerCapability player = event.player.getCapability(StatesCapabilities.PLAYER, null);
		if(player == null){
			Print.chat(event.player, "Player data couldn't be loaded.");
			return;
		}
		if(!player.isLoaded()){ player.load(); }
		States.PLAYERS.put(event.player.getGameProfile().getId(), player);
		Print.chat(event.player, "&e====-====-====-====-====-====" + States.PREFIX);
		Print.chat(event.player, "&6Welcome back " + player.getFormattedNickname() + "&6!");//TODO replace with nick
		if(event.player.dimension != 0){
			Print.chat(event.player, "&2You are currently in &7DIM($0)&0.".replace("$0", event.player.dimension + ""));
		}
		else{
			Chunk chunk = StateUtil.getChunk(event.player);
			Print.chat(event.player, "&2You are currently in a district called &7$0&2.".replace("$0", chunk.getDistrict().getName()));
			Print.chat(event.player, "&2Which is part of the &6$1 &7$0&2.".replace("$0", chunk.getDistrict().getMunicipality().getName()).replace("$1", chunk.getDistrict().getMunicipality().getType().getTitle()));
			Print.chat(event.player, "&2In the State of &7$0&2.".replace("$0", chunk.getDistrict().getMunicipality().getState().getName()));
		}
		if(player.getMailbox() == null) Print.chat(event.player, "&6You have &7no mailbox set&6!"); 
		Print.chat(event.player, "&e====-====-====-====-====-====" + States.PREFIX);
		sendLocationUpdate(event.player, null, "&6Welcome back " + player.getFormattedNickname() + "&6!", "", "", 3);
		if(UpdateHandler.STATE != null){
			Print.chat(event.player, UpdateHandler.STATE);
		}
		//
		MessageSender.toWebhook(null, event.player.getGameProfile().getName() + " joined.");
		TaxSystem.processPlayerTax(TaxSystem.getProbableSchedule(), player);
	}
	
	@SubscribeEvent
	public static void onLogout(cpw.mods.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event){
		States.PLAYERS.remove(event.player.getGameProfile().getId());
		MessageSender.toWebhook(null, event.player.getGameProfile().getName() + " left.");
	}
	
	@SubscribeEvent
	public static void onRespawn(PlayerEvent.Clone event){
		event.entityPlayer.getCapability(StatesCapabilities.PLAYER, null)
			.copyFromOld(event.original.getCapability(StatesCapabilities.PLAYER, null));
		States.PLAYERS.put(event.entityPlayer.getGameProfile().getId(),
			event.entityPlayer.getCapability(StatesCapabilities.PLAYER, null));
		MessageSender.toWebhook(null, event.entityPlayer.getGameProfile().getName() + " respawned.");
	}
	
	@SubscribeEvent
	public static void onRickClickBlock(PlayerInteractEvent event){
		if(event.world.isRemote || event.entityPlayer.dimension != 0 || event.action != PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
			return;
		}
		Block state = event.world.getBlock(event.x,event.y,event.z);
		if(state instanceof BlockSign){
			//IBlockState state = event.world.getBlockState(event.getPos());
			TileEntitySign te_sign = (TileEntitySign)event.world.getTileEntity(event.x,event.y,event.z);
			if(te_sign == null || te_sign.signText == null || te_sign.signText[0] == null){
				return;
			}
			Chunk chunk = StateUtil.getChunk(event.x,event.z);
			SignTileEntityCapability cap = te_sign.getCapability(StatesCapabilities.SIGN_TE, null);
			if(te_sign.signText[0].equalsIgnoreCase("[States]")){
				if(cap != null){ cap.setup(chunk); }
			}
			else if(cap != null && cap.isStatesSign()){
				cap.onPlayerInteract(chunk, event.entityPlayer);
			}
			else return;
		}
		else if(state instanceof BlockChest || state instanceof BlockFurnace
				|| state instanceof BlockHopper || state instanceof BlockDispenser
				|| state instanceof BlockDropper || state instanceof BlockLever
				|| state instanceof BlockButton || state instanceof BlockPressurePlate
				|| state instanceof BlockRedstoneRepeater || state instanceof BlockRedstoneComparator){
			if(!checkAccess(event.world, event.x,event.y,event.z, event.entityPlayer)){
				Print.chat(event.entityPlayer, "No permission to interact with these blocks here.");
				event.setCanceled(true);
				return;
			}
		}
		else return;
	}
	
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event){
		if(event.getPlayer().dimension != 0){ return; }
		if(!checkAccess(event.world, event.x,event.y,event.z, event.getPlayer())){
			event.getPlayer().addChatComponentMessage(new ChatComponentText(Formatter.format( "No permission to break blocks here.")));
			event.setCanceled(true);
		}
		return;
	}

	@SubscribeEvent
	public static void onBlockPlace(BlockEvent.PlaceEvent event){
		if(event.player.dimension != 0){ return; }
		if(!checkAccess(event.world, event.x,event.y,event.z, event.player)){
			event.player.addChatComponentMessage(new ChatComponentText(Formatter.format("No permission to place blocks here.")));
			event.setCanceled(true);
		}
		return;
	}
	
	public static boolean checkAccess(World world, int posX, int posY, int posZ, EntityPlayer player){
		if(StateUtil.isAdmin(player)){ return true; }
		Chunk chunk = StateUtil.getChunk(posX,posZ);
		if(chunk.getDistrict().getId() < 0){
			if(chunk.getDistrict().getId() == -1){
				if(Config.ALLOW_WILDERNESS_ACCESS){
					chunk.setEdited(Time.getDate());
					return true;
				}
				return false;
			}
			else if(chunk.getDistrict().getId() == -2){
				if(chunk.getChanged() + Time.DAY_MS < Time.getDate()){
					chunk.setDistrict(StateUtil.getDistrict(-1));
					//TODO log
					chunk.save();
					Print.chat(player, "Updating chunk...");
						return false;
				}
				if(posY > Config.TRANSIT_ZONE_BOTTOM_LIMIT && posY < Config.TRANSIT_ZONE_TOP_LIMIT){
					chunk.setEdited(Time.getDate());
					return true;
				}
				return false;
			}
			else{
				Print.chat(player, "Unknown district type.");
				return false;
			}
		}
		if(hp(chunk, player)){
			chunk.setEdited(Time.getDate());
			return true;
		}
		return false;
	}
	
	private static boolean hp(Chunk chunk, EntityPlayer player){
		PlayerCapability cap = player.getCapability(StatesCapabilities.PLAYER, null);
		if(cap == null){
			if((cap = StateUtil.getPlayer(player.getUniqueID(), true)) == null){
				return false;
			}
		}
		else if(cap.getUUID() == null || cap.getMunicipality() == null){
			return false;
		}
		switch(chunk.getType()){
			case PRIVATE:{
				return chunk.getOwner().equals(cap.getUUIDAsString()) || chunk.getPlayerWhitelist().contains(cap.getUUID()) || cap.isMayorOf(chunk.getDistrict().getMunicipality()) || cap.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
			}
			case NORMAL:{
				return cap.getMunicipality().getId() == chunk.getDistrict().getMunicipality().getId();
			}
			case DISTRICT:{
				return cap.isDistrictManagerOf(chunk.getDistrict()) || cap.isMayorOf(chunk.getDistrict().getMunicipality()) || cap.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
			}
			case MUNICIPAL:{
				return cap.isMayorOf(chunk.getDistrict().getMunicipality()) || cap.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
			}
			case STATEOWNED:{
				return cap.isStateLeaderOf(chunk.getDistrict().getMunicipality().getState());
			}
			case COMPANY: return false;//TODO
			case PUBLIC: return true;
			default:{
				return false;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public static void onMessage(ServerChatEvent event){
		if(!Config.STATES_CHAT){
			MessageSender.toWebhook(event.player.getCapability(StatesCapabilities.PLAYER, null), event.message);
			return;
		}
		//event.setCanceled(true); Static.getServer().addScheduledTask(() -> { Sender.sendAs(event.player, event.getMessage()); });
		PlayerCapability cap = event.player.getCapability(StatesCapabilities.PLAYER, null); ChatComponentTranslation com = event.component;
		com.getFormatArgs()[0] = new ChatComponentText(Formatter.format("&" + (StateUtil.isAdmin(event.player) ? "4" : "6") + "#&8] " + cap.getFormattedNickname() + "&0:"));
		com.getFormatArgs()[1] = new ChatComponentText(Formatter.format("&7" + ((IChatComponent)com.getFormatArgs()[1]).getUnformattedText()));
		event.component =(new ChatComponentTranslation("states.chat.text", com.getFormatArgs()));
		MessageSender.toWebhook(cap, event.message);
	}
	
	@SubscribeEvent
	public static void onTick(TickEvent.PlayerTickEvent event){
		if(event.player.worldObj.isRemote || event.player.dimension != 0){ return; }
		PlayerCapability player = event.player.getCapability(StatesCapabilities.PLAYER, null);
		if(player != null && Time.getDate() > player.getLastPositionUpdate()){
			player.setPositionUpdate(Time.getDate());
			player.setCurrenkChunk(StateUtil.getChunk(event.player));
			//
			if(player.getCurrentChunk() == null || player.getLastChunk() == null){
				return;
			}
			if(player.getCurrentChunk().getDistrict() != player.getLastChunk().getDistrict()){
				Chunk chunk = player.getCurrentChunk();
				sendLocationUpdate(event.player, chunk, chunk.getDistrict().getMunicipality().getState().getName(), chunk.getDistrict().getMunicipality().getName(), chunk.getDistrict().getName(), 0);
			}
		}
	}
	
	public static void sendLocationUpdate(EntityPlayer player, Chunk chunk, String line0, String line1, String line2, int time){
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("target_listener", "states:gui");
		nbt.setString("task", "show_location_update");
		writeIcon(nbt, chunk == null ? "" : chunk.getDistrict().getMunicipality().getState().getIcon(), 0, "red");
		writeIcon(nbt, chunk == null ? "" : chunk.getDistrict().getMunicipality().getIcon(), 1, "green");
		writeIcon(nbt, chunk == null ? "" : chunk.getDistrict().getIcon(), 2, "blue");
		nbt.setString("line0", line0 == null ? " " : line0);
		nbt.setString("line1", line1 == null ? " " : line1);
		nbt.setString("line2", line2 == null ? " " : line2);
		if(time > 0){ nbt.setInteger("time", time); }
		PacketHandler.getInstance().sendTo(new PacketNBTTagCompound(nbt), (EntityPlayerMP)player);
	}
	
	private static final List<String> colours = Arrays.asList(new String[]{"green", "yellow", "red", "blue"});
	
	private static final void writeIcon(NBTTagCompound compound, String icon, int id, String color){
		if(icon != null && !icon.equals("")){
			if(colours.contains(icon)){
				compound.setString("color_" + id, icon);
			}
			else{
				compound.setString("icon_" + id, icon);
			}
		}
		else if(color == null){
			compound.setInteger("x_" + id, 64);
			compound.setInteger("y_" + id, 224);
		}
		else{
			compound.setString("color_" + id, color);
		}
	}

}
