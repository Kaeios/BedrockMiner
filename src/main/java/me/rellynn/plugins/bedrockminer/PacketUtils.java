package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import org.bukkit.Material;

/**
 * Created by gwennaelguich on 12/03/2017.
 */
public abstract class PacketUtils {
    private static int nextId = 11032000;

    public static void broadcastBlockBreakAnimationPacket(BlockPosition position, int stage) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers()
                .write(0, 0)
                .write(1, stage);
        packet.getBlockPositionModifier().write(0, position);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet);
    }

    public static void broadcastBlockBreakEffectPacket(BlockPosition position, Material type) {
        PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.WORLD_EVENT);
        packet.getIntegers()
                .write(0, 2001)
                .write(1, type.getId());
        packet.getBlockPositionModifier().write(0, position);
        packet.getBooleans().write(0, false);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet);
    }
}
