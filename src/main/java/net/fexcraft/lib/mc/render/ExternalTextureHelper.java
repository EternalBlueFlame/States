package net.fexcraft.lib.mc.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ExternalTextureHelper {
    private static final Map<String, ResourceLocation> map = new HashMap<String, ResourceLocation>();

    public static ResourceLocation get(String s){
        if(map.containsKey(s)){ return map.get(s); }
        ResourceLocation texture = new ResourceLocation("fcl:remote/", s);
        ITextureObject object = Minecraft.getMinecraft().renderEngine.getTexture(texture);
        if(object == null){ File file = new File(s);
            ThreadDownloadImageData tdid = new ThreadDownloadImageData(file.exists() ? file : null, s, null, null);
            Minecraft.getMinecraft().renderEngine.loadTexture(texture, (object = tdid));
        } map.put(s, texture); return texture;
    }
}
