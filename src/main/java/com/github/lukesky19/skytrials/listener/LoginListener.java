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
package com.github.lukesky19.skytrials.listener;

import com.github.lukesky19.skytrials.configuration.loader.PlayerLoader;
import com.github.lukesky19.skytrials.manager.TrialManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class LoginListener implements Listener {
    private final TrialManager trialManager;
    private final PlayerLoader playerLoader;

    public LoginListener(TrialManager trialManager, PlayerLoader playerLoader) {
        this.trialManager = trialManager;
        this.playerLoader = playerLoader;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        playerLoader.createPlayerData(uuid);
        trialManager.handlePlayerLogin(uuid, player);
    }
}
