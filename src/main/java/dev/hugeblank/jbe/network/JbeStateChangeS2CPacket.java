package dev.hugeblank.jbe.network;

import dev.hugeblank.jbe.MainInit;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public class JbeStateChangeS2CPacket implements CustomPayload {
    public static final CustomPayload.Id<JbeStateChangeS2CPacket> PACKET_ID = new CustomPayload.Id<>(MainInit.id("state_change"));
    public static final PacketCodec<PacketByteBuf, JbeStateChangeS2CPacket> PACKET_CODEC = PacketCodec.of(JbeStateChangeS2CPacket::write, JbeStateChangeS2CPacket::new);

    public static final int ALLOW_ICE_BOAT_SPEED = 0;
    public static final int HORSE_STAMINA = 1;

    private final int reason;
    private final float value;

    public JbeStateChangeS2CPacket(int reason, float value) {
        this.reason = reason;
        this.value = value;
    }

    protected JbeStateChangeS2CPacket(PacketByteBuf buf) {
        this.reason = buf.readUnsignedByte();
        this.value = buf.readFloat();
    }

    public int getReason() {
        return reason;
    }

    public float getValue() {
        return value;
    }

    public void write(PacketByteBuf buf) {
        buf.writeByte(this.reason);
        buf.writeFloat(this.value);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }
}
