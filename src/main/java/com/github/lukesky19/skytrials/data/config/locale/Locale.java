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
package com.github.lukesky19.skytrials.data.config.locale;

import com.github.lukesky19.skylib.libs.configurate.objectmapping.ConfigSerializable;

import java.util.List;

/**
 * This record contains the locale configuration for the plugin's messages.
 * @param configVersion The config version of the file.
 * @param prefix The plugin's prefix appended to most if not all messages.
 * @param reload The reload message.
 * @param help The plugin's help message.
 * @param joinTrial The message sent to the player when they join a trial.
 * @param playerJoinedTrial The message sent to other players when another player joins the trial.
 * @param trialOnCooldown The message sent when a player can't join a trial due to a cooldown.
 * @param joinTrialActive The message sent when a player can't join a trial due to it being active.
 * @param joinTrialInTrial The player sent when a player can't join a trial because they are in a trial.
 * @param ready The message sent to the player when they indicate they are ready to start the trial.
 * @param notReady The message sent to the player when they indicate they are no longer ready to start the trial.
 * @param playerIsReady The message sent to other players when a player is ready to start the trial.
 * @param playerIsNotReady The message sent to other players when a player is no longer ready to start the trial.
 * @param startTrial The message sent to the players in the trial when a trial starts.
 * @param broadcastTrialStart The message sent to all other players when a trial starts.
 * @param startTrialActive The message sent to a player who attempts to start the trial when the trial is active.
 * @param startTrialNotInTrial The message sent to a player who attempts to start a trial when not in a trial.
 * @param leaveTrial The message sent to the player that leaves a trial.
 * @param playerLeaveTrial The message sent to all other players that leaves a trial.
 * @param leaveTrialNotInTrial The message sent to the player that attempts to leave a trial while not in one.
 * @param trialEnd The message sent to all players in the trial when a trial ends.
 * @param trialEndReload The message sent to all players in the trial when a trial ends due to a plugin reload.
 * @param trialEndedWhileOffline The message sent to a player when a trial they were in ended while they were offline.
 * @param broadcastTrialEnd The message sent to all other players when a trial ends.
 * @param diedInTrial The message sent to the player who dies in a trial.
 * @param playerDiedInTrial The message sent to all other players when a player dies in the trial.
 * @param levelUp The message sent to all players in the trial when a level up occurs.
 * @param cooldownApplied The message sent when a cooldown is applied for a trial.
 * @param playerCooldownApplied The message sent to the player who added a cooldown for another player.
 * @param cooldownRemoved The message sent when a cooldown is removed for a trial.
 * @param playerCooldownRemoved The message sent to the player who removed a cooldown for another player.
 * @param cooldownEnded The message sent when a trial cooldown ends.
 * @param cooldownTime The message sent to display a trial's cooldown.
 * @param playerCooldownTime The message to view a player's trial cooldown.
 * @param noCooldown The message sent when a player doesn't have a cooldown.
 * @param playerNoCooldown The message sent when another player doesn't have a cooldown.
 * @param timeMessage The {@link TimeMessage} config for the time placeholder.
 */
@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        String reload,
        List<String> help,
        String joinTrial,
        String playerJoinedTrial,
        String trialOnCooldown,
        String joinTrialActive,
        String joinTrialInTrial,
        String ready,
        String notReady,
        String playerIsReady,
        String playerIsNotReady,
        String startTrial,
        String broadcastTrialStart,
        String startTrialActive,
        String startTrialNotInTrial,
        String leaveTrial,
        String playerLeaveTrial,
        String leaveTrialNotInTrial,
        String trialEnd,
        String trialEndReload,
        String trialEndedWhileOffline,
        String broadcastTrialEnd,
        String diedInTrial,
        String playerDiedInTrial,
        String levelUp,
        String cooldownApplied,
        String playerCooldownApplied,
        String cooldownRemoved,
        String playerCooldownRemoved,
        String cooldownEnded,
        String cooldownTime,
        String playerCooldownTime,
        String noCooldown,
        String playerNoCooldown,
        TimeMessage timeMessage) {
    /**
     * Configuration for the time placeholder.
     * @param prefix The text to display before the first time unit.
     * @param years The text to display when the player's time enters years.
     * @param months The text to display when the player's time enters months.
     * @param weeks The text to display when the player's time enters weeks.
     * @param days The text to display when the player's time enters days.
     * @param hours The text to display when the player's time enters hours.
     * @param minutes The text to display when the player's time enters minutes.
     * @param seconds The text to display when the player's time enters seconds.
     * @param suffix The text to display after the last time unit.
     */
    @ConfigSerializable
    public record TimeMessage(
            String prefix,
            String years,
            String months,
            String weeks,
            String days,
            String hours,
            String minutes,
            String seconds,
            String suffix) {}
}