package com.github.lukesky19.skytrials.listener;

import com.github.lukesky19.skytrials.manager.TrialManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class DeathListener implements Listener {
    private final TrialManager trialManager;

    public DeathListener(TrialManager trialManager) {
        this.trialManager = trialManager;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        trialManager.handlePlayerDeath(uuid, player);
    }
}
