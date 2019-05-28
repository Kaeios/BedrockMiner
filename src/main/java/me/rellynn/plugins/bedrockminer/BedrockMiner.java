package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

/**
 * Created by gwennaelguich on 12/03/2017.
 * Edited by Kaeios on 14/03/2019
 */
public final class BedrockMiner extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        if(getConfig().getDouble("config") == 1.0){
            getConfig().set("config", 1.1);
            getConfig().set("drop-bedrock", false);
        }
        if(getConfig().getDouble("config") == 1.1){
            getConfig().set("config", 1.2);
            getConfig().set("protection-height", 5);
        }
        if(getConfig().getDouble("config") == 1.2){
            getConfig().set("config", 1.3);
            getConfig().set("break-blocks.BEDROCK", getConfig().getInt("base-time", 200));
        }
        if(getConfig().getDouble("config") == 1.3){
            getConfig().set("config", 1.4);
            getConfig().set("tool.type", Collections.singletonList(getConfig().getString("tool.type")));
        }
        saveConfig();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }
}
