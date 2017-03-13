package me.rellynn.plugins.bedrockminer;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers.PlayerDigType;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by gwennaelguich on 12/03/2017.
 */
public class PacketListener extends PacketAdapter {
    private BedrockMiner plugin;
    private Map<Player, Integer> players;

    PacketListener(BedrockMiner plugin) {
        super(plugin, PacketType.Play.Client.BLOCK_DIG);
        this.plugin = plugin;
        this.players = new WeakHashMap<>();
    }

    private void stopDigging(final BlockPosition position, final Player player) {
        if (players.containsKey(player)) {
            Bukkit.getScheduler().cancelTask(players.remove(player));
            new BukkitRunnable() {

                @Override
                public void run() {
                    PacketUtils.sendBlockBreakAnimationPacket(position, -1, player);
                }
            }.runTaskLater(plugin, 1);
        }
    }

    private void breakBlock(Block block, BlockPosition position, Player player) {
        BlockBreakEvent breakEvt = new BlockBreakEvent(block, player);
        Bukkit.getPluginManager().callEvent(breakEvt);
        if (!breakEvt.isCancelled()) {
            block.breakNaturally();
            PacketUtils.sendBlockBreakEffectPacket(position, player);
        }
    }

    public void onPacketReceiving(PacketEvent evt) {
        final Player player = evt.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        final BlockPosition position = evt.getPacket().getBlockPositionModifier().read(0);
        PlayerDigType type = evt.getPacket().getPlayerDigTypes().read(0);
        switch (type) {
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                stopDigging(position, player);
                break;
            case START_DESTROY_BLOCK:
                ItemStack inHand = player.getItemInHand();
                if (position.getY() < 5 || (player.getWorld().getEnvironment() == World.Environment.NETHER && position.getY() > 123)) {
                    return;
                } else if (position.toLocation(player.getWorld()).getBlock().getType() != Material.BEDROCK || !plugin.allowedTools.contains(inHand.getType())) {
                    return;
                }
                final long ticksPerStage = Math.round(plugin.baseTime / Math.pow(1.3, inHand.getEnchantmentLevel(Enchantment.DIG_SPEED)) / 9);
                players.put(player, new BukkitRunnable() {
                    int ticks = 0;

                    @Override
                    public void run() {
                        ticks += 5;
                        int stage;
                        Block block = position.toLocation(player.getWorld()).getBlock();
                        if (block.getType() == Material.BEDROCK && ticksPerStage != 0 && (stage = (int) (ticks / ticksPerStage)) <= 9) {
                            PacketUtils.sendBlockBreakAnimationPacket(position, stage, player);
                        } else {
                            stopDigging(position, player);
                            if (block.getType() == Material.BEDROCK)
                                breakBlock(block, position, player);
                        }
                    }
                }.runTaskTimer(plugin, 0, 5).getTaskId());
                break;
        }
    }
}
