package com.yourname.stockexchange;

import org.bukkit.ChatColor;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.block.Action;

public class StockSignListener implements Listener {
    private final StockManager stockManager;
    private final StockExchangePlugin plugin;

    public StockSignListener(StockManager stockManager, StockExchangePlugin plugin) {
        this.stockManager = stockManager;
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        String[] lines = event.getLines();
        if (StockSign.isValidStockSign(lines)) {
            Player player = event.getPlayer();
            if (!player.hasPermission("stockexchange.createsign")) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You don't have permission to create stock signs!");
                return;
            }

            // Create the sign and update it
            Sign sign = (Sign) event.getBlock().getState();
            StockSign stockSign = StockSign.createFromSign(sign, stockManager);
            if (stockSign != null) {
                stockSign.updateSign(sign);
                sign.setEditable(false);
                sign.update();
            }
        }
    }

    @EventHandler
    public void onSignClick(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;
        if (!(event.getClickedBlock().getState() instanceof Sign)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Sign sign = (Sign) event.getClickedBlock().getState();
        event.getPlayer().sendMessage(ChatColor.YELLOW + "DEBUG: Sign right-clicked!");

        StockSign stockSign = StockSign.createFromSign(sign, stockManager);
        if (stockSign == null) {
            event.getPlayer().sendMessage(ChatColor.RED + "DEBUG: Not a valid stock sign after update!");
            return;
        }

        event.setCancelled(true);
        if (stockSign.isBuySign()) {
            plugin.getLastViewedBuySign().put(event.getPlayer().getUniqueId(), stockSign);
            event.getPlayer().sendMessage(ChatColor.YELLOW + "You are now viewing the " + stockSign.getStockSymbol() + " buy sign. Use /buyshares <amount> to purchase shares.");
        } else {
            plugin.getLastViewedSellSign().put(event.getPlayer().getUniqueId(), stockSign);
            event.getPlayer().sendMessage(ChatColor.YELLOW + "You are now viewing the " + stockSign.getStockSymbol() + " sell sign. Use /sellshares <amount> to sell shares.");
        }
    }

    public void startSignUpdateTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Update all signs in the world
                plugin.getServer().getWorlds().forEach(world -> {
                    for (org.bukkit.Chunk chunk : world.getLoadedChunks()) {
                        for (org.bukkit.block.BlockState state : chunk.getTileEntities()) {
                            if (state instanceof Sign) {
                                Sign sign = (Sign) state;
                                StockSign stockSign = StockSign.createFromSign(sign, stockManager);
                                if (stockSign != null) {
                                    stockSign.updateSign(sign);
                                    sign.setEditable(false);
                                    sign.update();
                                }
                            }
                        }
                    }
                });
            }
        }.runTaskTimer(plugin, 20L, 2400L); // Update every 2 minutes (2400 ticks)
    }
} 