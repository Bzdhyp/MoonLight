package net.minecraft.network.play.server;

import java.io.IOException;

import lombok.Getter;
import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.world.World;

public class S19PacketEntityStatus implements Packet<INetHandlerPlayClient>
{
    @Getter
    private int entityId;
    private byte logicOpcode;

    public S19PacketEntityStatus()
    {
    }

    public S19PacketEntityStatus(Entity entityIn, byte opCodeIn)
    {
        this.entityId = entityIn.getEntityId();
        this.logicOpcode = opCodeIn;
    }

    public void readPacketData(PacketBuffer buf) throws IOException
    {
        this.entityId = buf.readInt();
        this.logicOpcode = buf.readByte();
    }

    public void writePacketData(PacketBuffer buf) throws IOException
    {
        buf.writeInt(this.entityId);
        buf.writeByte(this.logicOpcode);
    }

    public void processPacket(INetHandlerPlayClient handler)
    {
        handler.handleEntityStatus(this);
    }

    public Entity getEntity(World worldIn)
    {
        return worldIn.getEntityByID(this.entityId);
    }

    public byte getOpCode()
    {
        return this.logicOpcode;
    }
}
