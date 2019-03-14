package net.fexcraft.mod.states.util;


import cpw.mods.fml.client.IModGuiFactory;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;
import net.fexcraft.mod.states.States;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.List;
import java.util.Set;

public class GuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft mc_instance){
		//
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories(){
		return null;
	}
	
	public static class ConfigGui extends GuiConfig {

		public ConfigGui(GuiScreen parent){
			super(parent, getList(), States.MODID, true, true, "State Settings");
			titleLine2 = Config.getConfig().getConfigFile().getAbsolutePath();
		}
		
		public static List<IConfigElement> getList(){
			return Config.getList();
		}
		
	}

	@Override
	public boolean hasConfigGui(){
		return true;
	}

	@Override
	public GuiScreen createConfigGui(GuiScreen parentScreen){
		return new ConfigGui(parentScreen);
	}
	
}