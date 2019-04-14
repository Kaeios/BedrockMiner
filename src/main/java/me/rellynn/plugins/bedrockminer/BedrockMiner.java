package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by gwennaelguich on 12/03/2017.
 * Edited by Kaeios on 14/03/2019
 */
public final class BedrockMiner extends JavaPlugin {

    long baseTime;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        baseTime = getConfig().getLong("base-time", 200);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }
}
