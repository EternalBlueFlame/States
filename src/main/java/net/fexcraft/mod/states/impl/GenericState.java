package net.fexcraft.mod.states.impl;

import java.io.File;
import java.util.List;
import java.util.UUID;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.mod.fsmm.api.Account;
import net.fexcraft.mod.fsmm.api.Bank;
import net.fexcraft.mod.fsmm.util.DataManager;
import net.fexcraft.mod.lib.fcl.ArrayList;
import net.fexcraft.mod.lib.fcl.JsonUtil;
import net.fexcraft.mod.states.States;
import net.fexcraft.mod.states.api.State;
import net.fexcraft.mod.states.util.StateUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

public class GenericState implements State {
	
	private int id, capital;
	private String name, color, icon;
	private long created, changed, price;
	private UUID creator, leader;
	private Account account;
	private ArrayList<Integer> neighbors, municipalities, blacklist;
	private ArrayList<UUID> council;
	private byte chunktaxpercent, citizentaxpercent;
	private BlockPos mailbox;

	public GenericState(int value){
		id = value;
		JsonObject obj = StateUtil.getStateJson(value).getAsJsonObject();
		name = JsonUtil.getIfExists(obj, "name", "Unnamed State");
		created = JsonUtil.getIfExists(obj, "created", Time.getDate()).longValue();
		changed = JsonUtil.getIfExists(obj, "changed", Time.getDate()).longValue();
		creator = obj.has("creator") ? UUID.fromString(obj.get("creator").getAsString()) : UUID.fromString(States.CONSOLE_UUID);
		leader = obj.has("leader") ? UUID.fromString(obj.get("leader").getAsString()) : null;
		account = DataManager.getAccount("state:" + id, false, true);
		capital = JsonUtil.getIfExists(obj, "capital", -1).intValue();
		neighbors = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "neighbors", new JsonArray()).getAsJsonArray());
		municipalities = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "municipalities", new JsonArray()).getAsJsonArray());
		council = JsonUtil.jsonArrayToUUIDArray(JsonUtil.getIfExists(obj, "council", new JsonArray()).getAsJsonArray());
		color = JsonUtil.getIfExists(obj, "color", "#ffffff");
		blacklist = JsonUtil.jsonArrayToIntegerArray(JsonUtil.getIfExists(obj, "blacklist", new JsonArray()).getAsJsonArray());
		price = JsonUtil.getIfExists(obj, "price", 0).longValue();
		icon = JsonUtil.getIfExists(obj, "icon", States.DEFAULT_ICON);
		chunktaxpercent = JsonUtil.getIfExists(obj, "chunk_tax_percent", 0).byteValue();
		citizentaxpercent = JsonUtil.getIfExists(obj, "citizen_tax_percent", 0).byteValue();
		mailbox = obj.has("mailbox") ? BlockPos.fromLong(obj.get("mailbox").getAsLong()) : null;
	}

	@Override
	public JsonObject toJsonObject(){
		JsonObject obj = new JsonObject();
		obj.addProperty("id", id);
		obj.addProperty("name", name);
		obj.addProperty("created", created);
		obj.addProperty("creator", creator.toString());
		obj.addProperty("changed", changed);
		if(!(leader == null)){ obj.addProperty("leader", leader.toString()); }
		obj.add("neighbors", JsonUtil.getArrayFromIntegerList(neighbors));
		obj.add("municipalities", JsonUtil.getArrayFromIntegerList(municipalities));
		obj.addProperty("capital", capital);
		obj.add("council", JsonUtil.getArrayFromUUIDList(council));
		obj.addProperty("balance", account.getBalance());
		obj.addProperty("color", color);
		obj.add("blacklist", JsonUtil.getArrayFromIntegerList(blacklist));
		obj.addProperty("price", price);
		if(icon != null){ obj.addProperty("icon", icon); }
		if(chunktaxpercent > 0){
			obj.addProperty("chunk_tax_percent", chunktaxpercent);
		}
		if(citizentaxpercent > 0){
			obj.addProperty("citizen_tax_percent", citizentaxpercent);
		}
		if(mailbox != null) obj.addProperty("mailbox", mailbox.toLong());
		return obj;
	}

	@Override
	public void save(){
		JsonObject obj = toJsonObject();
		obj.addProperty("last_save", Time.getDate());
		File file = getStateFile();
		if(!file.getParentFile().exists()){ file.getParentFile().mkdirs(); }
		JsonUtil.write(file, obj);
	}

	@Override
	public int getId(){
		return id;
	}

	@Override
	public String getName(){
		return name;
	}

	@Override
	public void setName(String new_name){
		name = new_name;
	}

	@Override
	public boolean isUnionCapital(){
		return false;//TODO
	}

	@Override
	public void setChanged(long new_change){
		changed = new_change;
	}

	@Override
	public List<Integer> getMunicipalities(){
		return municipalities;
	}

	@Override
	public List<Integer> getNeighbors(){
		return neighbors;
	}

	@Override
	public long getCreated(){
		return created;
	}

	@Override
	public UUID getCreator(){
		return creator;
	}

	public void setCreator(UUID uuid){
		creator = uuid;
	}

	@Override
	public long getChanged(){
		return changed;
	}

	@Override
	public Account getAccount(){
		return account;
	}

	@Override
	public UUID getLeader(){
		return leader;
	}

	@Override
	public void setLeader(UUID uuid){
		leader = uuid;
	}

	@Override
	public List<UUID> getCouncil(){
		return council;
	}

	@Override
	public int getCapitalId(){
		return capital;
	}

	@Override
	public void setCapitalId(int id){
		capital = id;
	}

	@Override
	public String getColor(){
		return color;
	}

	@Override
	public void setColor(String newcolor){
		color = newcolor;
	}

	@Override
	public List<Integer> getBlacklist(){
		return blacklist;
	}

	@Override
	public long getPrice(){
		return price;
	}

	@Override
	public void setPrice(long new_price){
		price = new_price;
	}

	@Override
	public String getIcon(){
		return icon;
	}

	@Override
	public void setIcon(String url){
		icon = url;
	}

	@Override
	public byte getChunkTaxPercentage(){
		return chunktaxpercent;
	}

	@Override
	public void setChunkTaxPercentage(byte newtax){
		chunktaxpercent = newtax;
	}

	@Override
	public byte getCitizenTaxPercentage(){
		return citizentaxpercent;
	}

	@Override
	public void setCitizenTaxPercentage(byte newtax){
		citizentaxpercent = newtax;
	}

	@Override
	public void unload(){
		DataManager.unloadAccount(account);
	}
	
	@Override
	public void finalize(){ unload(); }

	@Override
	public Bank getBank(){
		return DataManager.getBank(account.getBankId(), true, true);
	}

	@Override
	public BlockPos getMailbox(){
		return mailbox;
	}

	@Override
	public void setMailbox(BlockPos pos){
		this.mailbox = pos;
	}

	@Override
	public void setMailbox(TileEntity pos){
		this.mailbox = new BlockPos(pos);
	}
}
