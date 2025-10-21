
package com.craftvn.shopsystem.gem;

import com.craftvn.shopsystem.ShopSystem;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class GemTags {
    public static final NamespacedKey KIND = new NamespacedKey(ShopSystem.get(), "kind"); // COIN or GEM
    public static final NamespacedKey TYPE = new NamespacedKey(ShopSystem.get(), "type");
    public static final NamespacedKey EXPIRE = new NamespacedKey(ShopSystem.get(), "expire");

    public static void mark(ItemMeta m, String kind, String type, Long expireMs){
        PersistentDataContainer p = m.getPersistentDataContainer();
        p.set(KIND, PersistentDataType.STRING, kind);
        p.set(TYPE, PersistentDataType.STRING, type);
        if (expireMs != null) p.set(EXPIRE, PersistentDataType.LONG, expireMs);
    }
    public static boolean isKind(ItemStack it, String kind){
        if (it==null || !it.hasItemMeta()) return false;
        String v = it.getItemMeta().getPersistentDataContainer().get(KIND, PersistentDataType.STRING);
        return kind.equalsIgnoreCase(v);
    }
    public static String type(ItemStack it){
        if (it==null || !it.hasItemMeta()) return null;
        return it.getItemMeta().getPersistentDataContainer().get(TYPE, PersistentDataType.STRING);
    }
    public static Long expire(ItemStack it){
        if (it==null || !it.hasItemMeta()) return null;
        return it.getItemMeta().getPersistentDataContainer().get(EXPIRE, PersistentDataType.LONG);
    }
}
