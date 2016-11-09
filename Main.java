package io.fredde.chestExecute;

import io.fredde.chestExecute.Chest;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;

public class Main
extends JavaPlugin
implements Listener {
    Logger logger;
    List<Chest> chests;
    File fileConfig;
    File fileDirectoryConfig;
    FileConfiguration fileDelegate;
    int checks = 0;
    final String FILE_CONFIG_NAME = "settings.yml";
    final String FILE_CONFIG_ROOT_PATH = "ce";

    public void onEnable() {
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
        this.logger = this.getLogger();
        this.init();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() != null && event.getClickedBlock().getType() == Material.CHEST) {
            Player player = event.getPlayer();
            Block block = event.getClickedBlock();
            if (player.hasPermission("ce.use") || player.isOp()) {
                for (Chest chest : this.chests) {
                    if (!block.getLocation().toVector().equals((Object)chest.getLocation()) || !block.getWorld().equals((Object)chest.getWorld())) continue;
                    event.setCancelled(true);
                    if (chest.isActive()) {
                        chest.setActive(false);
                        if (Math.random() < chest.getChance()) {
                            player.sendMessage(this.cc("&aYou were lucky!"));
                            player.getWorld().playSound(block.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 5.0f, 0.0f);
                            Iterator<String> i$ = chest.getCommands().iterator();
                            while (i$.hasNext()) {
                                String command = i$.next();
                                command = command.replace("{player}", player.getName());
                                this.getServer().dispatchCommand((CommandSender)this.getServer().getConsoleSender(), command);
                            }
                            continue;
                        }
                        player.sendMessage(this.cc("&cYou were unlucky."));
                        player.getWorld().playSound(block.getLocation(), Sound.BLOCK_GLASS_BREAK, 5.0f, 0.0f);
                        continue;
                    }
                    player.sendMessage(this.cc("&eThis chest is on a cooldown."));
                }
            } else {
                player.sendMessage(this.cc("&cYou're not allowed to use this chest."));
            }
        }
    }

    public void init() {
        if (this.createDirectoryConfig()) {
            ++this.checks;
            this.logger.info("[" + this.checks + "] createDirectoryConfig is valid.");
            if (this.createFileConfig("settings.yml")) {
                ++this.checks;
                this.logger.info("[" + this.checks + "] createFileConfig is valid.");
                if (this.loadFileConfig()) {
                    ++this.checks;
                    this.logger.info("[" + this.checks + "] loadFileConfig is valid.");
                    int interval = this.fileDelegate.getInt("ce.interval");
                    if (interval > 20) {
                        BukkitScheduler chestActivation = this.getServer().getScheduler();
                        chestActivation.scheduleSyncRepeatingTask((Plugin)this, new Runnable(){

                            @Override
                            public void run() {
                                for (Chest chest : Main.this.chests) {
                                    chest.setActive(true);
                                }
                                Main.this.logger.info("All chests reactivated.");
                            }
                        }, 0, (long)interval);
                        BukkitScheduler chestEffect = this.getServer().getScheduler();
                        chestEffect.scheduleSyncRepeatingTask((Plugin)this, new Runnable(){

                            @Override
                            public void run() {
                                for (Chest chest : Main.this.chests) {
                                    Location location = new Location(chest.getWorld(), (double)chest.getLocation().getBlockX(), (double)(chest.getLocation().getBlockY() + 2), (double)chest.getLocation().getBlockZ());
                                    chest.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 0);
                                }
                            }
                        }, 0, 200);
                        this.logger.info("Timers started.");
                        this.logger.info("Successful boot!");
                    } else {
                        this.logger.warning("Interval is invalid.");
                        this.getServer().getPluginManager().disablePlugin((Plugin)this);
                    }
                } else {
                    this.logger.warning("loadFileConfig failed.");
                    this.getServer().getPluginManager().disablePlugin((Plugin)this);
                }
            } else {
                this.logger.warning("createFileConfig failed.");
                this.getServer().getPluginManager().disablePlugin((Plugin)this);
            }
        } else {
            this.logger.warning("createDirectoryConfig failed.");
            this.getServer().getPluginManager().disablePlugin((Plugin)this);
        }
    }

    public boolean createFileConfig(String fileConfigName) {
        this.fileConfig = new File(this.getDataFolder(), fileConfigName);
        if (!this.fileConfig.exists()) {
            try {
                if (this.fileConfig.createNewFile()) {
                    this.logger.info("fileConfig created.");
                    this.fileDelegate = YamlConfiguration.loadConfiguration((File)this.fileConfig);
                    this.logger.info("fileDelegate assigned.");
                    this.fileDelegate.set("ce.interval", (Object)1728000);
                    for (int i = 0; i < 3; ++i) {
                        this.fileDelegate.set("ce.chest" + i + ".location", (Object)new Vector(i, i, i));
                        this.fileDelegate.set("ce.chest" + i + ".world", (Object)((World)this.getServer().getWorlds().get(0)).getName());
                        ArrayList<String> commands = new ArrayList<String>();
                        commands.add("say lorem");
                        commands.add("tell {player} ipsum");
                        this.fileDelegate.set("ce.chest" + i + ".commands", commands);
                        this.fileDelegate.set("ce.chest" + i + ".chance", (Object)((double)Math.round(Math.random() * 100.0) / 100.0));
                    }
                    this.saveFileConfig();
                    PrintWriter printWriter = new PrintWriter(new FileWriter("plugins/chestExecute/settings.yml", true));
                    printWriter.write("\n");
                    printWriter.write("# These are the settings for chestExecute.\n");
                    printWriter.write("# interval - how often the chests reactive (20 = 1 second).\n");
                    printWriter.write("# To create more chests, just simply copy one of the default ones and change the location, world, commands and chance.\n");
                    printWriter.write("\n");
                    printWriter.write("# Version: " + this.getDescription().getVersion() + "\n");
                    printWriter.close();
                    this.logger.info("fileConfig populated.");
                    return true;
                }
            }
            catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        this.logger.info("fileConfig already created.");
        this.fileDelegate = YamlConfiguration.loadConfiguration((File)this.fileConfig);
        this.logger.info("fileDelegate assigned.");
        return true;
    }

    public boolean createDirectoryConfig() {
        this.fileDirectoryConfig = new File(this.getDataFolder().toString());
        if (!this.fileDirectoryConfig.exists()) {
            if (this.fileDirectoryConfig.mkdir()) {
                this.logger.info("directoryConfig created.");
                return true;
            }
            return false;
        }
        this.logger.info("directoryConfig already created.");
        return true;
    }

    public boolean saveFileConfig() {
        try {
            this.fileDelegate.save(this.fileConfig);
            this.logger.info("fileConfig saved.");
            return true;
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean loadFileConfig() {
        this.chests = new ArrayList<Chest>();
        if (this.fileDelegate.contains("ce")) {
            ConfigurationSection fileDelegateConfigurationSection = this.fileDelegate.getConfigurationSection("ce");
            for (String target : fileDelegateConfigurationSection.getKeys(false)) {
                if (!target.startsWith("chest")) continue;
                Vector location = this.fileDelegate.getVector("ce." + target + ".location");
                World world = this.getServer().getWorld(this.fileDelegate.getString("ce." + target + ".world"));
                ArrayList<String> commands = new ArrayList<String>();
                for (String command : this.fileDelegate.getStringList("ce." + target + ".commands")) {
                    commands.add(command);
                }
                double chance = this.fileDelegate.getDouble("ce." + target + ".chance");
                if (location != null && world != null && commands.size() > 0 && chance != 0.0) {
                    this.chests.add(new Chest(location, world, commands, chance, true));
                    continue;
                }
                this.logger.warning("loadFileConfig population failed.");
                return false;
            }
            return true;
        }
        return false;
    }

    public String cc(String message) {
        return ChatColor.translateAlternateColorCodes((char)'&', (String)message);
    }

}
