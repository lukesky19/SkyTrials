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
package com.github.lukesky19.skytrials;

import com.github.lukesky19.skylib.api.adventure.AdventureUtil;
import com.github.lukesky19.skylib.api.version.VersionUtil;
import com.github.lukesky19.skytrials.command.SkyTrialsCommand;
import com.github.lukesky19.skytrials.manager.locale.LocaleManager;
import com.github.lukesky19.skytrials.manager.settings.SettingsManager;
import com.github.lukesky19.skytrials.manager.trial.TrialConfigManager;
import com.github.lukesky19.skytrials.database.ConnectionManager;
import com.github.lukesky19.skytrials.database.DatabaseManager;
import com.github.lukesky19.skytrials.database.QueueManager;
import com.github.lukesky19.skytrials.listener.*;
import com.github.lukesky19.skytrials.manager.blocks.SpawnerManager;
import com.github.lukesky19.skytrials.manager.blocks.VaultManager;
import com.github.lukesky19.skytrials.manager.entity.EntityManager;
import com.github.lukesky19.skytrials.manager.player.CooldownManager;
import com.github.lukesky19.skytrials.manager.player.GracePeriodManager;
import com.github.lukesky19.skytrials.manager.player.PlayerDataManager;
import com.github.lukesky19.skytrials.manager.task.TaskManager;
import com.github.lukesky19.skytrials.manager.trial.TrialDataManager;
import com.github.lukesky19.skytrials.manager.trial.TrialManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

/**
 * This class is the entry point to the plugin.
 */
public final class SkyTrials extends JavaPlugin {
    private SettingsManager settingsManager;
    private LocaleManager localeManager;
    private TrialConfigManager trialConfigManager;
    private TrialDataManager trialDataManager;
    private TrialManager trialManager;
    private DatabaseManager databaseManager;
    private PlayerDataManager playerDataManager;
    private TaskManager taskManager;

    /**
     * Default Constructor.
     */
    public SkyTrials() {}

    /**
     * Enables the plugin and initializes all data.
     */
    @Override
    public void onEnable() {
        if(!versionCheck()) return;
        if(!checkSkyLibVersion()) return;

        settingsManager = new SettingsManager(this);
        localeManager = new LocaleManager(this, settingsManager);

        EntityManager entityManager = new EntityManager(this);
        SpawnerManager spawnerManager = new SpawnerManager(this);
        VaultManager vaultManager = new VaultManager(this);

        ConnectionManager connectionManager = new ConnectionManager(this);
        QueueManager queueManager = new QueueManager(connectionManager);
        databaseManager = new DatabaseManager(connectionManager, queueManager);

        playerDataManager = new PlayerDataManager(databaseManager);

        CooldownManager cooldownManager = new CooldownManager(this, localeManager, playerDataManager, databaseManager);
        GracePeriodManager gracePeriodManager = new GracePeriodManager(playerDataManager);

        trialConfigManager = new TrialConfigManager(this);
        trialDataManager = new TrialDataManager(this, trialConfigManager);
        trialManager = new TrialManager(this, localeManager, trialDataManager, spawnerManager, entityManager, vaultManager, cooldownManager, gracePeriodManager);

        taskManager = new TaskManager(this, trialManager, playerDataManager, cooldownManager, gracePeriodManager);

        taskManager.startTimerTask();
        taskManager.startPlayerDataSaveTask();

        SkyTrialsCommand skyTrialsCommand = new SkyTrialsCommand(this, trialManager, localeManager, cooldownManager);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands ->
                        commands.registrar().register(skyTrialsCommand.createCommand(),
                                "Command to manage and use the SkyTrials plugin.",
                                List.of("trials", "skytrial", "trial")));

        PluginManager pluginManager = this.getServer().getPluginManager();
        pluginManager.registerEvents(new LoginListener(trialManager), this);
        pluginManager.registerEvents(new LogoutListener(trialManager), this);
        pluginManager.registerEvents(new PlayerDeathListener(trialManager), this);
        pluginManager.registerEvents(new EntityDeathListener(trialManager), this);
        pluginManager.registerEvents(new SpawnerSpawnListener(trialManager), this);
        pluginManager.registerEvents(new PlayerDeathListener(trialManager), this);
        pluginManager.registerEvents(new EntityPotionEffectListener(trialManager), this);

        playerDataManager.loadPlayerData();

        reload();
    }

    /**
     * Disables the plugin. This cleans up and saves any data as necessary.
     */
    @Override
    public void onDisable() {
        if(taskManager != null) {
            taskManager.stopTimerTask();
            taskManager.stopPlayerDataSaveTask();
        }

        if(trialManager != null) trialManager.clearTrials();

        if(playerDataManager != null && databaseManager != null) {
            playerDataManager.savePlayerData().whenComplete((v, t) ->
                    databaseManager.handlePluginDisable());
        }
    }

    /**
     * Reloads the plugin.
     */
    public void reload() {
        trialManager.clearTrials();

        settingsManager.reload();
        localeManager.reload();
        trialConfigManager.reload();
        trialDataManager.createTrialData();
        trialManager.createTrials();
    }

    /**
     * Checks if the Server has the proper SkyLib version.
     * @return true if it does, false if not.
     */
    private boolean checkSkyLibVersion() {
        PluginManager pluginManager = this.getServer().getPluginManager();
        Plugin skyLib = pluginManager.getPlugin("SkyLib");
        if (skyLib != null) {
            String version = skyLib.getPluginMeta().getVersion();
            String[] splitVersion = version.split("\\.");
            int second = Integer.parseInt(splitVersion[1]);

            if(second >= 3) {
                return true;
            }
        }

        this.getComponentLogger().error(AdventureUtil.serialize("SkyLib Version 1.3.0.0 or newer is required to run this plugin."));
        this.getServer().getPluginManager().disablePlugin(this);
        return false;
    }

    /**
     * Checks if the server is running a supported version.
     * @return true if supported, otherwise false.
     */
    private boolean versionCheck() {
        int major = VersionUtil.getMajorVersion();
        int minor = VersionUtil.getMinorVersion();

        if(major < 21 && minor < 3) {
            this.getComponentLogger().error(AdventureUtil.serialize("This plugin currently only supports versions 1.21.4, 1.21.5, 1.21.6, and 1.21.7."));
            this.getComponentLogger().error(AdventureUtil.serialize("If a new Minecraft version has released, please check my <click:OPEN_URL:https://github.com/lukesky19>GitHub</click> for updates."));
            this.getServer().getPluginManager().disablePlugin(this);
            return false;
        }

        return true;
    }
}
