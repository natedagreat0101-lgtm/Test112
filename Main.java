package com.unbound.halloween;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class Main extends JavaPlugin implements CommandExecutor, Listener {

    private final Set<UUID> enabledPlayers = new HashSet<>();
    private int soundIndex = 0;
    private final Sound[] spookySounds = {
        Sound.AMBIENT_CAVE,
        Sound.ENTITY_GHAST_WARN,
        Sound.ENTITY_ZOMBIE_VILLAGER_CONVERTED,
        Sound.EVENT_RAID_HORN,
        Sound.ENTITY_PHANTOM_DEATH
    };

    @Override
    public void onEnable() {
        getLogger().info("HalloweenCountdown v3.0 successfully loaded!");
        
        // Register events and commands
        getServer().getPluginManager().registerEvents(this, this);
        if (getCommand("timer") != null) {
            getCommand("timer").setExecutor(this);
        }

        // Enable for all currently online players upon reload/startup
        for (Player player : Bukkit.getOnlinePlayers()) {
            enabledPlayers.add(player.getUniqueId());
        }

        // 1. Actionbar Countdown Task (Runs every second / 20 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                LocalDateTime target = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
                                           .withHour(12).withMinute(0).withSecond(0).withNano(0);

                Duration duration = Duration.between(now, target);

                Component actionbarComponent;
                if (duration.isNegative() || duration.isZero()) {
                    actionbarComponent = Component.text("🎃 HALLOWEEN EVENT IS LIVE! 🎃")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD);
                } else {
                    long days = duration.toDays();
                    long hours = duration.toHours() % 24;
                    long minutes = duration.toMinutes() % 60;
                    long seconds = duration.getSeconds() % 60;

                    String timeStr = String.format("%dd %02dh %02dm %02ds", days, hours, minutes, seconds);

                    actionbarComponent = Component.text("Halloween Event in: ")
                            .color(NamedTextColor.RED)
                            .decorate(TextDecoration.BOLD)
                            .append(Component.text(timeStr).color(NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, false));
                }

                for (UUID uuid : enabledPlayers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.sendActionBar(actionbarComponent);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        // 2. Spooky Multi-Track Sound Loop Task (Plays rotating spooky audio every 18 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                Sound currentSpookySound = spookySounds[soundIndex];
                soundIndex = (soundIndex + 1) % spookySounds.length;

                for (UUID uuid : enabledPlayers) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null && player.isOnline()) {
                        player.stopSound(Sound.MUSIC_GAME);
                        player.stopSound(Sound.MUSIC_CREATIVE);
                        player.playSound(player.getLocation(), currentSpookySound, 0.8f, 0.8f);
                    }
                }
            }
        }.runTaskTimer(this, 0L, 360L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // Automatically ON by default when joining
        UUID uuid = event.getPlayer().getUniqueId();
        enabledPlayers.add(uuid);
        event.getPlayer().sendMessage(Component.text("§c[Halloween] §eCountdown & spooky music enabled! Type §b/timer off §eto disable."));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        enabledPlayers.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("off")) {
                enabledPlayers.remove(uuid);
                player.sendActionBar(Component.empty());
                player.sendMessage(Component.text("§c[Halloween] §eCountdown timer and spooky music turned OFF."));
                return true;
            } else if (args[0].equalsIgnoreCase("on")) {
                enabledPlayers.add(uuid);
                player.sendMessage(Component.text("§a[Halloween] §eCountdown timer and spooky music turned ON!"));
                return true;
            }
        }

        player.sendMessage(Component.text("§cUsage: /timer on §eor §c/timer off"));
        return true;
    }

    @Override
    public void onDisable() {
        getLogger().info("HalloweenCountdown disabled!");
    }
}
