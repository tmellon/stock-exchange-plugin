package com.yourname.stockexchange;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;

//figure out sign with chest inventory
//rework into spigot

public class BuySharesCommand implements CommandExecutor {
    private final StockManager stockManager;
    private final StockExchangePlugin plugin;

    public BuySharesCommand(StockManager stockManager, StockExchangePlugin plugin) {
        this.stockManager = stockManager;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return true;
        }
        Player player = (Player) sender;
        if (args.length != 1) {
            player.sendMessage(ChatColor.RED + "Usage: /buyshares <amount>");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount.");
            return true;
        }

        StockSign stockSign = plugin.getLastViewedBuySign().get(player.getUniqueId());
        if (stockSign == null) {
            player.sendMessage(ChatColor.RED + "You must look at a buy sign first!");
            return true;
        }

        String stockSymbol = stockSign.getStockSymbol();
        Stock stock = stockManager.getStocks().get(stockSymbol);
        if (stock == null) {
            player.sendMessage(ChatColor.RED + "Stock not found.");
            return true;
        }

        double price = stock.getCurrentPrice();
        double totalCost = price * amount;
        double credit = stockManager.getPlayerCredit(player);

        if (credit < totalCost) {
            player.sendMessage(ChatColor.RED + "Not enough credit! You have $" + String.format("%.2f", credit));
            return true;
        }

        // Update credit and portfolio
        stockManager.reducePlayerCredit(player, totalCost);
        stockManager.buyStock(player, stockSymbol, amount);

        // Give receipt
        ItemStack receipt = new ItemStack(Material.PAPER, 1);
        ItemMeta meta = receipt.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Stock Purchase Receipt");
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.YELLOW + "Stock: " + stockSymbol);
        lore.add(ChatColor.YELLOW + "Shares: " + amount);
        lore.add(ChatColor.YELLOW + "Price per share: " + String.format("%.2f", price));
        lore.add(ChatColor.YELLOW + "Total cost: " + String.format("%.2f", totalCost));
        lore.add(ChatColor.GRAY + "Date: " + java.time.LocalDateTime.now().withNano(0));
        meta.setLore(lore);
        receipt.setItemMeta(meta);
        player.getInventory().addItem(receipt);

        player.sendMessage(ChatColor.GREEN + "Bought " + amount + " shares of " + stockSymbol + " for $" + String.format("%.2f", totalCost));
        player.sendMessage(ChatColor.GRAY + "Remaining credit: $" + String.format("%.2f", stockManager.getPlayerCredit(player)));
        return true;
    }
} 