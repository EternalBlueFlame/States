package net.fexcraft.mod.states.guis;

import net.fexcraft.lib.mc.network.packet.PacketNBTTagCompound;
import net.fexcraft.mod.fsmm.util.Print;
import net.fexcraft.mod.lib.fcl.PacketHandler;
import net.fexcraft.mod.states.util.ImageUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.lwjgl.opengl.GL11;

public class WelcomeGui extends GuiContainer {

	public static final  ResourceLocation texture = new ResourceLocation("states:textures/gui/welcome.png");
	private Button[] buttons = new Button[9];
	private ResourceLocation[] rss = new ResourceLocation[7 * 7];
	
	public WelcomeGui(EntityPlayer player, World world, int x, int y, int z){
		super(new PlaceholderContainer());
		xSize = 256; ySize = 200;
		//
		Chunk chunk = world.getChunkFromBlockCoords(x, z);
		int i = 0;
		for(int j = -3; j < 4; j++){
			for(int k = -3; k < 4; k++){
				rss[i++] = ImageUtil.getTempChunkImage(world, chunk.xPosition + j, chunk.zPosition + k);
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY){
		//
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks){
		this.mc.getTextureManager().bindTexture(texture);
		this.drawTexturedModalRect((this.width - xSize) / 2, (this.height - ySize) / 2, 0, 0, xSize, ySize);
		//
		for(Button button : buttons){
			button.drawButton(mc, mouseX, mouseY);
		}
		for(int i = 0; i < 7; i++){
			for(int j = 0; j < 7; j++){
				this.mc.getTextureManager().bindTexture(rss[(i * 7) + j]);
				//this.drawTexturedModalRect(this.guiLeft + 119 + (i * 18), this.guiTop + 62 + (j * 18), 0, 0, 16, 16);
				//
				int x = this.guiLeft + 119 + (i * 18);
				int y = this.guiTop + 62 + (j * 18);
				//
		        Tessellator tessellator = Tessellator.instance;
		        BufferBuilder bufferbuilder = tessellator.getBuffer();
		        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		        bufferbuilder.pos((double)(x + 0), (double)(y + 16), (double)this.zLevel).tex((double)((float)(0 + 0) * 0.00390625F), (double)((float)(0 + 256) * 0.00390625F)).endVertex();
		        bufferbuilder.pos((double)(x + 16), (double)(y + 16), (double)this.zLevel).tex((double)((float)(0 + 256) * 0.00390625F), (double)((float)(0 + 256) * 0.00390625F)).endVertex();
		        bufferbuilder.pos((double)(x + 16), (double)(y + 0), (double)this.zLevel).tex((double)((float)(0 + 256) * 0.00390625F), (double)((float)(0 + 0) * 0.00390625F)).endVertex();
		        bufferbuilder.pos((double)(x + 0), (double)(y + 0), (double)this.zLevel).tex((double)((float)(0 + 0) * 0.00390625F), (double)((float)(0 + 0) * 0.00390625F)).endVertex();
		        tessellator.draw();
			}
		}
	}
	
	@Override
	public void initGui(){
		super.initGui();
		buttonList.clear();
		for(int i = 0; i < 9; i++){
			buttonList.add(buttons[i] = new Button(i, this.guiLeft + 9, this.guiTop + 48 + (i * 16), 100, 14, " - - - - - "));
			buttons[i].enabled = buttons[i].visible = true;
		}
		buttons[0].displayString = "Area View";
		buttons[1].displayString = "Districts";
		buttons[2].displayString = "Municipalities";
		buttons[3].displayString = "States";
		buttons[4].displayString = "Unions";
		//buttons[5].displayString = "";
		buttons[6].displayString = "Companies";
		buttons[7].displayString = "Player Settings";
		//buttons[8].displayString = "";
	}
	
	@Override
    public void onGuiClosed(){
        super.onGuiClosed();
        for(ResourceLocation rs : rss){
        	mc.getTextureManager().deleteTexture(rs);
        }
    }
	
	@Override
	public void actionPerformed(GuiButton button){
		Print.debug(button.displayString, button.id+"");
		if(button.id >= 9){
			return;
		}
		NBTTagCompound compound = new NBTTagCompound();
		compound.setString("target_listener", "states:gui");
		compound.setInteger("from", 0);
		compound.setInteger("button", button.id);
		Print.debug(compound.toString());
		PacketHandler.getInstance().sendToServer(new PacketNBTTagCompound(compound));
	}
	
	public class Button extends GuiButton {
		
		public Button(int buttonId, int x, int y, int widthIn, int heightIn, String buttonText){
			super(buttonId, x, y, widthIn, heightIn, buttonText);
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY){
			if(!this.visible){ return; }
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
			if(this.displayString.length() > 0){
				int j = 14737632;
				if(packedFGColour != 0){ j = packedFGColour; }
	            else if(!this.enabled){ j = 10526880; }
	            else if(this.field_146123_n){ j = 16777120; }
	            this.drawCenteredString(mc.fontRenderer, this.displayString, this.xPosition + this.width / 2, this.yPosition + (this.height - 6) / 2, j);
			}
		}
		
	}

}
