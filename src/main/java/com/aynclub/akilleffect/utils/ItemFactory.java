package com.aynclub.akilleffect.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemFactory {
    public static ItemStack create(Material material, byte data, String displayName, String... lore) {
        @SuppressWarnings("deprecation")
        ItemStack itemStack = new MaterialData(material, data).toItemStack(1);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(displayName);
        if (lore != null) {
            List<String> finalLore = new ArrayList<String>();
            Collections.addAll(finalLore, lore);
            itemMeta.setLore(finalLore);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public static ItemStack create(Material material, byte data, String displayName) {
        return create(material, data, displayName, (String[]) null);
    }

    public static void addHiddenLore(ItemStack item, String lore) {
        ItemMeta meta = item.getItemMeta();
        List<String> loreLines = meta.hasLore() ? new ArrayList<String>(meta.getLore()) : new ArrayList<String>();
        loreLines.add(lore);
        meta.setLore(loreLines);
        item.setItemMeta(meta);
    }
}
