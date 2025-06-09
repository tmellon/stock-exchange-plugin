package com.yourname.stockexchange;

public class Stock {
    private final String symbol;
    private double currentPrice;
    private double basePrice;
    private double volatility;

    public Stock(String symbol, double basePrice, double volatility) {
        this.symbol = symbol;
        this.basePrice = basePrice;
        this.currentPrice = basePrice;
        this.volatility = volatility;
    }

    public String getSymbol() {
        return symbol;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void updatePrice() {
        // Simple random walk with mean reversion
        double change = (Math.random() - 0.5) * volatility;
        currentPrice = Math.max(0.1, currentPrice + change);
    }

    public double getBasePrice() {
        return basePrice;
    }

    public double getVolatility() {
        return volatility;
    }
} 