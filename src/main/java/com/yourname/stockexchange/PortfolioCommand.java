package com.yourname.stockexchange;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.Map;

public class PortfolioCommand implements CommandExecutor {
    private final StockManager stockManager;

    public PortfolioCommand(StockManager stockManager) {
        this.stockManager = stockManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        Player player = (Player) sender;
        Map<String, Integer> portfolio = stockManager.getPlayerPortfolio(player);
        if (portfolio.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You do not own any shares.");
            return true;
        }
        player.sendMessage(ChatColor.GOLD + "=== Your Portfolio ===");
        for (Map.Entry<String, Integer> entry : portfolio.entrySet()) {
            String symbol = entry.getKey();
            int shares = entry.getValue();
            if (shares <= 0) continue;
            double avgBuy = stockManager.getPlayerAverageBuyPrice(player, symbol);
            Stock stock = stockManager.getStocks().get(symbol);
            double currentPrice = stock != null ? stock.getCurrentPrice() : 0.0;
            double currentValue = shares * currentPrice;
            player.sendMessage(ChatColor.YELLOW + symbol + ChatColor.WHITE + ": " + shares + " shares | " +
                "Avg Buy: $" + String.format("%.2f", avgBuy) + " | " +
                "Current: $" + String.format("%.2f", currentPrice) + " | " +
                "Value: $" + String.format("%.2f", currentValue));
        }
        return true;
    }
} 