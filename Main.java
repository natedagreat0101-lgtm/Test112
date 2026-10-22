package com.unbound.halloween;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("HalloweenCountdown v2.0.0 successfully loaded!");

        // 1. Actionbar Countdown Task (Runs every second / 20 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                
                // Target: Upcoming Sunday at 12:00:00 PM
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

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.sendActionBar(actionbarComponent);
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        // 2. Spooky Ambient Sound Loop Task (Plays every 18 seconds / 360 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.stopSound(Sound.MUSIC_GAME);
                    player.stopSound(Sound.MUSIC_CREATIVE);
                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.75f);
                }
            }
        }.runTaskTimer(this, 0L, 360L);
    }

    @Override
    public void onDisable() {
        getLogger().info("HalloweenCountdown disabled!");
    }
}
