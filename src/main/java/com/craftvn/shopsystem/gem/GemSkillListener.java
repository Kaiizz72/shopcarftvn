
package com.craftvn.shopsystem.gem;

import com.craftvn.shopsystem.ShopSystem;
import com.craftvn.shopsystem.util.Msg;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class GemSkillListener implements Listener {
    private final ShopSystem plugin;
    private final Set<UUID> shielded = new HashSet<>();

    public GemSkillListener(ShopSystem plugin){ this.plugin = plugin; }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onUse(PlayerInteractEvent e) {
        if (e.getHand() != EquipmentSlot.HAND) return;
        if (e.getAction() != Action.RIGHT_CLICK_AIR && e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Player p = e.getPlayer();
        ItemStack it = p.getInventory().getItemInMainHand();
        if (it==null || !it.hasItemMeta()) return;

        String kindCoin = "COIN", kindGem = "GEM";
        if (!GemTags.isKind(it, kindCoin) && !GemTags.isKind(it, kindGem)) return;

        // check expire for GEM
        if (GemTags.isKind(it, kindGem)) {
            Long exp = GemTags.expire(it);
            if (exp!=null && System.currentTimeMillis() > exp) {
                p.getInventory().setItemInMainHand(null);
                Msg.send(p, "&cNgọc đã hết hạn!");
                e.setCancelled(true); return;
            }
        }

        String type = GemTags.type(it);
        if (type == null) return;

        boolean ok = cast(type, p);
        if (!ok) { Msg.send(p, "&cKhông thể dùng ở đây!"); return; }

        // consume 1 item
        int amt = it.getAmount();
        if (amt > 1) it.setAmount(amt - 1);
        else p.getInventory().setItemInMainHand(null);

        e.setCancelled(true);
    }

    private boolean cast(String type, Player p){
        switch (type) {
            // GEM skills (xịn)
            case "HEAL" -> {
                p.setHealth(Math.min(p.getMaxHealth(), p.getHealth() + 12));
                p.getWorld().spawnParticle(Particle.HEART, p.getLocation(), 20, 1,1,1);
                Msg.send(p, "&a✦ Bạn đã hồi máu!");
                return true;
            }
            case "INVIS" -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 20*15, 0));
                Msg.send(p, "&7✦ Bạn tàng hình 15s!");
                return true;
            }
            case "SPEED" -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20*30, 1));
                Msg.send(p, "&b✦ Tăng tốc 30s!");
                return true;
            }
            case "SHIELD" -> {
                shielded.add(p.getUniqueId());
                Msg.send(p, "&e✦ Khiên bảo vệ sẵn sàng!");
                return true;
            }
            case "STRENGTH" -> {
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20*60, 0));
                Msg.send(p, "&c✦ Cường hóa 60s!");
                return true;
            }

            // COIN gems (cùi)
            case "TO_TROI" -> {
                World w = p.getWorld();
                Location loc = p.getLocation();
                Random rnd = new Random();
                int placed = 0;
                for (int x=-2;x<=2;x++){
                    for (int z=-2;z<=2;z++){
                        if (placed >= 28) break;
                        Location bLoc = loc.clone().add(x,0,z);
                        if (w.getBlockAt(bLoc).getType()==Material.AIR && rnd.nextBoolean()){
                            Material prev = Material.AIR;
                            w.getBlockAt(bLoc).setType(Material.COBWEB);
                            placed++;
                            long delay = 20L * (1 + rnd.nextInt(4));
                            Bukkit.getScheduler().runTaskLater(ShopSystem.get(), () -> {
                                if (w.getBlockAt(bLoc).getType()==Material.COBWEB) w.getBlockAt(bLoc).setType(prev);
                            }, delay);
                        }
                    }
                }
                Msg.send(p, "&b✦ Ngọc Tơ Trói tung ra lưới!");
                return true;
            }
            case "KHOI_MU" -> {
                p.getWorld().spawnParticle(Particle.SMOKE_LARGE, p.getLocation(), 40, 2,1,2);
                for (Player near : p.getWorld().getPlayers()) {
                    if (near!=p && near.getLocation().distance(p.getLocation())<=3){
                        near.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0));
                    }
                }
                Msg.send(p, "&8✦ Khói mù bao phủ!");
                return true;
            }
            case "SLIME" -> {
                for (Player near : p.getWorld().getPlayers()) {
                    if (near!=p && near.getLocation().distance(p.getLocation())<=3){
                        near.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 60, 1));
                    }
                }
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_SLIME_SQUISH, 1, 1);
                Msg.send(p, "&a✦ Slime dính làm chậm địch!");
                return true;
            }
            case "BAT_NAY" -> {
                for (Player near : p.getWorld().getPlayers()) {
                    if (near!=p && near.getLocation().distance(p.getLocation())<=3){
                        Vector v = near.getLocation().toVector().subtract(p.getLocation().toVector()).normalize().multiply(0.8);
                        near.setVelocity(v.setY(0.3));
                    }
                }
                p.getWorld().playSound(p.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 1, 1);
                Msg.send(p, "&c✦ Hất tung đối thủ!");
                return true;
            }
            case "TUYET_TAN" -> {
                for (int i=0;i<5;i++){
                    Snowball sb = p.launchProjectile(Snowball.class);
                    sb.setShooter(p);
                }
                p.getWorld().playSound(p.getLocation(), Sound.BLOCK_SNOW_BREAK, 1, 1);
                Msg.send(p, "&f✦ Bão tuyết tung ra!");
                return true;
            }
        }
        return false;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onFatal(EntityDamageEvent e){
        if (!(e.getEntity() instanceof Player p)) return;
        if (!shielded.contains(p.getUniqueId())) return;
        double finalHp = p.getHealth() - e.getFinalDamage();
        if (finalHp <= 0){
            e.setCancelled(true);
            p.setHealth(2.0);
            shielded.remove(p.getUniqueId());
            p.getWorld().playSound(p.getLocation(), Sound.ITEM_SHIELD_BLOCK, 1, 1);
            Msg.send(p, "&6✦ Khiên bảo vệ đã cứu bạn!");
        }
    }
}
