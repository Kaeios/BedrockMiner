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
        if(getConfig().getDouble("config") == 1.0){
            getConfig().set("config", 1.1);
            getConfig().set("drop-bedrock", false);
            saveConfig();
        }
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }
}
