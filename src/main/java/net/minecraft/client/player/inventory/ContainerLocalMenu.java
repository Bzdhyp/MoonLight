package net.minecraft.client.player.inventory;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.ILockableContainer;
import net.minecraft.world.LockCode;

public class ContainerLocalMenu extends InventoryBasic implements ILockableContainer
{
    private final String guiID;
    private final Int2IntMap field_174895_b = new Int2IntOpenHashMap();
    public boolean realChest;

    public ContainerLocalMenu(String id, IChatComponent title, int slotCount)
    {
        super(title, slotCount);
        this.guiID = id;
        this.realChest = title.toString().contains("container.chest");
    }

    public int getField(int id)
    {
        return this.field_174895_b.getOrDefault(id, 0);
    }

    public void setField(int id, int value)
    {
        this.field_174895_b.put(id, value);
    }

    public int getFieldCount()
    {
        return this.field_174895_b.size();
    }

    public boolean isLocked()
    {
        return false;
    }

    public void setLockCode(LockCode code)
    {
    }

    public LockCode getLockCode()
    {
        return LockCode.EMPTY_CODE;
    }

    public String getGuiID()
    {
        return this.guiID;
    }

    public Container createContainer(InventoryPlayer playerInventory, EntityPlayer playerIn)
    {
        throw new UnsupportedOperationException();
    }
}
