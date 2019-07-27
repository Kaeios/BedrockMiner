package me.rellynn.plugins.bedrockminer;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BedrockTool {

    private final Material material;
    private final String name;
    private final List<String> lore;
    private final Map<Enchantment, Integer> enchants;

    public BedrockTool(Material material, String name, List<String> lore, Map<Enchantment, Integer> enchants) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.enchants = enchants;

        if(lore == null) lore = Collections.emptyList();
    }

    public boolean matchItem(final ItemStack item) {
        if (!item.getType().equals(material)) return false;
        if (!item.getItemMeta().getDisplayName().equals(name)) return false;
        if (!lore.isEmpty() && !item.getItemMeta().hasLore()) return false;
        if (!lore.isEmpty() && !item.getItemMeta().getLore().equals(lore)) return false;
        for (final Enchantment enchant : enchants.keySet()) {
            if (item.getEnchantmentLevel(enchant) < enchants.get(enchant)) return false;
        }
        return true;
    }

}
