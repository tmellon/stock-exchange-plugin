package com.sdf.stockexchange;

import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.sdf.stockexchange.commands.BuySharesCommand;
import com.sdf.stockexchange.commands.PortfolioCommand;
import com.sdf.stockexchange.commands.PriceCommand;
import com.sdf.stockexchange.commands.SellSharesCommand;
import com.sdf.stockexchange.commands.SetCreditCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class StockExchangePlugin extends JavaPlugin {
    private StockManager stockManager;
    private BukkitTask priceUpdateTask;
    private StockSignListener signListener;
    private final Map<UUID, StockSign> lastViewedBuySign = new HashMap<>();
    private final Map<UUID, StockSign> lastViewedSellSign = new HashMap<>();
    private Map<Location, String> signRegistry;
    
    @Override
    public void onEnable() {
        getLogger().info("StockExchange plugin enabled!");
        this.signRegistry = new HashMap<>();
        
        // Initialize stock manager
        stockManager = new StockManager();
        
        // Register commands
        getCommand("buyshares").setExecutor(new BuySharesCommand(stockManager, this));
        getCommand("sellshares").setExecutor(new SellSharesCommand(stockManager, this));
        getCommand("stockprice").setExecutor(new PriceCommand(stockManager));
        getCommand("setcredit").setExecutor(new SetCreditCommand(stockManager));
        getCommand("portfolio").setExecutor(new PortfolioCommand(stockManager));
        
        // Register sign listener
        signListener = new StockSignListener(stockManager, this);
        getServer().getPluginManager().registerEvents(signListener, this);
        signListener.startSignUpdateTask();
        
        // Start price update task (every 2 minutes)
        priceUpdateTask = getServer().getScheduler().runTaskTimer(this, () -> {
            stockManager.updateStockPrices();
            getLogger().info("Stock prices have been updated!");
        }, 2400L, 2400L); // 2400 ticks = 2 minutes
    }

    @Override
    public void onDisable() {
        if (priceUpdateTask != null) {
            priceUpdateTask.cancel();
        }
        getLogger().info("StockExchange plugin disabled.");
    }

    public StockManager getStockManager() {
        return stockManager;
    }

    public Map<UUID, StockSign> getLastViewedBuySign() {
        return lastViewedBuySign;
    }

    public Map<UUID, StockSign> getLastViewedSellSign() {
        return lastViewedSellSign;
    }

    public Map<Location, String> getSignRegistry() {
        return signRegistry;
    }
}
