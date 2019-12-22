package fr.cocoraid.prodigyxray;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.util.*;

/**
 * Created by cocoraid on 19/06/2017.
 */
public class ProdigyXray extends JavaPlugin  implements Listener {


    private static ProdigyXray instance;
    private Scoreboard scoreboard;
    private ProdigyXrayConfig px;
    public Economy economy = null;


    private List<Team> teams = new ArrayList<>();
    private void setupTeam() {
        Xray.getMap().keySet().forEach(m -> {
            Team team = scoreboard.registerNewTeam(m.name());
            team.setColor(Xray.getMap().get(m));
            teams.add(team);
        });
    }



    @Override
    public void onEnable() {
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        instance = this;
        Bukkit.getPluginManager().registerEvents(this, this);
        try {
            px = new ProdigyXrayConfig(new File("plugins/ProdigyXray", "prodigyxray.yml"));
            px.load();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }


        try {
            RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
            }
        }catch(java.lang.NoClassDefFoundError e) {

        }


        ConsoleCommandSender c = Bukkit.getServer().getConsoleSender();

        c.sendMessage(" ");
        c.sendMessage(" ");

        c.sendMessage(CC.red +  "_____           _ _         __ __");
        c.sendMessage(CC.red + "|  _  |___ ___ _| |_|___ _ _|  |  |___ ___ _ _");
        c.sendMessage(CC.red + "|   __|  _| . | . | | . | | |-   -|  _| .'| | |");
        c.sendMessage(CC.red + "|__|  |_| |___|___|_|_  |_  |__|__|_| |__,|_  |");
        c.sendMessage(CC.red + "                    |___|___|             |___|");
        c.sendMessage(" ");
        c.sendMessage(CC.d_green + "The Prodigy is the man who knows where to find ores...");
        c.sendMessage(" ");
        c.sendMessage(CC.d_green + "Depency: " + (getServer().getPluginManager().getPlugin("ProtocolLib")!=null ? "§a✔" : "§4✘"));
        if(getServer().getPluginManager().getPlugin("ProtocolLib")==null) {
            c.sendMessage(" §c You must install ProtocolLib");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        c.sendMessage(" ");
        c.sendMessage(CC.d_green + "Optional Depencies: ");
        c.sendMessage(CC.green + "    - Vault Economy: " + (economy != null ? "§a✔" : "§4✘"));
        if(px.buyable && economy == null) {
            c.sendMessage(" §c You have enabled buyable system in the config while vault is not installed");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }


        setupTeam();
        new HitFallingBlockListener(this);

        new BukkitRunnable() {

            @Override
            public void run() {

                cooldown.keySet().removeIf(id -> {
                    if(cooldown.get(id) <= 0)
                        return true;
                    else {
                        cooldown.put(id, cooldown.get(id) - 1);
                        return false;
                    }
                });

            }
        }.runTaskTimer(ProdigyXray.getInstance(), 20, 20);
    }

    @Override
    public void onDisable() {
        Xray.getXrayers().values().forEach(xray -> xray.stop());
        teams.forEach(t -> t.unregister());
    }

    @EventHandler
    public void quit(PlayerQuitEvent e) {
        if(Xray.getXrayers().containsKey(e.getPlayer().getUniqueId()))
            Xray.getXrayers().remove(e.getPlayer().getUniqueId());
    }

    private static Map<UUID, Integer> cooldown = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (command.getName().equalsIgnoreCase("prodigyxray") || command.getName().equalsIgnoreCase("px") ) {
                if (args.length == 0) {
                    if(p.hasPermission("prodigy.xray")) {
                        if(!Xray.getXrayers().containsKey(p.getUniqueId())) {
                            if (px.cooldown > 0 && cooldown.containsKey(p.getUniqueId())) {
                                p.sendMessage(px.cooldownmsg.replace("&", "§").replace("%time", String.valueOf(cooldown.get(p.getUniqueId()))));
                                return false;
                            }

                            new Xray(p).start();

                        } else p.sendMessage(px.alreadyusing.replace("&","§"));
                    } else
                        p.sendMessage(px.permission.replace("&","§"));
                } else if(args.length == 1) {
                    try {
                        if (Xray.getMap().containsKey(Material.valueOf(args[0].toUpperCase() + "_ORE"))) {
                            if(!px.disabledOre.contains(args[0].toLowerCase())) {
                                if (p.hasPermission("prodigy.xray" + args[0].toLowerCase()) || p.hasPermission("prodigyxray.start.*")) {
                                    if (!Xray.getXrayers().containsKey(p.getUniqueId())) {
                                        new Xray(p).start(Material.valueOf(args[0].toUpperCase() + "_ORE"));
                                        if (px.cooldown > 0 && cooldown.containsKey(p.getUniqueId()) && !p.hasPermission("prodigy.xray.cooldown.bypass")) {
                                            p.sendMessage(px.cooldownmsg.replace("&", "§").replace("%time", String.valueOf(cooldown.get(p.getUniqueId()))));
                                            return false;
                                        }
                                    } else p.sendMessage(px.alreadyusing.replace("&", "§"));
                                } else
                                    p.sendMessage(px.permission.replace("&", "§"));
                            } else {
                                p.sendMessage(px.disabled.replace("&", "§"));
                            }
                        }
                    } catch(Exception e) {
                        List<String> ores = new ArrayList<>();
                        Xray.getMap().keySet().forEach(m -> {
                            ores.add(m.name().toLowerCase().replace("_ore",""));
                        });
                        px.disabledOre.forEach(s -> {
                            ores.remove(s);
                        });
                        String list = "";
                        for (String ore : ores) {
                            list = list + ", " + Xray.getMap().get(Material.valueOf(ore.toUpperCase() + "_ORE")) + ore;
                        }

                        p.sendMessage(px.available.replace("&", "§") + list);
                    }
                } else {
                    p.sendMessage("§c/px ; /px <ore>");
                }
            }
        }
        return false;

    }

    public static Map<UUID, Integer> getCooldown() {
        return cooldown;
    }

    public static ProdigyXray getInstance() {
        return instance;
    }

    public Scoreboard getScoreboard() {
        return scoreboard;
    }

    public ProdigyXrayConfig getPx() {
        return px;
    }
}
