package me.rellynn.plugins.bedrockminer.commands;

import me.rellynn.plugins.bedrockminer.BedrockMiner;
import me.rellynn.plugins.bedrockminer.BedrockTool;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;

public class BedrockCommand implements CommandExecutor {

    private final BedrockMiner plugin;

    public BedrockCommand(BedrockMiner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        if(args.length == 3){
            if(args[0].equals("give")) {
                final Player player = Bukkit.getPlayer(args[1]);
                if(player == null || !player.isOnline()) {
                    commandSender.sendMessage("§cThis player is not online.");
                } else {
                    try {
                        final int tool = Integer.valueOf(args[2]);
                        if(plugin.getTools().size() < tool) {
                            commandSender.sendMessage("§cInvalid tool id");
                        }else {
                            player.getInventory().addItem(plugin.getTools().get(tool).getItem());
                        }
                    }catch(NumberFormatException expected) {
                        commandSender.sendMessage("§cTool id must be a number");
                    }
                }
            } else {
                sendHelp(commandSender);
            }
        }else{
            sendHelp(commandSender);
        }

        return true;
    }

    private void sendHelp(final CommandSender commandSender){
        commandSender.sendMessage("§8[§cBedrockMiner§8] §eUse §c/bedrockminer give <player> <tool-id> §eto give a bedrock tool.");
        commandSender.sendMessage("§e");
        commandSender.sendMessage("§eList of tools:");
        int index = 0;
        for(BedrockTool tool : plugin.getTools()){
            final TextComponent text = new TextComponent("§8[§c"+ index++ +"§8] §e" + (tool.getName().equals("") ? tool.getMaterial().toString() : tool.getName()));
            if(!(commandSender instanceof Player) || !plugin.isSpigot()) {
                commandSender.sendMessage(text.getText());
                return;
            }
            final BaseComponent[] hoverText = new BaseComponent[4 + tool.getLore().size() + tool.getEnchants().size()];
            hoverText[0] = new TextComponent("§cName: §e"+ (tool.getName().equals("") ? "none" : tool.getName()) +"\n");
            hoverText[1] = new TextComponent("§cMaterial: §e"+ tool.getMaterial().toString() +"\n");
            hoverText[2] = new TextComponent("§cLore:\n");
            int iLore = 3;
            for(final String line : tool.getLore()){
                hoverText[iLore++] = new TextComponent("§7- §e"+line +"\n");
            }
            hoverText[iLore++] = new TextComponent("§cEnchantments:\n");
            for(Enchantment enchantment : tool.getEnchants().keySet()){
                hoverText[iLore++] = new TextComponent(" §e"+ enchantment.getName() + " " + tool.getEnchants().get(enchantment) +"\n");
            }
            text.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText));
            ((Player) commandSender).spigot().sendMessage(text);
        }
    }

}
