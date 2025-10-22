
package com.craftvn.shopsystem.gui;

import com.craftvn.shopsystem.ShopSystem;
import com.craftvn.shopsystem.gem.GemTags;
import com.craftvn.shopsystem.util.ItemUtil;
import com.craftvn.shopsystem.util.Msg;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.Material;
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

public class CoinShop implements CommandExecutor, Listener {
    private final ShopSystem plugin;
    private final String TITLE;

    public CoinShop(ShopSystem pl){
        this.plugin = pl;
        this.TITLE = plugin.getConfig().getString("ui.titles.shopcoin", "Shop Coin");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        open(p);
        return true;
    }

    private void open(Player p){
        int rows = plugin.getConfig().getInt("shopcoin.rows", 3);
        Inventory inv = Bukkit.createInventory(null, rows*9, Msg.color(TITLE));
        List<Map<?,?>> items = plugin.getConfig().getMapList("shopcoin.items");
        for (Map<?,?> m : items){
            try {
                String name = (String)m.get("name");
                String mat = (String)m.get("material");
                Object rawAmount = m.get("amount");
                int amount;
                if (rawAmount instanceof Number num) {
                    amount = num.intValue();
                } else {
                    amount = 1;
                }
                int slot = ((Number)m.get("slot")).intValue();
                Object rawPrice = m.get("price_coin");
                int price;
                if (rawPrice instanceof Number num) {
                    price = num.intValue();
                } else {
                    price = 0;
                }
                @SuppressWarnings("unchecked")
                List<String> lore;
                Object rawLore = m.get("lore");
                if (rawLore instanceof List<?>) {
                    lore = ((List<?>) rawLore).stream().map(Object::toString).toList();
                } else {
                    lore = new ArrayList<>();
                }
                ItemStack it = ItemUtil.makeItem(Material.valueOf(mat), amount, name, lore);
                ItemMeta meta = it.getItemMeta();
                meta.getPersistentDataContainer().set(GemTags.KIND, PersistentDataType.STRING, "COIN_OR_VANILLA");
                // optional: if coin gem skill present -> mark as COIN with TYPE
                if (m.containsKey("coin_gem_skill")){
                    String type = (String)m.get("coin_gem_skill");
                    GemTags.mark(meta, "COIN", type, null);
                }
                meta.getPersistentDataContainer().set(new org.bukkit.NamespacedKey(plugin, "price"), PersistentDataType.INTEGER, price);
                it.setItemMeta(meta);
                inv.setItem(slot, it);
            } catch (Exception ex){
                plugin.getLogger().warning("Bỏ qua item shopcoin lỗi cấu hình: "+m);
            }
        }
        p.openInventory(inv);
    }

    @EventHandler
    public void click(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory()==null) return;
        String title = e.getView().getTitle();
        if (!title.equals(Msg.color(TITLE))) return;
        e.setCancelled(true);
        ItemStack it = e.getCurrentItem();
        if (it==null || it.getType()==Material.AIR) return;
        ItemMeta meta = it.getItemMeta();
        Integer price = meta.getPersistentDataContainer().get(new org.bukkit.NamespacedKey(plugin, "price"), PersistentDataType.INTEGER);
        if (price == null) return;

        if (p.getInventory().firstEmpty() == -1) {
            Msg.send(p, plugin.getConfig().getString("messages.inv_full"));
            return;
        }

        if (ShopSystem.get().econ().getBalance(p) < price){
            Msg.send(p, plugin.getConfig().getString("messages.not_enough_coin"));
            return;
        }

        EconomyResponse r = ShopSystem.get().econ().withdrawPlayer(p, price);
        if (!r.transactionSuccess()){
            Msg.send(p, plugin.getConfig().getString("messages.not_enough_coin"));
            return;
        }

        // Give a CLONE (avoid moving GUI item)
        ItemStack give = it.clone();
        ItemMeta gm = give.getItemMeta();
        // keep COIN-gem tags if exist; otherwise clear KIND mark
        String kind = gm.getPersistentDataContainer().get(GemTags.KIND, PersistentDataType.STRING);
        if (!"COIN".equals(kind)) {
            gm.getPersistentDataContainer().remove(GemTags.KIND);
            gm.getPersistentDataContainer().remove(GemTags.TYPE);
        }
        give.setItemMeta(gm);

        p.getInventory().addItem(give);
        Msg.send(p, plugin.getConfig().getString("messages.bought").replace("%item%", it.getItemMeta().getDisplayName()));
    }
}
