package me.rellynn.plugins.bedrockminer;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BedrockTool {

    private final Material material;
    private final String name;
    private final List<String> lore;
    private final Map<Enchantment, Integer> enchants;

    public BedrockTool(Material material, String name, List<String> lore, Map<Enchantment, Integer> enchants) {
        if(lore == null) lore = Collections.emptyList();
        name = ChatColor.translateAlternateColorCodes('&', name);
        final List<String> newLore = new ArrayList<>();
        for(final String line : lore){
            newLore.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        this.material = material;
        this.name = name;
        this.lore = newLore;
        this.enchants = enchants;
    }

    public boolean matchItem(final ItemStack item) {
        if (!item.getType().equals(material)) return false;
        if (!name.equals("") && !item.getItemMeta().getDisplayName().equals(name)) return false;
        if (!lore.isEmpty() && !item.getItemMeta().hasLore()) return false;
        if (!lore.isEmpty() && !item.getItemMeta().getLore().equals(lore)) return false;
        for (final Enchantment enchant : enchants.keySet()) {
            if (item.getEnchantmentLevel(enchant) < enchants.get(enchant)) return false;
        }
        return true;
    }

    public Material getMaterial() {
        return material;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public Map<Enchantment, Integer> getEnchants() {
        return enchants;
    }

    public ItemStack getItem(){
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(lore);
        enchants.forEach((enchant, level) -> meta.addEnchant(enchant, level, true));
        item.setItemMeta(meta);
        return item;
    }

}
