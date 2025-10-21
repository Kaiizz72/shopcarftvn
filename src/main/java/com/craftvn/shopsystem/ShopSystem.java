
package com.craftvn.shopsystem;

import com.craftvn.shopsystem.gem.ExpireTask;
import com.craftvn.shopsystem.gem.GemSkillListener;
import com.craftvn.shopsystem.gui.CoinShop;
import com.craftvn.shopsystem.gui.GemShop;
import com.craftvn.shopsystem.gui.SellShop;
import com.craftvn.shopsystem.util.Msg;
import net.milkbowl.vault.economy.Economy;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class ShopSystem extends JavaPlugin {

    private static ShopSystem instance;
    private Economy econ;
    private PlayerPointsAPI ppAPI;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        // Vault
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            getLogger().severe("Thiếu Vault!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Không tìm thấy Economy provider.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        econ = rsp.getProvider();

        // PlayerPoints
        if (getServer().getPluginManager().getPlugin("PlayerPoints") == null) {
            getLogger().severe("Thiếu PlayerPoints!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        ppAPI = PlayerPoints.getInstance().getAPI();

        // Commands
        CoinShop coinShop = new CoinShop(this);
        GemShop gemShop = new GemShop(this);
        SellShop sellShop = new SellShop(this);

        getCommand("shopcoin").setExecutor(coinShop);
        getCommand("shopgem").setExecutor(gemShop);
        getCommand("bando").setExecutor(sellShop);

        // Listeners
        Bukkit.getPluginManager().registerEvents(coinShop, this);
        Bukkit.getPluginManager().registerEvents(gemShop, this);
        Bukkit.getPluginManager().registerEvents(sellShop, this);
        Bukkit.getPluginManager().registerEvents(new GemSkillListener(this), this);

        // Tasks
        new ExpireTask(this).runTaskTimer(this, 20L * 60, 20L * 60);

        Msg.info("ShopSystem đã bật!");
    }

    @Override
    public void onDisable() {
        Msg.info("ShopSystem đã tắt!");
    }

    public static ShopSystem get() { return instance; }
    public Economy econ() { return econ; }
    public PlayerPointsAPI pp() { return ppAPI; }
}
