package com.sdf.stockexchange.util;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import com.sdf.stockexchange.gui.ChestUI;

import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryUIManager implements Listener{

    private ArrayList<ChestUI> invUI = new ArrayList<ChestUI>();

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();

        // Only intercept clicks in our custom UI
        String title = event.getView().title().toString();

        if (!title.contains("Click Me")) return; // Or match exactly if you set a specific title

        event.setCancelled(true); // Prevent taking items

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        if (clicked.getType() == Material.DIAMOND) {
            player.sendMessage("You clicked the diamond!");
            player.closeInventory();
        }
    }
}
