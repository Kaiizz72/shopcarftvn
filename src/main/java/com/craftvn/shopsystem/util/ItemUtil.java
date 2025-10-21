
package com.craftvn.shopsystem.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public final class ItemUtil {
    public static ItemStack makeItem(Material mat, int amount, String name, List<String> lore){
        ItemStack it = new ItemStack(mat, Math.max(1, amount));
        ItemMeta m = it.getItemMeta();
        if (name != null) m.setDisplayName(Msg.color(name));
        if (lore != null) {
            List<String> ls = new ArrayList<>();
            for (String s : lore) ls.add(Msg.color(s));
            m.setLore(ls);
        }
        m.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
        it.setItemMeta(m);
        return it;
    }
}
