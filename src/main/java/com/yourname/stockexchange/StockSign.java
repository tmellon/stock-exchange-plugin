package com.yourname.stockexchange;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

public class StockSign {
    private final StockManager stockManager;
    private final String stockSymbol;
    private final boolean isBuySign;

    public StockSign(StockManager stockManager, String stockSymbol, boolean isBuySign) {
        this.stockManager = stockManager;
        this.stockSymbol = stockSymbol;
        this.isBuySign = isBuySign;
    }

    public void updateSign(Sign sign) {
        Stock stock = stockManager.getStocks().get(stockSymbol);
        if (stock == null) return;

        sign.setLine(0, ChatColor.DARK_BLUE + "[Stock]");
        sign.setLine(1, ChatColor.GREEN + (isBuySign ? "BUY" : "SELL"));
        sign.setLine(2, ChatColor.YELLOW + stockSymbol);
        sign.setLine(3, ChatColor.GOLD + String.format("%.2f", stock.getCurrentPrice()));
        sign.setEditable(false);
        sign.update();
    }

    public boolean handleInteraction(Player player, Sign sign) {
        if (isBuySign) {
            return handleBuy(player, sign);
        } else {
            return handleSell(player, sign);
        }
    }

    private boolean handleBuy(Player player, Sign sign) {
        int diamonds = countItems(player, Material.DIAMOND);
        int emeralds = countItems(player, Material.EMERALD);
        double totalValue = (diamonds * 100.0) + (emeralds * 50.0);

        Stock stock = stockManager.getStocks().get(stockSymbol);
        if (stock == null) {
            player.sendMessage(ChatColor.RED + "DEBUG: Stock not found: " + stockSymbol);
            return false;
        }

        double sharePrice = stock.getCurrentPrice();
        int maxShares = (int) (totalValue / sharePrice);

        if (maxShares <= 0) {
            player.sendMessage(ChatColor.RED + "DEBUG: Not enough currency. Diamonds: " + diamonds + ", Emeralds: " + emeralds + ", Total value: " + totalValue + ", Share price: " + sharePrice);
            return false;
        }

        if (stockManager.buyStock(player, stockSymbol, maxShares)) {
            removeItems(player, Material.DIAMOND, diamonds);
            removeItems(player, Material.EMERALD, emeralds);

            double totalCost = maxShares * sharePrice;
            player.sendMessage(ChatColor.RED + "DEBUG: Total cost: " + totalCost + " and share price: " + sharePrice);
            String msg = ChatColor.GREEN + "Bought " + maxShares + " shares of " + stockSymbol + " at " + String.format("%.2f", sharePrice) + " each (Total: " + String.format("%.2f", totalCost) + ")";
            player.sendMessage(msg);
            player.sendMessage(ChatColor.GRAY + "Current balance: " + stockManager.getPlayerBalance(player));

            // Give receipt
            org.bukkit.inventory.ItemStack receipt = new org.bukkit.inventory.ItemStack(Material.PAPER, 1);
            org.bukkit.inventory.meta.ItemMeta meta = receipt.getItemMeta();
            meta.setDisplayName(ChatColor.GOLD + "Stock Purchase Receipt");
            java.util.List<String> lore = new java.util.ArrayList<>();
            lore.add(ChatColor.YELLOW + "Stock: " + stockSymbol);
            lore.add(ChatColor.YELLOW + "Shares: " + maxShares);
            lore.add(ChatColor.YELLOW + "Price per share: " + String.format("%.2f", sharePrice));
            lore.add(ChatColor.YELLOW + "Total cost: " + String.format("%.2f", totalCost));
            lore.add(ChatColor.GRAY + "Date: " + java.time.LocalDateTime.now().withNano(0));
            meta.setLore(lore);
            receipt.setItemMeta(meta);
            player.getInventory().addItem(receipt);
            return true;
        } else {
            player.sendMessage(ChatColor.RED + "DEBUG: buyStock() returned false.");
        }
        return false;
    }

    private boolean handleSell(Player player, Sign sign) {
        Map<String, Integer> portfolio = stockManager.getPlayerPortfolio(player);
        int shares = portfolio.getOrDefault(stockSymbol, 0);

        if (shares <= 0) {
            player.sendMessage(ChatColor.RED + "You don't own any shares of " + stockSymbol + "!");
            return false;
        }

        if (stockManager.sellStock(player, stockSymbol, shares)) {
            // Give currency based on current balance
            double price = stockManager.getStocks().get(stockSymbol).getCurrentPrice();
            double totalValue = price * shares;
            double balance = stockManager.getPlayerBalance(player);
            int diamonds = (int) (balance / 100.0);
            int emeralds = (int) ((balance % 100.0) / 50.0);

            player.getInventory().addItem(new ItemStack(Material.DIAMOND, diamonds));
            if (emeralds > 0) {
                player.getInventory().addItem(new ItemStack(Material.EMERALD, emeralds));
            }

            player.sendMessage(ChatColor.GREEN + "Successfully sold " + shares + " shares of " + stockSymbol + " at " + String.format("%.2f", price) + " each (Total: " + String.format("%.2f", totalValue) + ")");
            player.sendMessage(ChatColor.GRAY + "Current balance: " + stockManager.getPlayerBalance(player));
            return true;
        }
        return false;
    }

    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    item.setAmount(0);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }
                if (remaining <= 0) break;
            }
        }
    }

    public static boolean isValidStockSign(String[] lines) {
        if (lines.length < 4) return false;
        String line0 = org.bukkit.ChatColor.stripColor(lines[0]);
        String line1 = org.bukkit.ChatColor.stripColor(lines[1]);
        return line0.equalsIgnoreCase("[Stock]") &&
               (line1.equalsIgnoreCase("BUY") || line1.equalsIgnoreCase("SELL"));
    }

    public static StockSign createFromSign(Sign sign, StockManager stockManager) {
        String[] lines = sign.getLines();
        if (!isValidStockSign(lines)) return null;

        // Strip color codes for logic checks
        String line1 = org.bukkit.ChatColor.stripColor(lines[1]);
        String line2 = org.bukkit.ChatColor.stripColor(lines[2]);

        boolean isBuySign = line1.equalsIgnoreCase("BUY");
        String stockSymbol = line2.toUpperCase();

        return new StockSign(stockManager, stockSymbol, isBuySign);
    }

    public boolean isBuySign() {
        return isBuySign;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }
} 