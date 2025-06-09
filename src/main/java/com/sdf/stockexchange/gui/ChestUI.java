package com.sdf.stockexchange.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.sdf.stockexchange.StockManager;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

import net.kyori.adventure.text.Component;

public class ChestUI {
    private final Inventory inventory;

    public ChestUI(String stockSymbol, StockManager stockManager) {
        this.inventory = Bukkit.createInventory(null, 27, "Stock: " + stockSymbol);

        // Example item
        ItemStack buy = new ItemStack(Material.EMERALD);
        ItemMeta meta = buy.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Buy 1 Share");
        buy.setItemMeta(meta);
        inventory.setItem(13, buy); // Center slot
    }

    public void open(Player player) {
        player.openInventory(inventory);
    }
}

