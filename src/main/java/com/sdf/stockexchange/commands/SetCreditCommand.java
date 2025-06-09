package com.sdf.stockexchange.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sdf.stockexchange.Stock;
import com.sdf.stockexchange.StockExchangePlugin;
import com.sdf.stockexchange.StockManager;
import com.sdf.stockexchange.StockSign;

public class SetCreditCommand implements CommandExecutor {
    private final StockManager stockManager;

    public SetCreditCommand(StockManager stockManager) {
        this.stockManager = stockManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Only allow console or players with permission
        if (!(sender.isOp() || sender.hasPermission("stockexchange.setcredit"))) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }
        if (args.length != 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /setcredit <player> <amount>");
            return true;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }
        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Invalid amount.");
            return true;
        }
        stockManager.setPlayerCredit(target, amount);
        sender.sendMessage(ChatColor.GREEN + "Set " + target.getName() + "'s credit to $" + String.format("%.2f", amount));
        target.sendMessage(ChatColor.YELLOW + "Your credit has been set to $" + String.format("%.2f", amount) + " by an admin.");
        return true;
    }
} 