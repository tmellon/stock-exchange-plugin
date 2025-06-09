package com.sdf.stockexchange.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.sdf.stockexchange.Stock;
import com.sdf.stockexchange.StockExchangePlugin;
import com.sdf.stockexchange.StockManager;
import com.sdf.stockexchange.StockSign;

public class PriceCommand implements CommandExecutor {
    private final StockManager stockManager;

    public PriceCommand(StockManager stockManager) {
        this.stockManager = stockManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            // Show all stock prices
            sender.sendMessage(ChatColor.GOLD + "=== Current Stock Prices ===");
            for (Stock stock : stockManager.getStocks().values()) {
                sender.sendMessage(formatStockPrice(stock));
            }
            return true;
        }

        // Show specific stock price
        String symbol = args[0].toUpperCase();
        Stock stock = stockManager.getStocks().get(symbol);
        
        if (stock == null) {
            sender.sendMessage(ChatColor.RED + "Stock '" + symbol + "' not found!");
            return true;
        }

        sender.sendMessage(formatStockPrice(stock));
        return true;
    }

    private String formatStockPrice(Stock stock) {
        return ChatColor.YELLOW + stock.getSymbol() + ": " +
               ChatColor.GREEN + String.format("%.2f", stock.getCurrentPrice()) + " " +
               ChatColor.GRAY + "(Base: " + String.format("%.2f", stock.getBasePrice()) + ")";
    }
} 