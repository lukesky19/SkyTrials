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
package com.github.lukesky19.skytrials.util;

/**
 * This enum is used to identify why a trial ends.
 */
public enum TrialEndReason {
    /**
     * When a trial ends due to a trial's time ending.
     */
    TIMEOUT,
    /**
     * When a trial ends due to all levels in a trial being completed.
     */
    COMPLETED,
    /**
     * When a trial ends due to no players left in the trial.
     */
    EMPTY,
    /**
     * When a trial ends due to the last player dying and no other players being left in the trial.
     */
    DEATH,
    /**
     * When a trial ends due to a plugin reload.
     */
    RELOAD
}
