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
package com.github.lukesky19.skytrials.listener;

import com.github.lukesky19.skytrials.trial.AbstractTrial;
import com.github.lukesky19.skytrials.manager.trial.TrialManager;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles when an entity dies in a trial and passes the event to the trial.
 */
public class EntityDeathListener implements Listener {
    private final @NotNull TrialManager trialManager;

    /**
     * Constructor
     * @param trialManager A {@link TrialManager} instance.
     */
    public EntityDeathListener(@NotNull TrialManager trialManager) {
        this.trialManager = trialManager;
    }

    /**
     * When an entity dies, check if the location was in a trial and pass the event to the trial if necessary.
     * @param entityDeathEvent An {@link EntityDeathEvent}.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent entityDeathEvent) {
        Entity killedEntity = entityDeathEvent.getEntity();

        AbstractTrial trial = trialManager.getTrialByLocation(killedEntity.getLocation());
        if(trial != null) {
            trial.handleEntityDeath(entityDeathEvent);
        }
    }
}