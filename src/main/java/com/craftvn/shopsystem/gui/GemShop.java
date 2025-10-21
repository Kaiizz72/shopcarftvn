
package com.craftvn.shopsystem.gui;

import com.craftvn.shopsystem.ShopSystem;
import com.craftvn.shopsystem.gem.GemTags;
import com.craftvn.shopsystem.util.ItemUtil;
import com.craftvn.shopsystem.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GemShop implements CommandExecutor, Listener {
    private final ShopSystem plugin;
    private final String TITLE;
    private final int expireMinutes;

    public GemShop(ShopSystem pl){
        this.plugin = pl;
        this.TITLE = plugin.getConfig().getString("ui.titles.shopgem", "Shop Gem");
        this.expireMinutes = plugin.getConfig().getInt("shopgem.expire_minutes", 60);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        open(p);
        return true;
    }

    private void open(Player p){
        int rows = plugin.getConfig().getInt("shopgem.rows", 3);
        Inventory inv = Bukkit.createInventory(null, rows*9, Msg.color(TITLE));
        List<Map<?,?>> items = plugin.getConfig().getMapList("shopgem.items");
        for (Map<?,?> m : items){
            try {
                String name = (String)m.get("name");
                String mat = (String)m.get("material");
                int slot = ((Number)m.get("slot")).intValue();
                int priceGem = ((Number)m.get("price_gem")).intValue();
                String type = (String)m.get("type");
                @SuppressWarnings("unchecked")
                List<String> lore = (List<String>) m.getOrDefault("lore", new ArrayList<String>());
                ItemStack it = ItemUtil.makeItem(Material.valueOf(mat), 1, name, lore);
                ItemMeta meta = it.getItemMeta();
                GemTags.mark(meta, "GEM", type, System.currentTimeMillis() + expireMinutes*60L*1000L);
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "price_gem"), PersistentDataType.INTEGER, priceGem);
                it.setItemMeta(meta);
                inv.setItem(slot, it);
            } catch (Exception ex){
                plugin.getLogger().warning("Bỏ qua item shopgem lỗi cấu hình: "+m);
            }
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void click(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory()==null) return;
        if (!e.getView().getTitle().equals(Msg.color(TITLE))) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem();
        if (it==null || it.getType()==Material.AIR) return;
        ItemMeta meta = it.getItemMeta();
        Integer priceGem = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "price_gem"), PersistentDataType.INTEGER);
        if (priceGem == null) return;

        // limit one each type
        String type = meta.getPersistentDataContainer().get(GemTags.TYPE, PersistentDataType.STRING);
        if (plugin.getConfig().getBoolean("shopgem.limit_one_each_type", true)) {
            for (ItemStack invItem : p.getInventory().getContents()){
                if (invItem==null) continue;
                if (GemTags.isKind(invItem, "GEM")){
                    String t = GemTags.type(invItem);
                    if (type!=null && type.equals(t)){
                        Msg.send(p, plugin.getConfig().getString("messages.gem_owned"));
                        return;
                    }
                }
            }
        }

        if (p.getInventory().firstEmpty() == -1) {
            Msg.send(p, plugin.getConfig().getString("messages.inv_full"));
            return;
        }

        if (ShopSystem.get().pp().look(p.getUniqueId()) < priceGem){
            Msg.send(p, plugin.getConfig().getString("messages.not_enough_gem"));
            return;
        }

        boolean ok = ShopSystem.get().pp().take(p.getUniqueId(), priceGem);
        if (!ok){
            Msg.send(p, plugin.getConfig().getString("messages.not_enough_gem"));
            return;
        }

        ItemStack give = it.clone(); // keep GEM tags
        p.getInventory().addItem(give);
        Msg.send(p, plugin.getConfig().getString("messages.bought").replace("%item%", it.getItemMeta().getDisplayName()));
    }
}
