package com.chylex.respack.packs;

import net.minecraft.client.resources.ResourcePackListEntry;
import net.minecraft.client.resources.ResourcePackListEntryFound;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class ResourcePackListProcessor{
	private static String name(ResourcePackListEntry entry){
		if (entry instanceof ResourcePackListEntryCustom)return ((ResourcePackListEntryCustom)entry).func_148312_b();
		else if (entry instanceof ResourcePackListEntryFound)return ((ResourcePackListEntryFound)entry).func_148318_i().getResourcePackName();
		else return "<INVALID>";
	}
	
	private static String nameSort(ResourcePackListEntry entry, boolean reverse){
		String pfx1 = !reverse ? "a" : "z";
		String pfx2 = !reverse ? "b" : "z";
		String pfx3 = !reverse ? "z" : "a";
		
		if (entry instanceof ResourcePackListEntryFolder folder){
            return folder.isUp ? pfx1+folder.folderName : pfx2+folder.folderName; // sort folders first
		}
		
		if (entry instanceof ResourcePackListEntryCustom)return pfx3+((ResourcePackListEntryCustom)entry).func_148312_b();
		else if (entry instanceof ResourcePackListEntryFound)return pfx3+((ResourcePackListEntryFound)entry).func_148318_i().getResourcePackName();
		else return pfx3+"<INVALID>";
	}
	
	private static String description(ResourcePackListEntry entry){
		if (entry instanceof ResourcePackListEntryCustom)return ((ResourcePackListEntryCustom)entry).func_148311_a();
		else if (entry instanceof ResourcePackListEntryFound)return ((ResourcePackListEntryFound)entry).func_148318_i().getTexturePackDescription();
		else return "<INVALID>";
	}
	
	public static final Comparator<ResourcePackListEntry> sortAZ = (entry1, entry2) -> String.CASE_INSENSITIVE_ORDER.compare(nameSort(entry1, false), nameSort(entry2, false));
	
	public static final Comparator<ResourcePackListEntry> sortZA = (entry1, entry2) -> -String.CASE_INSENSITIVE_ORDER.compare(nameSort(entry1, true), nameSort(entry2, true));
	
	private final List<ResourcePackListEntry> sourceList, targetList;
	
	private Comparator<ResourcePackListEntry> sorter;
	private Pattern textFilter;
	
	public ResourcePackListProcessor(List<ResourcePackListEntry> sourceList, List<ResourcePackListEntry> targetList){
		this.sourceList = sourceList;
		this.targetList = targetList;
		refresh();
	}
	
	public void setSorter(Comparator<ResourcePackListEntry> comparator){
		this.sorter = comparator;
		refresh();
	}
	
	public void setFilter(String text){
		if (text == null || text.isEmpty()){
			textFilter = null;
		}
		else{
			textFilter = Pattern.compile("\\Q"+text.replace("*","\\E.*\\Q")+"\\E",Pattern.CASE_INSENSITIVE);
		}
		
		refresh();
	}
	
	public void refresh(){
		targetList.clear();
		
		for(ResourcePackListEntry entry:sourceList){
			if (checkFilter(name(entry)) || checkFilter(description(entry))){
				targetList.add(entry);
			}
		}
		
		if (sorter != null){
			Collections.sort(targetList,sorter);
		}
	}
	
	private boolean checkFilter(String entryText){
		return textFilter == null || textFilter.matcher(entryText.toLowerCase(Locale.ENGLISH)).find();
	}
}
