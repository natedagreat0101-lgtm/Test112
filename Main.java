package com.unbound.halloween;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Month;

public class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("HalloweenCountdown plugin enabled!");

        // Actionbar Countdown Task (Updates every second / 20 ticks)
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalDateTime now = LocalDateTime.now();
                // Sunday, Oct 25, 2026 at 12:00 PM
                LocalDateTime target = LocalDateTime.of(2026, Month.OCTOBER, 25, 12, 0, 0);

                Duration duration = Duration.between(now, target);

                String message;
                if (duration.isNegative() || duration.isZero()) {
                    message = ChatColor.GOLD + "" + ChatColor.BOLD + "🎃 HALLOWEEN EVENT IS LIVE! 🎃";
                } else {
                    long days = duration.toDays();
                    long hours = duration.toHours() % 24;
                    long minutes = duration.toMinutes() % 60;
                    long seconds = duration.getSeconds() % 60;

                    message = String.format("%s%sHalloween Event in: %s%dd %02dh %02dm %02ds",
                            ChatColor.RED, ChatColor.BOLD, ChatColor.YELLOW, days, hours, minutes, seconds);
                }

                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
                }
            }
        }.runTaskTimer(this, 0L, 20L);

        // Music Loop Task (Plays spooky ambient audio every 18 seconds)
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.stopSound(Sound.MUSIC_GAME);
                    player.stopSound(Sound.MUSIC_CREATIVE);
                    player.playSound(player.getLocation(), Sound.AMBIENT_CAVE, 1.0f, 0.75f);
                }
            }
        }.runTaskTimer(this, 0L, 360L); // 360 ticks = 18 seconds
    }

    @Override
    public void onDisable() {
        getLogger().info("HalloweenCountdown plugin disabled!");
    }
}
