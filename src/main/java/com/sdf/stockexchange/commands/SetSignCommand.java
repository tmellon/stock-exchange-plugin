package com.sdf.stockexchange.commands;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sdf.stockexchange.StockExchangePlugin;

public class SetSignCommand implements CommandExecutor {

    private final StockExchangePlugin plugin;

    public SetSignCommand(StockExchangePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /setsign <stockSymbol>");
            return true;
        }

        Player player = (Player) sender;
        Block block = player.getTargetBlockExact(5);

        if (block == null || !(block.getState() instanceof Sign)) {
            player.sendMessage(ChatColor.RED + "Please look at a sign!");
            return true;
        }

        String stockSymbol = args[0].toUpperCase();
        Sign sign = (Sign) block.getState();
        sign.setLine(0, ChatColor.BOLD + "[Buy]");
        sign.setLine(1, stockSymbol);
        sign.setLine(2, "Right click");
        sign.update();

        plugin.getSignRegistry().put(block.getLocation(), stockSymbol);
        player.sendMessage(ChatColor.GREEN + "Sign set for stock: " + stockSymbol);
        return true;
    }
}
