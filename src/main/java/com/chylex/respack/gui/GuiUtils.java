package com.chylex.respack.gui;

import com.chylex.respack.packs.ResourcePackListEntryFolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Util;
import org.lwjgl.opengl.GL11;
import org.lwjglx.Sys;

import java.io.File;
import java.net.URI;
import java.util.List;

public final class GuiUtils{
	private static final Minecraft mc = Minecraft.getMinecraft();
	
	public static void openFolder(File file){
		String s = file.getAbsolutePath();
		
		if (Util.getOSType() == Util.EnumOS.OSX){
			try{
				Runtime.getRuntime().exec(new String[]{ "/usr/bin/open", s });
				return;
			}catch(Exception ignored){}
		}
		else if (Util.getOSType() == Util.EnumOS.WINDOWS){
			String command = String.format("cmd.exe /C start \"Open file\" \"%s\"",s);
			
			try{
				Runtime.getRuntime().exec(command);
				return;
			}catch(Exception ignored){}
		}
		
		try{
			final Class cls = Class.forName("java.awt.Desktop");
			final Object desktop = cls.getMethod("getDesktop").invoke(null);
			
			cls.getMethod("browse",URI.class).invoke(desktop,file.toURI());
		}catch(Throwable t){
			Sys.openURL("file://"+s);
		}
	}
	
	public static void renderFolderEntry(ResourcePackListEntryFolder entry, int x, int y, boolean isSelected){
		entry.func_148313_c();
		GlStateManager.color(1F,1F,1F,1F);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GL11.GL_SRC_ALPHA,GL11.GL_ONE_MINUS_SRC_ALPHA);
		Gui.drawModalRectWithCustomSizedTexture(x,y,0F,0F,32,32,32F,32F);
		GlStateManager.disableBlend();
		
		int i2;

		if ((mc.gameSettings.touchscreen||isSelected)&&entry.func_148310_d()){
			Gui.drawRect(x,y,x+32,y+32,-1601138544);
			GlStateManager.color(1F,1F,1F,1F);
		}
		
		String s = entry.func_148312_b();
		i2 = mc.fontRendererObj.getStringWidth(s);
		
		if (i2>157){
			s = mc.fontRendererObj.trimStringToWidth(s,157-mc.fontRendererObj.getStringWidth("..."))+"...";
		}
		
		mc.fontRendererObj.drawStringWithShadow(s,x+32+2,y+1,16777215);
		List list = mc.fontRendererObj.listFormattedStringToWidth(entry.func_148311_a(),157);
		
		for(int j2 = 0; j2<2&&j2<list.size(); ++j2){
			mc.fontRendererObj.drawStringWithShadow((String)list.get(j2),x+32+2,y+12+10*j2,8421504);
		}
	}
}
