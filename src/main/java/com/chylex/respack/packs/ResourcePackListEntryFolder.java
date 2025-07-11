package com.chylex.respack.packs;

import com.chylex.respack.gui.GuiCustomResourcePacks;
import com.chylex.respack.gui.GuiUtils;
import net.minecraft.util.ResourceLocation;

import java.io.File;

public class ResourcePackListEntryFolder extends ResourcePackListEntryCustom{
	private static final ResourceLocation folderResource = new ResourceLocation("betterresourcepacks:textures/gui/folder.png"); // http://www.iconspedia.com/icon/folion-icon-27237.html

	private final GuiCustomResourcePacks ownerScreen;
	
	public final File folder;
	public final String folderName;
	public final boolean isUp;
	
	public ResourcePackListEntryFolder(GuiCustomResourcePacks ownerScreen, File folder){
		super(ownerScreen);
		this.ownerScreen = ownerScreen;
		this.folder = folder;
		this.folderName = folder.getName();
		this.isUp = false;
	}
	
	public ResourcePackListEntryFolder(GuiCustomResourcePacks ownerScreen, File folder, boolean isUp){
		super(ownerScreen);
		this.ownerScreen = ownerScreen;
		this.folder = folder;
		this.folderName = "..";
		this.isUp = isUp;
	}

	@Override
	public void func_148313_c(){
		mc.getTextureManager().bindTexture(folderResource);
	}
	
	@Override
	public String func_148312_b(){
		return folderName;
	}
	
	@Override
	public String func_148311_a(){
		return isUp ? "(Back)" : "(Folder)";
	}
	
	@Override
	public boolean mousePressed(int p_148278_1_, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_){
		ownerScreen.moveToFolder(folder);
		return true;
	}
	
	@Override
	public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected){
		GuiUtils.renderFolderEntry(this,x,y,isSelected);
	}
}
