package me.rellynn.plugins.bedrockminer.listeners;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import me.rellynn.plugins.bedrockminer.BedrockMiner;
import me.rellynn.plugins.bedrockminer.PacketUtils;
import me.rellynn.plugins.bedrockminer.WorldSetting;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.configuration.Configuration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gwennaelguich on 12/03/2017.
 * Edited by Kaeios on 14/03/2019
 */
public final class PacketListener extends PacketAdapter {

    private final BedrockMiner plugin;
    private final Map<Player, Integer> players = new HashMap<>();

    public PacketListener(final BedrockMiner plugin) {
        super(plugin, PacketType.Play.Client.BLOCK_DIG);
        this.plugin = plugin;
    }

    private void stopDigging(final BlockPosition position, final Player player) {
        if(!players.containsKey(player)) return;
        Bukkit.getScheduler().cancelTask(players.remove(player));
        // Stop the break animation (-1 is the default stage of the animation)
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()-> PacketUtils.broadcastBlockBreakAnimationPacket(position, -1), 1L);
    }

    private void breakBlock(final Block block, final BlockPosition position, final Player player) {
        final BlockBreakEvent breakEvt = new BlockBreakEvent(block, player);
        // Use a block break event to be compatible with protection plugins
        Bukkit.getPluginManager().callEvent(breakEvt);
        if(breakEvt.isCancelled()) return;
        final Material blockType = block.getType();
        final Configuration config = plugin.getConfig();

        // Drop block
        if(config.getBoolean("break-blocks."+ blockType.toString() + ".drop", false)){
            if(!block.getDrops().isEmpty()){
                block.breakNaturally(player.getItemInHand());
            } else {
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(block.getLocation(), new ItemStack(blockType, 1));
            }
        }

        // Damage pickaxe
        final int damage = config.getInt("break-blocks."+ blockType.toString() +".durability", 1);
        player.getItemInHand().setDurability((short) (player.getItemInHand().getDurability()+damage));
        // Check if pickaxe is broken
        if(player.getItemInHand().getDurability() >= player.getItemInHand().getType().getMaxDurability())
            player.setItemInHand(null);
        player.updateInventory();

        // Destroy block with setType() to prevent dropping
        block.setType(Material.AIR);
        PacketUtils.broadcastBlockBreakEffectPacket(position, block.getType());
    }

    public void onPacketReceiving(final PacketEvent evt) {
        final Player player = evt.getPlayer();
        // Creative player can break everything
        if (player.getGameMode().equals(GameMode.CREATIVE)) return;
        final BlockPosition position = evt.getPacket().getBlockPositionModifier().read(0);
        final PlayerDigType type = evt.getPacket().getPlayerDigTypes().read(0);
        switch (type) {
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                stopDigging(position, player);
                break;
            case START_DESTROY_BLOCK:
                final Location location = position.toLocation(player.getWorld());
                // Natural bedrock mining protection
                if (!isValidWorld(location)) return;
                // Make sure chunk is loaded
                if(!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockZ() >> 4)) return;
                final Material blockType = location.getBlock().getType();
                // Check if block is breakable & is player have permission
                if (!plugin.getConfig().isInt("break-blocks." + blockType.toString() +".duration")) return;
                if (!player.hasPermission("bedrockminer."+ blockType.toString().toLowerCase())) return;

                // Create scheduler for animation
                players.put(player, Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin ,new Runnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        // If player disconnect from the server, stop animation & scheduler
                        final ItemStack inHand = player.getItemInHand();

                        if (!player.isOnline() || !plugin.isTool(inHand)) {
                            stopDigging(position, player);
                            return;
                        }

                        ticks += 5;
                        // Update animation
                        int stage;
                        final Block block = location.getBlock();
                        final long ticksPerStage = Math.round(plugin.getConfig().getInt("break-blocks."+ block.getType().toString() + ".duration", 300) / Math.pow(1.3, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED)) / 9);
                        if (plugin.getConfig().isInt("break-blocks."+ blockType.toString() +".duration") && ticksPerStage != 0 && (stage = (int) (ticks / ticksPerStage)) <= 9) {
                            PacketUtils.broadcastBlockBreakAnimationPacket(position, stage);
                        } else {
                            stopDigging(position, player);
                            if (plugin.getConfig().isInt("break-blocks."+ blockType.toString() + ".duration")) breakBlock(block, position, player);
                        }
                    }
                },0L, 5L));
                break;
        }
    }

    private boolean isValidWorld(final Location location){
        boolean isValid = new WorldSetting(location.getWorld().getName(), plugin.getConfig().getInt("default-world-settings.min-height", 5), plugin.getConfig().getInt("default-world-settings.max-height", 256), plugin.getConfig().getBoolean("default-world-settings.enabled", true)).isValidLocation(location).equals(WorldSetting.LocationStatus.ALLOW);

        for (WorldSetting worldSetting : plugin.getWorldSettings()) {
            if(worldSetting.isValidLocation(location).equals(WorldSetting.LocationStatus.ALLOW)) return true;
            if(worldSetting.isValidLocation(location).equals(WorldSetting.LocationStatus.DENY)) return false;
        }

        return isValid;
    }

}
