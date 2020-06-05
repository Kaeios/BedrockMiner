package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.ProtocolLibrary;
import me.rellynn.plugins.bedrockminer.commands.BedrockCommand;
import me.rellynn.plugins.bedrockminer.listeners.PacketListener;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

/**
 * Created by gwennaelguich on 12/03/2017.
 * Edited by Kaeios on 14/03/2019
 */
public final class BedrockMiner extends JavaPlugin {

    private final List<BedrockTool> tools = new ArrayList<>();
    private final List<Material> globalTools = new ArrayList<>();

    private final List<WorldSetting> worldSettings = new ArrayList<>();
    private boolean spigot = false;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        upgradeConfig();
        loadTools();
        loadWorldSettings();
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketListener(this));
        getCommand("bedrockminer").setExecutor(new BedrockCommand(this));
        spigot = getServer().getVersion().contains("Spigot");
    }

    private void loadWorldSettings() {
        final ConfigurationSection section = getConfig().getConfigurationSection("worlds-settings");
        section.getKeys(false).forEach(world ->{
            final String name = world;
            final boolean enabled = section.getBoolean(world + ".enabled", true);
            final int min = section.getInt(world + ".min-height", 5);
            final int max = section.getInt(world + ".max-height", 256);

            worldSettings.add(new WorldSetting(name, min, max, enabled));
        });
    }

    public void reloadPluginConfig(){
        reloadConfig();
        tools.clear();
        globalTools.clear();
        loadTools();
        worldSettings.clear();
        loadWorldSettings();
    }

    @Override
    public void onDisable() {
        ProtocolLibrary.getProtocolManager().removePacketListeners(this);
    }

    private void upgradeConfig(){
        if(getConfig().getDouble("config") == 1.8){
            getConfig().set("config", 1.9);
            getConfig().getConfigurationSection("tool").getKeys(false).forEach(key -> {
                if(getConfig().isSet("tool."+ key +".flags")) return;
                getConfig().set("tool."+ key +".flags", Collections.emptyList());
            });
            getConfig().set("global-tools", Collections.emptyList());
            saveConfig();
        }
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
            final List<ItemFlag> flags = new ArrayList<>();

            ConfigurationSection enchantSection = section.getConfigurationSection(key +".enchantments");

            if(enchantSection != null) {
                enchantSection.getKeys(false).forEach(enchant ->{
                    enchants.put(Enchantment.getByName(enchant.toUpperCase()), section.getInt(key +".enchantments."+ enchant));
                });
            }

            section.getStringList(key + ".flags").forEach(flagName -> flags.add(ItemFlag.valueOf(flagName.toUpperCase())));

            final int modelData = section.getInt(key +".modelData", 15912153); // 15912153 is just an arbitrary integer that is supposedly unique and won't interfere with other plugins

            tools.add(new BedrockTool(material, name, lore, enchants, (short) durability, repairable, modelData, flags));
        });

        getConfig().getStringList("global-tools").forEach(materialName -> globalTools.add(Material.valueOf(materialName.toUpperCase())));
    }

    public boolean isTool(final ItemStack item){
        if(item == null && globalTools.contains(Material.AIR)) return true;
        if(item == null) return false;
        if(globalTools.contains(item.getType())) return true;
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

    public List<WorldSetting> getWorldSettings() {
        return worldSettings;
    }

}
