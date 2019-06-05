package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gwennaelguich on 12/03/2017.
 * Edited by Kaeios on 14/03/2019
 */
public final class PacketListener extends PacketAdapter {

    private final BedrockMiner plugin;
    private final Map<Player, Integer> players;

    public PacketListener(final BedrockMiner plugin) {
        super(plugin, PacketType.Play.Client.BLOCK_DIG);
        this.plugin = plugin;
        this.players = new HashMap<>();
    }

    private void stopDigging(final BlockPosition position, final Player player) {
        if(!players.containsKey(player)) return;
        Bukkit.getScheduler().cancelTask(players.remove(player));
        // This runnable is useful to fix a weird bug with Bungeecord
        new BukkitRunnable() {
            @Override
            public void run() {
                // Stop the break animation (-1 is the default stage of the animation)
                PacketUtils.broadcastBlockBreakAnimationPacket(position, -1);
            }
        }.runTaskLater(plugin, 1);
    }

    private void breakBlock(final Block block, final BlockPosition position, final Player player) {
        final BlockBreakEvent breakEvt = new BlockBreakEvent(block, player);
        // Use a block break event to be compatible with protection plugins
        Bukkit.getPluginManager().callEvent(breakEvt);
        if(breakEvt.isCancelled()) return;


        if(plugin.getConfig().getBoolean("drop-bedrock", false))
            block.getWorld().dropItemNaturally(block.getLocation().add(0.5, 0.5, 0.5), new ItemStack(block.getType(), 1));

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
                // Natural bedrock mining protection
                if (position.getY() < plugin.getConfig().getInt("protection-height", 5) || (player.getWorld().getEnvironment() == World.Environment.NETHER && position.getY() > 123)) return;
                final Location location = position.toLocation(player.getWorld());
                // Make sure chunk is loaded
                if(!location.getWorld().isChunkLoaded(location.getBlockX() >> 4, location.getBlockY() >> 4)) return;
                final Material blockType = location.getBlock().getType();
                // Check if block is breakable & is player have permission
                if (!plugin.getConfig().isInt("break-blocks." + blockType.toString())) return;
                if (!player.hasPermission("bedrockminer."+ blockType.toString().toLowerCase())) return;

                // Create scheduler for animation
                players.put(player, new BukkitRunnable() {
                    int ticks = 0;
                    @Override
                    public void run() {
                        // If player disconnect from the server, stop animation & scheduler
                        if (!player.isOnline()) {
                            stopDigging(position, player);
                            return;
                        }

                        // Check if item used to break blocks is whitelisted
                        final ItemStack inHand = player.getItemInHand();
                        if (!plugin.getConfig().getString("tool.type").contains(inHand.getType().toString())) return;

                        // Check silk touch level
                        final int silkLevel = plugin.getConfig().getInt("tool.silk-level");
                        if(inHand.getItemMeta().getEnchantLevel(Enchantment.SILK_TOUCH) < silkLevel) return;

                        ticks += 5;
                        // Update animation
                        int stage;
                        final Block block = location.getBlock();
                        final long ticksPerStage = Math.round(plugin.getConfig().getInt("break-blocks."+ block.getType().toString(), 200) / Math.pow(1.3, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED)) / 9);
                        if (plugin.getConfig().isInt("break-blocks."+ blockType.toString()) && ticksPerStage != 0 && (stage = (int) (ticks / ticksPerStage)) <= 9) {
                            PacketUtils.broadcastBlockBreakAnimationPacket(position, stage);
                        } else {
                            stopDigging(position, player);
                            if (plugin.getConfig().isInt("break-blocks."+ blockType.toString())) breakBlock(block, position, player);
                        }
                    }
                }.runTaskTimer(plugin, 0, 5).getTaskId());
                break;
        }
    }
}
