package net.fexcraft.mod.states.events;

import cpw.mods.fml.client.event.ConfigChangedEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.fexcraft.mod.fsmm.util.Config;
import net.fexcraft.mod.states.States;

public class ConfigEvents {
	
	@SubscribeEvent
	public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event){
		if(event.modID.equals(States.MODID)){
			Config.refresh();
			if(Config.getConfig().hasChanged()){
				Config.getConfig().save();
			}
		}
	}
	
}
