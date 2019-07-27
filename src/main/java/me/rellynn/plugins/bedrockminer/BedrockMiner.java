package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.ProtocolLibrary;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by gwennaelguich on 12/03/2017.
 * Edited by Kaeios on 14/03/2019
 */
public final class BedrockMiner extends JavaPlugin {

    private final List<BedrockTool> tools = new ArrayList<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        upgradeConfig();
        loadTools();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }

    private void upgradeConfig(){
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
        if(getConfig().getDouble("config") == 1.4){
            getConfig().set("config", 1.5);
            getConfig().getConfigurationSection("break-blocks").getKeys(false).forEach(block ->{
                final int duration = getConfig().getInt("break-blocks."+ block, 300);
                getConfig().set("break-blocks."+ block, null);
                getConfig().set("break-blocks."+ block +".duration", duration);
                getConfig().set("break-blocks."+ block +".durability", 1);
                getConfig().set("break-blocks."+ block +".drop", getConfig().getBoolean("drop-bedrock"));
            });
        }
        if(getConfig().getDouble("config") == 1.5){
            getConfig().set("config", 1.6);
            final int silkLevel = getConfig().getInt("tool.silk-level");
            getConfig().set("tool.silk-level", null);
            int index = 0;
            for(final String tool : getConfig().getStringList("tool.type")){
                index++;
                getConfig().set("tool."+ index +".tool", tool.toUpperCase());
                getConfig().set("tool." + index +".name", "");
                getConfig().set("tool." + index +".lore", Collections.emptyList());
                if(silkLevel <= 0) continue;
                getConfig().set("tool."+ index +".enchantments."+ Enchantment.SILK_TOUCH.toString(), silkLevel);
            }
            getConfig().set("tool.type", null);
        }
        saveConfig();
    }

    public void loadTools(){
        final ConfigurationSection section = getConfig().getConfigurationSection("tool");
        section.getKeys(false).forEach(key ->{
            final Material material = Material.valueOf(section.getString(key+".tool"));
            final String name = section.getString(key+".name");
            final List<String> lore = section.getStringList(key+".lore");
            final Map<Enchantment, Integer> enchants = new HashMap<>();
            section.getConfigurationSection(key +".enchantments").getKeys(false).forEach(enchant ->{
                enchants.put(Enchantment.getByName(enchant.toUpperCase()), section.getInt(key +".enchantments."+ enchant));
            });
            tools.add(new BedrockTool(material, name, lore, enchants));
        });
    }

    public boolean isTool(final ItemStack item){
        for(BedrockTool tool : tools){
            if(tool.matchItem(item)) return true;
        }
        return false;
    }

}
