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
package com.github.lukesky19.skytrials;

import com.github.lukesky19.skytrials.command.SkyTrialsCommand;
import com.github.lukesky19.skytrials.configuration.loader.LocaleLoader;
import com.github.lukesky19.skytrials.configuration.loader.PlayerLoader;
import com.github.lukesky19.skytrials.configuration.loader.SettingsLoader;
import com.github.lukesky19.skytrials.configuration.loader.TrialLoader;
import com.github.lukesky19.skytrials.listener.DeathListener;
import com.github.lukesky19.skytrials.listener.LoginListener;
import com.github.lukesky19.skytrials.listener.LogoutListener;
import com.github.lukesky19.skytrials.manager.TrialManager;
import com.github.lukesky19.skytrials.util.ConfigurationUtility;
import com.github.lukesky19.skytrials.util.FormatUtil;
import io.papermc.paper.ServerBuildInfo;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class SkyTrials extends JavaPlugin {
    private SettingsLoader settingsLoader;
    private TrialLoader trialLoader;
    private LocaleLoader localeLoader;
    private boolean pluginState = true;

    public boolean isPluginDisabled() {
        return !pluginState;
    }

    public void setPluginState(boolean state) {
        pluginState = state;
    }

    @Override
    public void onEnable() {
        versionCheck();

        ConfigurationUtility configurationUtility = new ConfigurationUtility();
        settingsLoader = new SettingsLoader(this, configurationUtility);
        PlayerLoader playerLoader = new PlayerLoader(this, configurationUtility);
        localeLoader = new LocaleLoader(this, configurationUtility, settingsLoader);

        TrialManager trialManager = new TrialManager(this, playerLoader, localeLoader);
        trialLoader = new TrialLoader(this, trialManager, configurationUtility);

        SkyTrialsCommand skyTrialsCommand = new SkyTrialsCommand(this, trialManager, localeLoader);

        LoginListener loginListener = new LoginListener(trialManager, playerLoader);
        LogoutListener logoutListener = new LogoutListener(trialManager);
        DeathListener deathListener = new DeathListener(trialManager);

        Bukkit.getServer().getPluginManager().registerEvents(loginListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(logoutListener, this);
        Bukkit.getServer().getPluginManager().registerEvents(deathListener, this);

        Objects.requireNonNull(Bukkit.getPluginCommand("skytrials")).setExecutor(skyTrialsCommand);
        Objects.requireNonNull(Bukkit.getPluginCommand("skytrials")).setTabCompleter(skyTrialsCommand);

        reload();
    }

    public void reload() {
        settingsLoader.reload();
        localeLoader.reload();
        trialLoader.reload();
    }

    private void versionCheck() {
        final ServerBuildInfo build = ServerBuildInfo.buildInfo();

        final String minecraftVersionId = build.minecraftVersionId();
        if(minecraftVersionId.equals("1.21.1")) return;

        this.getComponentLogger().error(FormatUtil.format("This plugin currently only supports version 1.21.1."));
        this.getComponentLogger().error(FormatUtil.format("If a new Minecraft version has released, please check my <click:OPEN_URL:https://github.com/lukesky19>GitHub</click> for updates."));
        this.getServer().getPluginManager().disablePlugin(this);
    }
}
