/*
    SkyTrials is a mob arena plugin inspired by the Minecraft 1.21 Trial Chambers using Trial Spawners and Vault blocks.
    Copyright (C) 2024  lukeskywlker19

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
package com.github.lukesky19.skytrials.configuration.record;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public record Locale(
        String configVersion,
        String prefix,
        String reload,
        String reloadWhileInTrial,
        List<String> help,
        String noPermission,
        String unknownArgument,
        String inGameOnly,
        String invalidWorld,
        String invalidRegion,
        String invalidTrialId,
        String invalidBlockWorld,
        String invalidPlayerData,
        String invalidUuidData,
        String joinTrialInTrial,
        String joinTrialOnCooldown,
        String joinTrialActive,
        String joinTrial,
        String playerJoinedTrial,
        String ready,
        String notReady,
        String playerIsReady,
        String playerIsNotReady,
        String readyInfo,
        String notReadyInfo,
        String readyNotInTrial,
        String startTrialInTrial,
        String trialStarting,
        String trialHasStarted,
        String leaveTrialNotInTrial,
        String leaveTrial,
        String leaveTrialCooldown,
        String playerDiedInTrial,
        String trialRemainingTime,
        String trialCooldownEnds,
        String trialEndedWhileOffline,
        String trialTimeLimitEnd,
        String trialOnCooldown,
        String trialNotOnCooldown) { }
