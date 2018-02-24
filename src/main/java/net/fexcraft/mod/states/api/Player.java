package net.fexcraft.mod.states.api;

import java.util.UUID;

import javax.annotation.Nullable;

import net.fexcraft.mod.lib.perms.player.PlayerPerms;
import net.minecraft.command.ICommandSender;

public interface Player {
	
	public Municipality getMunicipality();
	
	public void setMunicipality(Municipality mun);
	
	public boolean isOnlinePlayer();
	
	public long getLastSave();
	
	@Nullable
	public String getRawNickname();
	
	public int getNicknameColor();
	
	public String getFormattedNickname(ICommandSender player);
	
	public PlayerPerms getPermissions();
	
	public UUID getUUID();

}
