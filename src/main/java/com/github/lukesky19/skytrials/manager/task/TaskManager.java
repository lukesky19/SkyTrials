/*
    SkyTrials is a plugin that offers different challenges or trials to tackle. Inspired by the Minecraft Trial Chambers and mob arenas.
    Copyright (C) 2024 lukeskywlker19

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published
    by the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.github.lukesky19.skytrials.manager.task;

import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.manager.player.CooldownManager;
import com.github.lukesky19.skytrials.manager.player.GracePeriodManager;
import com.github.lukesky19.skytrials.manager.player.PlayerDataManager;
import com.github.lukesky19.skytrials.manager.trial.TrialManager;
import com.github.lukesky19.skytrials.trial.AbstractTrial;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * This class manages {@link BukkitTask}s for the plugin.
 */
public class TaskManager {
    private final @NotNull SkyTrials skyTrials;
    private final @NotNull TrialManager trialManager;
    private final @NotNull PlayerDataManager playerDataManager;
    private final @NotNull CooldownManager cooldownManager;
    private final @NotNull GracePeriodManager gracePeriodManager;

    private @Nullable BukkitTask timerTask;
    private @Nullable BukkitTask playerDataSaveTask;

    /**
     * Constructor
     * @param skyTrials A {@link SkyTrials} instance.
     * @param trialManager A {@link TrialManager} instance.
     * @param playerDataManager A {@link PlayerDataManager} instance.
     * @param cooldownManager A {@link CooldownManager} instance.
     * @param gracePeriodManager A {@link GracePeriodManager} instance.
     */
    public TaskManager(
            @NotNull SkyTrials skyTrials,
            @NotNull TrialManager trialManager,
            @NotNull PlayerDataManager playerDataManager,
            @NotNull CooldownManager cooldownManager,
            @NotNull GracePeriodManager gracePeriodManager) {
        this.skyTrials = skyTrials;
        this.trialManager = trialManager;
        this.playerDataManager = playerDataManager;
        this.cooldownManager = cooldownManager;
        this.gracePeriodManager = gracePeriodManager;
    }

    /**
     * Start the task that runs every second to decrement trial timers, cooldowns, and grace periods.
     */
    public void startTimerTask() {
        timerTask = skyTrials.getServer().getScheduler().runTaskTimer(skyTrials, () -> {
            trialManager.getTrials().forEach(AbstractTrial::decrementTime);

            cooldownManager.decrementCooldowns();

            gracePeriodManager.decrementGracePeriods();
        }, 20L, 20L);
    }

    /**
     * Start the task that saves player data every 15 minutes.
     */
    public void startPlayerDataSaveTask() {
        playerDataSaveTask = skyTrials.getServer().getScheduler().runTaskTimer(skyTrials, () ->
                playerDataManager.savePlayerData(), 20L * 60 * 15, 20L * 60 * 15);
    }

    /**
     * Stop the task that runs every second to decrement trial timers, cooldowns, and grace periods.
     */
    public void stopTimerTask() {
        if(timerTask != null && !timerTask.isCancelled()) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    /**
     * Stop the task that saves player data every 15 minutes.
     */
    public void stopPlayerDataSaveTask() {
        if(playerDataSaveTask != null && !playerDataSaveTask.isCancelled()) {
            playerDataSaveTask.cancel();
            playerDataSaveTask = null;
        }
    }
}
