package com.sdf.stockexchange.util;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import com.sdf.stockexchange.StockExchangePlugin;
import com.sdf.stockexchange.gui.ChestUI;

public class SignClickListener implements Listener {

    private final StockExchangePlugin plugin;

    public SignClickListener(StockExchangePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignRightClick(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getClickedBlock() == null) return;

        Block block = event.getClickedBlock();
        if (!(block.getState() instanceof Sign)) return;

        Location location = block.getLocation();
        String symbol = plugin.getSignRegistry().get(location);
        if (symbol == null) return; // Not a registered stock sign

        Player player = event.getPlayer();
        player.sendMessage(ChatColor.YELLOW + "Opening stock GUI for " + symbol);

        ChestUI chestUI = new ChestUI(symbol, plugin.getStockManager());
        chestUI.open(player);
        event.setCancelled(true);
    }
}
