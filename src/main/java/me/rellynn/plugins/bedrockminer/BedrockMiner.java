package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gwennaelguich on 12/03/2017.
 */
public class BedrockMiner extends JavaPlugin {
    long baseTime;
    Set<Material> allowedTools;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        allowedTools = new HashSet<>();
        for (String name : getConfig().getStringList("allowed-tools")) {
            allowedTools.add(Material.valueOf(name));
        }
        baseTime = getConfig().getLong("base-time", 200);
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
    }
}
