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

public class SellSharesCommand implements CommandExecutor {
    private final StockManager stockManager;
    private final StockExchangePlugin plugin;

    public SellSharesCommand(StockManager stockManager, StockExchangePlugin plugin) {
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
            player.sendMessage(ChatColor.RED + "Usage: /sellshares <amount>");
            return true;
        }
        int amount;
        try {
            amount = Integer.parseInt(args[0]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be positive!");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Invalid amount specified!");
            return true;
        }

        StockSign stockSign = plugin.getLastViewedSellSign().get(player.getUniqueId());
        if (stockSign == null) {
            player.sendMessage(ChatColor.RED + "You must look at a sell sign first!");
            return true;
        }
        String symbol = stockSign.getStockSymbol();

        if (stockManager.sellStock(player, symbol, amount)) {
            Stock stock = stockManager.getStocks().get(symbol);
            double price = stock != null ? stock.getCurrentPrice() : 0.0;
            double totalValue = price * amount;

            // Give receipt
            ItemStack receipt = new ItemStack(Material.PAPER, 1);
            ItemMeta meta = receipt.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Stock Sale Receipt");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.YELLOW + "Stock: " + symbol);
            lore.add(ChatColor.YELLOW + "Shares sold: " + amount);
            lore.add(ChatColor.YELLOW + "Price per share: " + String.format("%.2f", price));
            lore.add(ChatColor.YELLOW + "Total value: " + String.format("%.2f", totalValue));
            lore.add(ChatColor.GRAY + "Date: " + java.time.LocalDateTime.now().withNano(0));
            meta.setLore(lore);
            receipt.setItemMeta(meta);
            player.getInventory().addItem(receipt);

            player.sendMessage(ChatColor.GREEN + "Successfully sold " + amount + " shares of " + symbol + " at $" + String.format("%.2f", price) + " each.");
            player.sendMessage(ChatColor.GRAY + "Current balance: " + stockManager.getPlayerBalance(player));
        } else {
            player.sendMessage(ChatColor.RED + "Failed to sell shares. Make sure you own enough shares!");
        }

        return true;
    }
} 