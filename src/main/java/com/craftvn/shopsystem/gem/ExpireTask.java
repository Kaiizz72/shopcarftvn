
package com.craftvn.shopsystem.gem;

import com.craftvn.shopsystem.util.Msg;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class ExpireTask extends BukkitRunnable {
    @Override public void run() {
        long now = System.currentTimeMillis();
        for (Player p : Bukkit.getOnlinePlayers()) {
            for (int i=0;i<p.getInventory().getSize();i++){
                ItemStack it = p.getInventory().getItem(i);
                if (it==null) continue;
                if (GemTags.isKind(it, "GEM")) {
                    Long exp = GemTags.expire(it);
                    if (exp != null && now > exp) {
                        p.getInventory().setItem(i, null);
                        Msg.send(p, "&cNgọc đã hết hạn!");
                    }
                }
            }
        }
    }
    public ExpireTask(com.craftvn.shopsystem.ShopSystem plugin){}
}
