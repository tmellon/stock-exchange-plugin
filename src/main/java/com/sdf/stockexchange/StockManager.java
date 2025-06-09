package com.sdf.stockexchange;

import org.bukkit.entity.Player;
import java.util.*;

public class StockManager {
    private final Map<String, Stock> stocks;
    private final Map<UUID, Map<String, Integer>> playerPortfolios;
    private final Map<UUID, Double> playerBalances;
    private final Map<UUID, Double> playerCredit = new HashMap<>();
    private final Map<UUID, Map<String, Double>> playerAverageBuyPrice = new HashMap<>();

    public StockManager() {
        this.stocks = new HashMap<>();
        this.playerPortfolios = new HashMap<>();
        this.playerBalances = new HashMap<>();
        initializeStocks();
    }

    private void initializeStocks() {
        // Initialize some example stocks
        stocks.put("DIAMOND", new Stock("DIAMOND", 100.0, 10.0));
        stocks.put("GOLD", new Stock("GOLD", 50.0, 5.0));
        stocks.put("IRON", new Stock("IRON", 25.0, 2.5));
        stocks.put("TESLA", new Stock("TSLA", 50.0, 30.0));
    }

    public boolean buyStock(Player player, String symbol, int amount) {
        Stock stock = stocks.get(symbol.toUpperCase());
        if (stock == null) {
            player.sendMessage("Stock not found");
            return false;
        }
        UUID playerId = player.getUniqueId();

        // Initialize player data if not exists
        playerPortfolios.putIfAbsent(playerId, new HashMap<>());
        playerAverageBuyPrice.putIfAbsent(playerId, new HashMap<>());

        // Update player's portfolio
        Map<String, Integer> portfolio = playerPortfolios.get(playerId);
        int oldShares = portfolio.getOrDefault(symbol, 0);
        int newShares = oldShares + amount;
        portfolio.put(symbol, newShares);

        // Update average buy price
        Map<String, Double> avgPriceMap = playerAverageBuyPrice.get(playerId);
        double currentPrice = stock.getCurrentPrice();
        double oldAvg = avgPriceMap.getOrDefault(symbol, currentPrice);
        if (oldShares == 0) {
            avgPriceMap.put(symbol, currentPrice);
        } else {
            double newAvg = ((oldAvg * oldShares) + (currentPrice * amount)) / newShares;
            avgPriceMap.put(symbol, newAvg);
        }

        return true;
    }

    public boolean sellStock(Player player, String symbol, int amount) {
        Stock stock = stocks.get(symbol.toUpperCase());
        if (stock == null) return false;

        UUID playerId = player.getUniqueId();
        Map<String, Integer> portfolio = playerPortfolios.get(playerId);
        
        if (portfolio == null || portfolio.getOrDefault(symbol, 0) < amount) return false;

        double totalValue = stock.getCurrentPrice() * amount;

        // Update player's portfolio and balance
        portfolio.put(symbol, portfolio.get(symbol) - amount);
        playerBalances.put(playerId, playerBalances.getOrDefault(playerId, 0.0) + totalValue);

        return true;
    }

    

    public void updateStockPrices() {
        for (Stock stock : stocks.values()) {
            stock.updatePrice();
        }
    }

    public Map<String, Stock> getStocks() {
        return Collections.unmodifiableMap(stocks);
    }

    public Map<String, Integer> getPlayerPortfolio(Player player) {
        return Collections.unmodifiableMap(playerPortfolios.getOrDefault(player.getUniqueId(), new HashMap<>()));
    }

    public double getPlayerBalance(Player player) {
        return playerBalances.getOrDefault(player.getUniqueId(), 0.0);
    }

    public void addBalance(Player player, double amount) {
        UUID playerId = player.getUniqueId();
        playerBalances.put(playerId, playerBalances.getOrDefault(playerId, 0.0) + amount);
    }

    public double getPlayerCredit(Player player) {
        return playerCredit.computeIfAbsent(player.getUniqueId(), k -> 500.0);
    }

    public void reducePlayerCredit(Player player, double amount) {
        UUID id = player.getUniqueId();
        playerCredit.put(id, getPlayerCredit(player) - amount);
    }

    public void setPlayerCredit(Player player, double amount) {
        playerCredit.put(player.getUniqueId(), amount);
    }

    public double getPlayerAverageBuyPrice(Player player, String symbol) {
        return playerAverageBuyPrice.getOrDefault(player.getUniqueId(), new HashMap<>()).getOrDefault(symbol, 0.0);
    }
} 