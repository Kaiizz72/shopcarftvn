
package com.craftvn.shopsystem.gui;

import com.craftvn.shopsystem.ShopSystem;
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

public class SellShop implements CommandExecutor, Listener {
    private final ShopSystem plugin;
    private final String TITLE_MAIN;
    private final String TITLE_AMOUNT;

    public SellShop(ShopSystem pl){
        this.plugin = pl;
        this.TITLE_MAIN = plugin.getConfig().getString("ui.titles.bando_main", "Bán Đồ");
        this.TITLE_AMOUNT = plugin.getConfig().getString("ui.titles.bando_amount", "Chọn Số Lượng");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player p)) return true;
        openMain(p);
        return true;
    }

    private void openMain(Player p){
        int rows = plugin.getConfig().getInt("bando.rows", 3);
        Inventory inv = Bukkit.createInventory(null, rows*9, Msg.color(TITLE_MAIN));
        List<Map<?,?>> cats = plugin.getConfig().getMapList("bando.categories");
        for (Map<?,?> c : cats){
            try {
                String name = (String)c.get("name");
                String mat = (String)c.get("material");
                int slot = ((Number)c.get("slot")).intValue();
                ItemStack it = ItemUtil.makeItem(Material.valueOf(mat), 1, name, List.of("&7➛ Chọn để xem vật phẩm"));
                ItemMeta meta = it.getItemMeta();
                meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "cat_index"), PersistentDataType.INTEGER, cats.indexOf(c));
                it.setItemMeta(meta);
                inv.setItem(slot, it);
            } catch (Exception ex){
                plugin.getLogger().warning("Bỏ qua category lỗi cấu hình: "+c);
            }
        }
        p.openInventory(inv);
    }

    private void openAmount(Player p, Material mat, double unitPrice){
        Inventory inv = Bukkit.createInventory(null, 27, Msg.color(TITLE_AMOUNT));
        // Buttons
        inv.setItem(10, ItemUtil.makeItem(Material.LIME_DYE, 1, "&a+1", null));
        inv.setItem(11, ItemUtil.makeItem(Material.LIME_DYE, 1, "&a+5", null));
        inv.setItem(12, ItemUtil.makeItem(Material.LIME_DYE, 1, "&a+10", null));
        inv.setItem(13, ItemUtil.makeItem(Material.SLIME_BALL, 1, "&aMAX", null));
        inv.setItem(14, ItemUtil.makeItem(Material.RED_DYE, 1, "&c-1", null));
        inv.setItem(15, ItemUtil.makeItem(Material.RED_DYE, 1, "&c-5", null));
        inv.setItem(21, ItemUtil.makeItem(Material.EMERALD_BLOCK, 1, "&a✔ Xác nhận", null));
        inv.setItem(23, ItemUtil.makeItem(Material.BARRIER, 1, "&c✖ Hủy", null));

        // Center item with info
        int have = countInInv(p, mat);
        List<String> lore = new ArrayList<>();
        lore.add("&7➛ Giá mỗi cái: &f" + Msg.pretty(unitPrice));
        lore.add("&7➛ Trong kho: &f" + have);
        lore.add("&e✦ Chọn số lượng và ấn &a✔");
        ItemStack center = ItemUtil.makeItem(mat, 1, "&eVật phẩm bán", lore);
        ItemMeta meta = center.getItemMeta();
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "sell_mat"), PersistentDataType.STRING, mat.name());
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "sell_unit"), PersistentDataType.DOUBLE, unitPrice);
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "sell_qty"), PersistentDataType.INTEGER, 0);
        center.setItemMeta(meta);
        inv.setItem(13, center);

        p.openInventory(inv);
    }

    @EventHandler
    public void click(InventoryClickEvent e){
        if (!(e.getWhoClicked() instanceof Player p)) return;
        if (e.getClickedInventory()==null) return;
        String title = e.getView().getTitle();

        if (title.equals(Msg.color(TITLE_MAIN))) {
            e.setCancelled(true);
            ItemStack it = e.getCurrentItem();
            if (it==null || it.getType()==Material.AIR) return;
            Integer catIndex = it.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"cat_index"), PersistentDataType.INTEGER);
            if (catIndex == null) return;
            // open items of category in same GUI (simple pick-first item style)
            List<Map<?,?>> cats = plugin.getConfig().getMapList("bando.categories");
            Map<?,?> cat = cats.get(catIndex);
            @SuppressWarnings("unchecked")
            List<Map<?,?>> items = (List<Map<?,?>>) cat.get("items");
            // For simplicity: open a list as amount GUI per click of each item slot
            // We'll place first 9 items into slots row 2
            Inventory inv = Bukkit.createInventory(null, 27, Msg.color(TITLE_MAIN));
            int base = 9;
            int pos = base;
            for (Map<?,?> m : items){
                try {
                    String mat = (String)m.get("material");
                    double price = ((Number)m.get("price_coin")).doubleValue();
                    ItemStack entry = ItemUtil.makeItem(Material.valueOf(mat), 1, "&f" + mat, List.of("&7➛ Giá: &f"+Msg.pretty(price)+" coin", "&e✦ Chạm để bán"));
                    ItemMeta meta = entry.getItemMeta();
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "open_amount_mat"), PersistentDataType.STRING, mat);
                    meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "open_amount_unit"), PersistentDataType.DOUBLE, price);
                    entry.setItemMeta(meta);
                    inv.setItem(pos++, entry);
                    if (pos>=27) break;
                } catch (Exception ignore){}
            }
            p.openInventory(inv);
            return;
        }

        // Clicking item in category list to open amount GUI
        if (title.equals(Msg.color(TITLE_MAIN))) {
            return;
        }

        if (title.equals(Msg.color(TITLE_AMOUNT))){
            e.setCancelled(true);
            ItemStack cur = e.getCurrentItem();
            if (cur==null || cur.getType()==Material.AIR) return;

            ItemStack center = e.getInventory().getItem(13);
            if (center==null || !center.hasItemMeta()) return;
            var pdc = center.getItemMeta().getPersistentDataContainer();
            String matName = pdc.get(new NamespacedKey(plugin,"sell_mat"), PersistentDataType.STRING);
            Double unit = pdc.get(new NamespacedKey(plugin,"sell_unit"), PersistentDataType.DOUBLE);
            Integer qty = pdc.get(new NamespacedKey(plugin,"sell_qty"), PersistentDataType.INTEGER);
            if (matName==null || unit==null || qty==null) return;

            Material mat = Material.valueOf(matName);

            switch (cur.getType()){
                case LIME_DYE -> qty += cur.getItemMeta().getDisplayName().contains("+10") ? 10 :
                        cur.getItemMeta().getDisplayName().contains("+5") ? 5 : 1;
                case SLIME_BALL -> qty = countInInv(p, mat);
                case RED_DYE -> qty -= cur.getItemMeta().getDisplayName().contains("-5") ? 5 : 1;
                case EMERALD_BLOCK -> {
                    int have = countInInv(p, mat);
                    qty = Math.max(1, Math.min(qty, have));
                    double total = unit * qty;
                    // remove items
                    removeItems(p, mat, qty);
                    // pay
                    pay(p, total);
                    Msg.send(p, plugin.getConfig().getString("messages.sold")
                            .replace("%amount%", String.valueOf(qty))
                            .replace("%item%", mat.name())
                            .replace("%value%", Msg.pretty(total)));
                    p.closeInventory();
                    return;
                }
                case BARRIER -> { p.closeInventory(); return; }
                default -> { return; }
            }
            if (qty < 0) qty = 0;
            // update qty
            pdc.set(new NamespacedKey(plugin,"sell_qty"), PersistentDataType.INTEGER, qty);
            var meta = center.getItemMeta(); meta.getPersistentDataContainer().set(new NamespacedKey(plugin,"sell_qty"), PersistentDataType.INTEGER, qty);
            List<String> lore = new ArrayList<>();
            lore.add("&7➛ Giá mỗi cái: &f" + Msg.pretty(unit));
            lore.add("&7➛ Đang chọn: &f" + qty + "x");
            lore.add("&e✦ Tổng cộng: &f" + Msg.pretty(unit*qty) + " coin");
            meta.setLore(lore.stream().map(Msg::color).toList());
            center.setItemMeta(meta);
            e.getInventory().setItem(13, center);
        }

        // Open amount GUI from list
        if (e.getView().getTitle().equals(Msg.color(TITLE_MAIN))) {
            ItemStack it = e.getCurrentItem();
            if (it==null || it.getType()==Material.AIR) return;
            String mat = it.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"open_amount_mat"), PersistentDataType.STRING);
            Double unit = it.getItemMeta().getPersistentDataContainer().get(new NamespacedKey(plugin,"open_amount_unit"), PersistentDataType.DOUBLE);
            if (mat!=null && unit!=null){
                openAmount((Player)e.getWhoClicked(), Material.valueOf(mat), unit);
            }
        }
    }

    private int countInInv(Player p, Material mat){
        int c=0;
        for (ItemStack s : p.getInventory().getContents()){
            if (s!=null && s.getType()==mat) c+=s.getAmount();
        }
        return c;
    }
    private void removeItems(Player p, Material mat, int qty){
        int left = qty;
        for (int i=0;i<p.getInventory().getSize();i++){
            ItemStack s = p.getInventory().getItem(i);
            if (s==null || s.getType()!=mat) continue;
            int take = Math.min(left, s.getAmount());
            s.setAmount(s.getAmount()-take);
            if (s.getAmount()<=0) p.getInventory().setItem(i, null);
            left -= take;
            if (left<=0) break;
        }
    }
    private void pay(Player p, double total){
        String mode = plugin.getConfig().getString("economy.pay_mode_on_sell", "vault");
        if ("essentials_auto".equalsIgnoreCase(mode)){
            String cmd = "eco give " + p.getName() + " " + Msg.pretty(total);
            try { Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd); }
            catch (Exception ex){
                ShopSystem.get().econ().depositPlayer(p, total);
            }
        } else {
            ShopSystem.get().econ().depositPlayer(p, total);
        }
    }
}
