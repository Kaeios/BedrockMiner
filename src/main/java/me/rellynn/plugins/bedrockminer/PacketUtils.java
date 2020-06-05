package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;

/**
 * Created by gwennaelguich on 12/03/2017.
 * Edited by Kaeios on 5/28/2019
 */
public abstract class PacketUtils {

    /**
     * display break animation to everyone
     * @param position position of the block
     * @param stage stage of the animation
     */
    public static void broadcastBlockBreakAnimationPacket(final BlockPosition position, final int stage) {
        final PacketContainer packet = ProtocolLibrary.getProtocolManager().createPacket(PacketType.Play.Server.BLOCK_BREAK_ANIMATION);
        packet.getIntegers()
                .write(0, 0)
                .write(1, stage);
        packet.getBlockPositionModifier().write(0, position);
        ProtocolLibrary.getProtocolManager().broadcastServerPacket(packet);
    }

}
