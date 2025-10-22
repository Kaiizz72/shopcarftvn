
package com.craftvn.shopsystem.util;

import com.craftvn.shopsystem.ShopSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class Msg {
    public static String color(String s){ return ChatColor.translateAlternateColorCodes('&', s==null?"":s); }
    public static void send(CommandSender to, String s){ if (to!=null && s!=null && !s.isEmpty()) to.sendMessage(color(s)); }
    public static void info(String s){ Bukkit.getConsoleSender().sendMessage("[ShopSystem] " + s); }
    public static String pretty(double d){
        if (Math.abs(d - Math.rint(d)) < 1e-9) return String.valueOf((long)Math.rint(d));
        return String.format("%.2f", d);
    }
}
