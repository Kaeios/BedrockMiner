package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.ProtocolLibrary;
import me.rellynn.plugins.bedrockminer.commands.BedrockCommand;
import me.rellynn.plugins.bedrockminer.listeners.PacketListener;
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
    private boolean spigot = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        upgradeConfig();
        loadTools();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
        getCommand("bedrockminer").setExecutor(new BedrockCommand(this));
        spigot = getServer().getVersion().contains("Spigot");
    }

    @Override
    public void reloadConfig(){
        super.reloadConfig();
        tools.clear();
        loadTools();
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
                getConfig().set("tool."+ index +".enchantments."+ Enchantment.SILK_TOUCH.getName(), silkLevel);
            }
            getConfig().set("tool.type", null);
        }
        if(getConfig().getDouble("config") == 1.6){
            getConfig().set("config", 1.7);
            for(final String tool : getConfig().getConfigurationSection("tool").getKeys(false)){
                getConfig().set("tool."+ tool +".durability", -1);
                getConfig().set("tool."+ tool +".repairable", true);
            }
        }
        if(getConfig().getDouble("config") == 1.7){
            getConfig().set("config", 1.8);
            getConfig().set("regions-list-is-whitelist", true);
            getConfig().set("regions-list", Collections.singletonList("no-bedrock-break"));

            getConfig().set("default-world-settings.enabled", true);
            getConfig().set("default-world-settings.min-height", 5);
            getConfig().set("default-world-settings.max-height", 256);

            getConfig().set("worlds-settings.world_nether.enabled", true);
            getConfig().set("worlds-settings.world_nether.min-height", 5);
            getConfig().set("worlds-settings.world_nether.max-height", 123);
        }
        saveConfig();
    }

    public void loadTools(){
        final ConfigurationSection section = getConfig().getConfigurationSection("tool");
        section.getKeys(false).forEach(key ->{
            final Material material = Material.valueOf(section.getString(key+".tool"));
            final String name = section.getString(key+".name", "");
            final List<String> lore = section.getStringList(key+".lore");
            final int durability = section.getInt(key +".durability", -1);
            final boolean repairable = section.getBoolean(key + ".repairable", true);
            final Map<Enchantment, Integer> enchants = new HashMap<>();
            section.getConfigurationSection(key +".enchantments").getKeys(false).forEach(enchant ->{
                enchants.put(Enchantment.getByName(enchant.toUpperCase()), section.getInt(key +".enchantments."+ enchant));
            });
            tools.add(new BedrockTool(material, name, lore, enchants, (short) durability, repairable));
        });
    }

    public boolean isTool(final ItemStack item){
        for(BedrockTool tool : tools){
            if(tool.matchItem(item)) return true;
        }
        return false;
    }

    public List<BedrockTool> getTools(){
        return new ArrayList<>(tools);
    }

    public boolean isSpigot() {
        return spigot;
    }

}
