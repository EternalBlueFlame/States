package net.fexcraft.mod.states.objects;

import java.util.List;

import javax.annotation.Nullable;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.fexcraft.mod.lib.fcl.Formatter;
import net.fexcraft.mod.states.util.NumberUtil;
import net.minecraft.server.MinecraftServer;

import net.fexcraft.lib.common.math.Time;
import net.fexcraft.lib.mc.api.registry.fItem;
import net.fexcraft.lib.mc.utils.Static;
import net.fexcraft.mod.states.States;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.world.World;

@fItem(modid = States.MODID, name = "mail", variants = 5, custom_variants = { "empty", "expired", "private", "invite", "system" })
public class MailItem extends Item {
	
	public static MailItem INSTANCE;
	
	public MailItem(){
		this.setCreativeTab(CreativeTab.INSTANCE);
		this.setMaxStackSize(1); INSTANCE = this;
		//this.setContainerItem(this);
	}
	
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip){
    	//if(stack.getTagCompound() == null && this.getDamage(stack) > 1){this.setDamage(stack, 1); return; }
    	if(stack.getItemDamage() == 0){
        	tooltip.add(Formatter.format("&7Empty Mail."));
    	}
    	else if(stack.getItemDamage() == 1){
        	tooltip.add(Formatter.format("&cExpired mail."));
    	}
    	else{
    		if(stack.getTagCompound() == null) return;
        	NBTTagCompound compound = stack.getTagCompound();
        	tooltip.add(Formatter.format("&9Type: &7" + compound.getString("Type")));
        	tooltip.add(Formatter.format("&9Sender: &7" + Static.getPlayerNameByUUID(compound.getString("Sender"))));
        	tooltip.add(Formatter.format("&9Receiver: &7" + (NumberUtil.isCreatable(compound.getString("Receiver")) ? compound.getString("Receiver") : Static.getPlayerNameByUUID(compound.getString("Receiver").replace("player:", "")))));
        	if(compound.hasKey("Expiry")){
            	tooltip.add(Formatter.format("&9Expires: &7" + Time.getAsString(compound.getLong("Expiry"))));
        	}
        	if(compound.hasKey("StatesData")){
        		NBTTagCompound nbt = compound.getCompoundTag("StatesData");
        		tooltip.add(Formatter.format("&5InviteType: &7" + nbt.getString("type")));
        		tooltip.add(Formatter.format("&5At: &7" + Time.getAsString(compound.getLong("at"))));
        		tooltip.add(Formatter.format("&5Target ID: &7" + nbt.getInteger("id")));
        		if(nbt.hasKey("status")){
        			tooltip.add(Formatter.format("&aStatus: &7" + nbt.getString("status")));
        		}
        	}
    	}
    }
    
    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int xPos, int yPos, int zPos, int facing, float hitX, float hitY, float hitZ){
    	if(world.isRemote) return EnumActionResult.PASS;
    	if(stack.getItemDamage() > 1){ MinecraftServer.getServer().getCommandManager().executeCommand(player, "/mail read"); }
        return EnumActionResult.PASS;
    }
    
    @Override
    public void getSubItems(Item item, CreativeTabs tab, List items){
        if(this.getCreativeTab()!=tab){return;}
        for(int i = 0; i < this.getClass().getAnnotation(fItem.class).variants(); i++){
            items.add(new ItemStack(this, 1, i));
        }
    }
    
    @Override
    public String getUnlocalizedName(ItemStack stack){
    	int var = this.getClass().getAnnotation(fItem.class).variants();
    	if(stack.getItemDamage() < var && stack.getItemDamage() >= 0){
    		return this.getUnlocalizedName() + "_" + this.getClass().getAnnotation(fItem.class).custom_variants()[stack.getItemDamage()];
    	} else return this.getUnlocalizedName();
    }
	
}