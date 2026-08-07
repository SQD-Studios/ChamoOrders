package net.chamosmp.ChamoOrders.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public final class SchedulerUtil {

    private SchedulerUtil() {
    }

    /**
     * Runs a sync task
     *
     * @param plugin The plugin instance (The plugin that owns the task)
     * @param task   The task to run
     */
    public static void runSync(@NotNull Plugin plugin, @NotNull Runnable task) {
        Bukkit.getGlobalRegionScheduler().run(plugin, t -> task.run());
    }

    /**
     * Runs a region task
     *
     * @param plugin   The plugin instance (The plugin that owns the task)
     * @param location The location to run it at
     * @param task     The task to run
     */
    public static void runAtLocation(@NotNull Plugin plugin, @NotNull Location location, @NotNull Runnable task) {
        Bukkit.getRegionScheduler().run(plugin, location, t -> task.run());
    }

    /**
     * Runs an async task immediately.
     *
     * @param plugin The plugin instance (The plugin that owns the task)
     * @param task   The task to run
     */
    public static void runAsync(@NotNull Plugin plugin, @NotNull Runnable task) {
        Bukkit.getAsyncScheduler().runNow(plugin, t -> task.run());
    }

    /**
     * Runs a task attached to an entity
     *
     * @param plugin   The plugin instance (The plugin that owns the task)
     * @param entity   The entity to attach it on
     * @param task     The task to run
     * @param fallback Retire callback to run if the entity is retired before the run callback can be invoked, may be null.
     */
    public static void runForEntity(@NotNull Plugin plugin, @NotNull Entity entity, @NotNull Runnable task, @NotNull Runnable fallback) {
        entity.getScheduler().run(plugin, t -> task.run(), fallback);
    }

    /**
     * Runs a sync task, delayed
     *
     * @param plugin     The plugin instance (The plugin that owns the task)
     * @param task       The task to run
     * @param delayTicks How much should it delay it in ticks
     */
    public static void runDelayed(@NotNull Plugin plugin, @NotNull Runnable task, long delayTicks) {
        Bukkit.getGlobalRegionScheduler().runDelayed(plugin, t -> task.run(), delayTicks);
    }
}
