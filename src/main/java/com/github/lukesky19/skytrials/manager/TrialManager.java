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
package com.github.lukesky19.skytrials.manager;

import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.configuration.loader.LocaleLoader;
import com.github.lukesky19.skytrials.configuration.record.Locale;
import com.github.lukesky19.skytrials.configuration.loader.PlayerLoader;
import com.github.lukesky19.skytrials.configuration.record.PlayerData;
import com.github.lukesky19.skytrials.configuration.record.TrialConfig;
import com.github.lukesky19.skytrials.data.Trial;
import com.github.lukesky19.skytrials.util.FormatUtil;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultConfig;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.TrialSpawner;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Vault;
import org.bukkit.block.spawner.SpawnRule;
import org.bukkit.block.spawner.SpawnerEntry;
import org.bukkit.craftbukkit.CraftLootTable;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.spawner.TrialSpawnerConfiguration;
import org.jetbrains.annotations.NotNull;

import javax.annotation.CheckForNull;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.TimeUnit;

public final class TrialManager {
    private final SkyTrials skyTrials;
    private final PlayerLoader playerLoader;
    private final LocaleLoader localeLoader;
    private final ComponentLogger logger;
    private final List<Trial> trialsList = new ArrayList<>();
    private final ZoneId zoneId = ZoneId.of( "America/New_York" );
    private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL);

    public TrialManager(SkyTrials skyTrials, PlayerLoader playerLoader, LocaleLoader localeLoader) {
        this.skyTrials = skyTrials;
        this.playerLoader = playerLoader;
        this.localeLoader = localeLoader;
        logger = skyTrials.getComponentLogger();
        scheduleCooldownCheck();
    }

    public List<Trial> getTrialsList() {
        return trialsList;
    }

    /**
     * Create a Trial based on a TrialConfig object.
     * @param trialConfig The TrialConfig representing a Trial.
     */
    public void createTrial(@NotNull TrialConfig trialConfig) {
        // Locale
        Locale locale = localeLoader.getLocale();
        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trialConfig.trialId()));

        TrialConfig.RegionData regionData = trialConfig.regionData();

        World trialWorld = skyTrials.getServer().getWorld(regionData.world());

        if(trialWorld == null) {
            logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
            return;
        }

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(trialWorld));
        if(regions == null) return;
        ProtectedRegion trialRegion = regions.getRegion(trialConfig.regionData().region());

        if(trialRegion == null) {
            logger.error(FormatUtil.format(locale.invalidRegion(), placeholders));
            return;
        }

        trialsList.add(
                new Trial(
                        trialConfig.trialId(),
                        trialConfig.regionData().joinArea(),
                        trialConfig.regionData().startArea(),
                        trialConfig.regionData().exitArea(),
                        trialRegion,
                        trialConfig.cooldownSeconds().longValue(),
                        trialConfig.timeLimitSeconds().longValue(),
                        trialConfig.trialSpawners(),
                        trialConfig.vaults()));
    }

    /**
     * When a player attempts to join a Trial.
     */
    public void playerJoinsTrial(String trialId, UUID uuid, Player player) {
        Locale locale = localeLoader.getLocale();
        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trialId));
        placeholders.add(Placeholder.parsed("player_name", player.getName()));

        Trial trial = getTrialById(trialId);
        if(trial == null) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidTrialId(), placeholders));
            return;
        }

        if(trial.isPlayerInTrial(uuid)) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.joinTrialInTrial(), placeholders));
            return;
        }

        if(trial.isPlayerOnCooldown(uuid)) {
            Long playerCooldown = trial.getPlayerCooldown(uuid);
            if(playerCooldown == null) return;

            String cooldownString = getFormattedCooldown(playerCooldown);
            placeholders.add(Placeholder.parsed("time", cooldownString));

            player.sendMessage(FormatUtil.format(locale.prefix() + locale.joinTrialOnCooldown(), placeholders));
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.trialOnCooldown(), placeholders));
            return;
        }

        if(trial.getStatus()) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.joinTrialActive(), placeholders));
            return;
        }

        TrialConfig.LocationData joinLoc = trial.getJoinLocation();
        World world = Bukkit.getWorld(joinLoc.world());
        if(world == null) {
            logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
            return;
        }
        Location loc = new Location(world, joinLoc.x(), joinLoc.y(), joinLoc.z());

        trial.addPlayer(uuid);
        player.teleportAsync(loc);
        player.sendMessage(FormatUtil.format(locale.prefix() + locale.joinTrial(), placeholders));
        for(Player p : skyTrials.getServer().getOnlinePlayers()) {
            p.sendMessage(FormatUtil.format(locale.prefix() + locale.playerJoinedTrial(), placeholders));
        }
    }

    /**
     * When a player wishes to toggle their ready status.
     * If all players in the trial have agreed to start, actually start the Trial.
     */
    public void playerIsReady(UUID uuid, Player player) {
        // Locale
        Locale locale = localeLoader.getLocale();

        Trial trial = getTrialByPlayer(uuid);
        if(trial == null) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.readyNotInTrial(), new ArrayList<>()));
            return;
        }

        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));
        placeholders.add(Placeholder.parsed("player_name", player.getName()));

        boolean status = !trial.getPlayerReadyStatus(uuid);
        trial.setPlayerReadyStatus(uuid, status);

        if(status) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.ready(), placeholders));
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.readyInfo(), placeholders));

            for(Map.Entry<UUID, Boolean> playerMap : trial.getPlayerMap().entrySet()) {
                Player p = skyTrials.getServer().getPlayer(playerMap.getKey());
                if(p != null) {
                    p.sendMessage(FormatUtil.format(locale.prefix() + locale.playerIsReady(), placeholders));
                }
            }
        } else {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.notReady(), placeholders));
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.notReadyInfo(), placeholders));
            for(Map.Entry<UUID, Boolean> playerMap : trial.getPlayerMap().entrySet()) {
                Player p = skyTrials.getServer().getPlayer(playerMap.getKey());
                if(p != null) {
                    p.sendMessage(FormatUtil.format(locale.prefix() + locale.playerIsNotReady(), placeholders));
                }
            }
        }

        if(trial.arePlayersReady()) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.trialStarting(), placeholders));
            startTrial(trial);
        }
    }

    /**
     * When a player uses the /skytrial leave command
     * A cooldown will apply whether the trial was completed or not
     */
    public void playerLeavesTrial(UUID uuid, Player player) {
        Locale locale = localeLoader.getLocale();
        Trial trial = getTrialByPlayer(uuid);
        if(trial == null) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.invalidTrialId(), new ArrayList<>()));
            return;
        }

        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));
        placeholders.add(Placeholder.parsed("player_name", player.getName()));

        TrialConfig.LocationData exitLoc = trial.getExitLocation();
        World world = Bukkit.getWorld(exitLoc.world());
        if(world == null) {
            logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
            return;
        }
        Location exitLocation = new Location(world, exitLoc.x(), exitLoc.y(), exitLoc.z());

        if(trial.getStatus()) {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.leaveTrial(), placeholders));
            player.teleportAsync(exitLocation);
            trial.removePlayer(uuid);
            trial.addCooldown(uuid);
            Long cooldown = trial.getPlayerCooldown(uuid);
            if(cooldown != null) {
                placeholders.add(Placeholder.parsed("time", getFormattedCooldown(cooldown)));
                player.sendMessage(FormatUtil.format(locale.prefix() + locale.leaveTrialCooldown(), placeholders));
                savePlayerCooldown(uuid, trial.getTrialId(), cooldown);
            }
        } else {
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.leaveTrial(), placeholders));
            player.teleportAsync(exitLocation);
            trial.removePlayer(uuid);
        }

        if(trial.getPlayerMap().isEmpty()) {
            lastPlayerLeftEndTrial(trial);
        }
    }

    private void scheduleCooldownCheck() {
        Bukkit.getScheduler().runTaskTimer(skyTrials, () -> {
            for(Trial trial : trialsList) {
                for(Map.Entry<UUID, Long> cooldownEntry : trial.getCooldowns().entrySet()) {
                    Long time = cooldownEntry.getValue();
                    if(time <= System.currentTimeMillis()) {
                        UUID uuid = cooldownEntry.getKey();
                        Player player = skyTrials.getServer().getPlayer(uuid);
                        trial.removeCooldown(uuid);
                        if(player != null) {
                            Locale locale = localeLoader.getLocale();
                            // Placeholders
                            List<TagResolver.Single> placeholders = new ArrayList<>();
                            placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));

                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.trialCooldownEnds(), placeholders));
                        }
                    }
                }
            }
        }, (20L * 60 * 15), (20L * 60 * 15)); /* 15 mins */
    }

    private void scheduleTimeLimitCheck(Trial trial) {
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(skyTrials, () -> {
            if(trial.getEndTime() != null) {
                if (trial.getEndTime() <= System.currentTimeMillis()) {
                    endTrial(trial);
                } else {
                    Locale locale = localeLoader.getLocale();
                    for (Map.Entry<UUID, Boolean> playerEntry : trial.getPlayerMap().entrySet()) {
                        Player player = skyTrials.getServer().getPlayer(playerEntry.getKey());
                        if(player != null) {
                            // Placeholders
                            List<TagResolver.Single> placeholders = new ArrayList<>();
                            placeholders.add(Placeholder.parsed("time", getRemainingTime(trial.getEndTime())));

                            player.sendMessage(FormatUtil.format(locale.prefix() + locale.trialRemainingTime(), placeholders));
                        }
                    }
                }
            }
        }, 1L, 20L * 300);

        trial.setTimeLimitTask(task);
    }

    public String getFormattedCooldown(@NotNull Long time) {
        Instant instant = Instant.ofEpochMilli(time);
        ZonedDateTime zdt = instant.atZone( zoneId );

        return formatter.format(zdt);
    }

    public String getRemainingTime(@NotNull Long time) {
        long leftover = time - System.currentTimeMillis();

        long days = TimeUnit.MILLISECONDS.toDays(leftover);
        long hours = TimeUnit.MILLISECONDS.toHours(leftover) % 24;
        long minutes = TimeUnit.MILLISECONDS.toMinutes(leftover) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(leftover) % 60;

        return days + " day(s) " + hours + " hour(s) " + minutes + " minute(s) " + seconds + " second(s)";
    }

    @CheckForNull
    public String getFormattedCooldownByTrialId(@NotNull UUID uuid, @NotNull String trialId) {
        Trial trial = getTrialById(trialId);
        if(trial != null) {
            Long cooldown = trial.getPlayerCooldown(uuid);
            if(cooldown != null) return getFormattedCooldown(cooldown);
        }

        return null;
    }

    public void handlePlayerLogOut(Player player) {
        final UUID uuid = player.getUniqueId();
        Trial trial = getTrialByPlayer(uuid);
        if(trial == null) return;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(skyTrials, () -> {
            if(trial.getPlayerCooldown(uuid) != null) {
                trial.removePlayer(uuid);
                trial.addCooldown(uuid);
                Long cooldown = trial.getPlayerCooldown(uuid);
                trial.removePlayerGracePeriod(uuid);
                savePlayerCooldown(uuid, trial.getTrialId(), cooldown);
            }
        }, 20L * 300);

        trial.setPlayerGracePeriod(uuid, task);
    }

    /**
     * If a player logs in a region that is a Trial, teleport them to spawn or whatever safe area.
     */
    public void handlePlayerLogin(UUID uuid, Player player) {
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionManager regions = container.get(BukkitAdapter.adapt(player.getWorld()));
        if(regions == null) return;

        Locale locale = localeLoader.getLocale();

        for(Trial trial : trialsList) {
            BukkitTask task = trial.getPlayerGracePeriod(uuid);
            if(task != null) {
                task.cancel();
                trial.removePlayerGracePeriod(uuid);
                return;
            }

            ApplicableRegionSet regionSet = regions.getApplicableRegions(BlockVector3.at(player.getX(), player.getY(), player.getZ()));
            for(ProtectedRegion region : regionSet) {
                if(trial.getRegion().getId().equals(region.getId())) {
                    // Placeholders
                    List<TagResolver.Single> placeholders = new ArrayList<>();
                    placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));

                    TrialConfig.LocationData exitLoc = trial.getExitLocation();
                    World world = Bukkit.getWorld(exitLoc.world());
                    if(world == null) {
                        logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
                        return;
                    }
                    Location exitLocation = new Location(world, exitLoc.x(), exitLoc.y(), exitLoc.z());


                    player.teleportAsync(exitLocation);
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.trialEndedWhileOffline(), new ArrayList<>()));
                    return;
                }
            }
        }

        loadPlayerCooldowns(player);
    }

    public void handlePlayerDeath(UUID uuid, Player player) {
        Trial trial = getTrialByPlayer(uuid);
        if(trial == null) return;

        final Locale locale = localeLoader.getLocale();

        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));

        // If there is only 1 player in the trial, use regular logic
        if(trial.getPlayerMap().size() == 1) {
            endTrial(trial);
            return;
        }

        TrialConfig.LocationData exitLoc = trial.getExitLocation();
        World world = Bukkit.getWorld(exitLoc.world());
        if(world == null) {
            logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
            return;
        }
        Location exitLocation = new Location(world, exitLoc.x(), exitLoc.y(), exitLoc.z());
        player.teleportAsync(exitLocation);

        trial.removePlayer(uuid);
        trial.addCooldown(uuid);

        Long cooldown = trial.getPlayerCooldown(uuid);
        if(cooldown != null) {
            placeholders.add(Placeholder.parsed("time", getFormattedCooldown(cooldown)));
            player.sendMessage(FormatUtil.format(locale.prefix() + locale.playerDiedInTrial(), placeholders));

            savePlayerCooldown(uuid, trial.getTrialId(), cooldown);
        }
    }

    private void startTrial(Trial trial) {
        Locale locale = localeLoader.getLocale();

        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));

        trial.setStatus(true);
        trial.setEndTime(System.currentTimeMillis() + (trial.getTimeLimitSeconds() * 1000));
        scheduleTimeLimitCheck(trial);

        TrialConfig.LocationData startLoc = trial.getStartLocation();
        World world = Bukkit.getWorld(startLoc.world());
        if(world == null) {
            logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
            return;
        }
        Location startLocation = new Location(world, startLoc.x(), startLoc.y(), startLoc.z());

        for(Map.Entry<UUID, Boolean> players : trial.getPlayerMap().entrySet()) {
            Player p = skyTrials.getServer().getPlayer(players.getKey());
            if(p != null) {
                p.teleportAsync(startLocation);
            }
        }

        for(Player p : skyTrials.getServer().getOnlinePlayers()) {
            p.sendMessage(FormatUtil.format(locale.prefix() + locale.trialHasStarted(), placeholders));
        }

        removeEntities(trial);

        placeTrialSpawners(trial);
        placeVaults(trial);
    }

    private void endTrial(Trial trial) {
        Locale locale = localeLoader.getLocale();

        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));

        if(trial.getTimeLimitTask() != null && !trial.getTimeLimitTask().isCancelled()) {
            trial.getTimeLimitTask().cancel();
        }

        removeSpawners(trial);
        removeVaults(trial);

        for(Map.Entry<UUID, BukkitTask> taskEntry : trial.getGracePeriods().entrySet()) {
            UUID uuid = taskEntry.getKey();
            BukkitTask task = taskEntry.getValue();

            task.cancel();
            trial.removePlayerGracePeriod(uuid);
            trial.removePlayer(uuid);
            trial.addCooldown(uuid);
            Long cooldown = trial.getPlayerCooldown(uuid);
            savePlayerCooldown(uuid, trial.getTrialId(), cooldown);
        }

        for(Map.Entry<UUID, Boolean> playerEntry : trial.getPlayerMap().entrySet()) {
            UUID uuid = playerEntry.getKey();
            Player player = skyTrials.getServer().getPlayer(uuid);

            TrialConfig.LocationData exitLoc = trial.getExitLocation();
            World world = Bukkit.getWorld(exitLoc.world());
            if(world == null) {
                logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
                return;
            }
            Location exitLocation = new Location(world, exitLoc.x(), exitLoc.y(), exitLoc.z());

            if(player != null) {
                player.teleportAsync(exitLocation);
            }

            if(player != null) {
                player.sendMessage(FormatUtil.format(locale.prefix() + locale.trialTimeLimitEnd(), new ArrayList<>()));
            }

            trial.removePlayer(uuid);
            trial.addCooldown(uuid);
            Long cooldown = trial.getPlayerCooldown(uuid);
            if (cooldown != null) {
                savePlayerCooldown(uuid, trial.getTrialId(), cooldown);
            }
        }

        trial.setStatus(false);
        trial.setTimeLimitTask(null);
        trial.setEndTime(null);
    }

    private void lastPlayerLeftEndTrial(Trial trial) {
        final Locale locale = localeLoader.getLocale();
        BukkitTask task = trial.getTimeLimitTask();
        if(task != null && !task.isCancelled()) {
            task.cancel();
        }

        removeSpawners(trial);
        removeVaults(trial);

        for(Map.Entry<UUID, BukkitTask> taskEntry : trial.getGracePeriods().entrySet()) {
            UUID uuid = taskEntry.getKey();
            BukkitTask graceTask = taskEntry.getValue();

            graceTask.cancel();
            trial.removePlayerGracePeriod(uuid);
            trial.removePlayer(uuid);
            trial.addCooldown(uuid);
            Long cooldown = trial.getPlayerCooldown(uuid);
            savePlayerCooldown(uuid, trial.getTrialId(), cooldown);
        }

        trial.setStatus(false);
        trial.setTimeLimitTask(null);
        trial.setEndTime(null);
    }

    private void placeTrialSpawners(Trial trial) {
        final Locale locale = localeLoader.getLocale();

        // Fetch the enchantment registry from the registry access
        final Registry<Enchantment> enchantmentRegistry = RegistryAccess
                .registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT);

        for(Map.Entry<Integer, TrialConfig.TrialSpawnerData> trialSpawnerDataEntry : trial.getTrialSpawnersConfig().entrySet()) {
            TrialConfig.TrialSpawnerData trialSpawnerData = trialSpawnerDataEntry.getValue();
            TrialConfig.LocationData locData = trialSpawnerData.locationData();
            TrialConfig.SpawnerConfig normal = trialSpawnerData.normal();
            TrialConfig.SpawnerConfig ominous = trialSpawnerData.ominous();

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));
            placeholders.add(Placeholder.parsed("block_id", trialSpawnerDataEntry.getKey().toString()));

            // Set Block to Trial Spawner
            World world = Bukkit.getWorld(trialSpawnerData.locationData().world());
            if (world == null) {
                logger.error(FormatUtil.format(locale.invalidBlockWorld(), placeholders));
                return;
            }

            int x = trialSpawnerData.locationData().x();
            int y = trialSpawnerData.locationData().y();
            int z = trialSpawnerData.locationData().z();
            Location location = new Location(world, x, y, z);

            BlockState blockState = world.getBlockState(location);
            blockState.setType(Material.TRIAL_SPAWNER);
            blockState.update(true);

            // Get updated block state from the world
            blockState = world.getBlockState(location);
            if(blockState instanceof TrialSpawner trialSpawner) {
                @NotNull TrialSpawnerConfiguration normalConfiguration = trialSpawner.getNormalConfiguration();
                @NotNull TrialSpawnerConfiguration ominousConfiguration = trialSpawner.getOminousConfiguration();

                // Normal
                normalConfiguration.setSpawnRange(normal.spawnRange());
                normalConfiguration.setRequiredPlayerRange(normal.playerRange());
                normalConfiguration.setBaseSimultaneousEntities(normal.simultaneousMobs());
                normalConfiguration.setAdditionalSimultaneousEntities(normal.simultaneousMobsAddedPerPlayer());

                HashMap<LootTable, Integer> normalLootTables = new HashMap<>();
                for(Map.Entry<Integer, TrialConfig.LootTableData> dataEntry : normal.lootTables().entrySet()) {
                    TrialConfig.LootTableData lootTableData = dataEntry.getValue();
                    NamespacedKey key = NamespacedKey.fromString(lootTableData.name(), null);

                    LootTable lootTable = null;
                    if (key != null) {
                        lootTable = skyTrials.getServer().getLootTable(key);
                    }
                    if(lootTable != null) {
                        normalLootTables.put(lootTable, lootTableData.weight());
                    }
                }

                normalConfiguration.setPossibleRewards(normalLootTables);

                for (Map.Entry<Integer, TrialConfig.SpawnPotential> spawnPotentialEntry : normal.spawnPotentials().entrySet()) {
                    TrialConfig.SpawnPotential spawnPotential = spawnPotentialEntry.getValue();
                    SpawnerEntry spawnerEntry = createSpawnerEntry(world, location, spawnPotential);
                    if(spawnerEntry != null) {
                        normalConfiguration.addPotentialSpawn(spawnerEntry);
                    }
                }

                // Ominous
                ominousConfiguration.setSpawnRange(ominous.spawnRange());
                ominousConfiguration.setRequiredPlayerRange(ominous.playerRange());
                ominousConfiguration.setBaseSimultaneousEntities(ominous.simultaneousMobs());
                ominousConfiguration.setAdditionalSimultaneousEntities(ominous.simultaneousMobsAddedPerPlayer());

                HashMap<LootTable, Integer> ominousLootTables = new HashMap<>();
                for(Map.Entry<Integer, TrialConfig.LootTableData> dataEntry : ominous.lootTables().entrySet()) {
                    TrialConfig.LootTableData lootTableData = dataEntry.getValue();
                    NamespacedKey key = NamespacedKey.fromString(lootTableData.name(), null);
                    LootTable lootTable = null;
                    if(key != null) {
                        lootTable = skyTrials.getServer().getLootTable(key);
                    }
                    if(lootTable != null) {
                        ominousLootTables.put(lootTable, lootTableData.weight());
                    }
                }
                
                ominousConfiguration.setPossibleRewards(ominousLootTables);

                for (Map.Entry<Integer, TrialConfig.SpawnPotential> spawnPotentialEntry : ominous.spawnPotentials().entrySet()) {
                    TrialConfig.SpawnPotential spawnPotential = spawnPotentialEntry.getValue();
                    SpawnerEntry spawnerEntry = createSpawnerEntry(world, location, spawnPotential);
                    if(spawnerEntry != null) {
                        ominousConfiguration.addPotentialSpawn(spawnerEntry);
                    }
                }

                trialSpawner.update();
            }
        }
    }

    @CheckForNull
    private SpawnerEntry createSpawnerEntry(World world, Location location, TrialConfig.SpawnPotential spawnPotential) {
        EntityType entity = EntityType.valueOf(spawnPotential.entity().toLowerCase());
        int weight = spawnPotential.weight();

        Entity spawnEntity = world.spawnEntity(location, entity);
        LivingEntity livingEntity = (LivingEntity) spawnEntity;
        EntityEquipment equipment = livingEntity.getEquipment();
        if(equipment != null) {
            setEntityEquipment(equipment, spawnPotential.equipment());
        }

        EntitySnapshot entitySnapshot = livingEntity.createSnapshot();
        livingEntity.remove();
        if(entitySnapshot != null) {
            SpawnRule spawnRule = new SpawnRule(0, 15, 0, 15);
            return new SpawnerEntry(entitySnapshot, spawnPotential.weight(), spawnRule);
        }

        return null;
    }

    private void setEntityEquipment(@NotNull EntityEquipment equipment, TrialConfig.EquipmentData equipmentData) {
        // Fetch the enchantment registry from the registry access
        final Registry<Enchantment> enchantmentRegistry = RegistryAccess
                .registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT);

        if (equipmentData.helmet() != null && equipmentData.helmet().name() != null) {
            ItemStack itemStack = new ItemStack(Material.valueOf(equipmentData.helmet().name()));
            for (Map.Entry<Integer, TrialConfig.EnchantmentData> enchantmentEntry : equipmentData.helmet().enchantments().entrySet()) {
                TrialConfig.EnchantmentData enchantmentData = enchantmentEntry.getValue();

                final Enchantment enchantment = enchantmentRegistry.getOrThrow(TypedKey.create(
                        RegistryKey.ENCHANTMENT, Key.key("minecraft:" + enchantmentData.name().toLowerCase())));

                itemStack.addEnchantment(enchantment, enchantmentData.level());
            }
            equipment.setHelmet(itemStack);
        }

        if (equipmentData.chestplate() != null && equipmentData.chestplate().name() != null) {
            ItemStack itemStack = new ItemStack(Material.valueOf(equipmentData.chestplate().name()));
            for (Map.Entry<Integer, TrialConfig.EnchantmentData> enchantmentEntry : equipmentData.chestplate().enchantments().entrySet()) {
                TrialConfig.EnchantmentData enchantmentData = enchantmentEntry.getValue();

                final Enchantment enchantment = enchantmentRegistry.getOrThrow(TypedKey.create(
                        RegistryKey.ENCHANTMENT, Key.key("minecraft:" + enchantmentData.name().toLowerCase())));

                itemStack.addEnchantment(enchantment, enchantmentData.level());
            }
            equipment.setChestplate(itemStack);
        }

        if (equipmentData.leggings() != null && equipmentData.leggings().name() != null) {
            ItemStack itemStack = new ItemStack(Material.valueOf(equipmentData.leggings().name()));
            for (Map.Entry<Integer, TrialConfig.EnchantmentData> enchantmentEntry : equipmentData.leggings().enchantments().entrySet()) {
                TrialConfig.EnchantmentData enchantmentData = enchantmentEntry.getValue();

                final Enchantment enchantment = enchantmentRegistry.getOrThrow(TypedKey.create(
                        RegistryKey.ENCHANTMENT, Key.key("minecraft:" + enchantmentData.name().toLowerCase())));

                itemStack.addEnchantment(enchantment, enchantmentData.level());
            }
            equipment.setLeggings(itemStack);
        }

        if (equipmentData.boots() != null && equipmentData.boots().name() != null) {
            ItemStack itemStack = new ItemStack(Material.valueOf(equipmentData.boots().name()));
            for (Map.Entry<Integer, TrialConfig.EnchantmentData> enchantmentEntry : equipmentData.boots().enchantments().entrySet()) {
                TrialConfig.EnchantmentData enchantmentData = enchantmentEntry.getValue();

                final Enchantment enchantment = enchantmentRegistry.getOrThrow(TypedKey.create(
                        RegistryKey.ENCHANTMENT, Key.key("minecraft:" + enchantmentData.name().toLowerCase())));

                itemStack.addEnchantment(enchantment, enchantmentData.level());
            }
            equipment.setBoots(itemStack);
        }

        if (equipmentData.mainHand() != null && equipmentData.mainHand().name() != null) {
            ItemStack itemStack = new ItemStack(Material.valueOf(equipmentData.mainHand().name()));
            for (Map.Entry<Integer, TrialConfig.EnchantmentData> enchantmentEntry : equipmentData.mainHand().enchantments().entrySet()) {
                TrialConfig.EnchantmentData enchantmentData = enchantmentEntry.getValue();

                final Enchantment enchantment = enchantmentRegistry.getOrThrow(TypedKey.create(
                        RegistryKey.ENCHANTMENT, Key.key("minecraft:" + enchantmentData.name().toLowerCase())));

                itemStack.addEnchantment(enchantment, enchantmentData.level());
            }
            equipment.setItemInMainHand(itemStack);
        }

        if (equipmentData.offHand() != null && equipmentData.offHand().name() != null) {
            ItemStack itemStack = new ItemStack(Material.valueOf(equipmentData.offHand().name()));
            for (Map.Entry<Integer, TrialConfig.EnchantmentData> enchantmentEntry : equipmentData.offHand().enchantments().entrySet()) {
                TrialConfig.EnchantmentData enchantmentData = enchantmentEntry.getValue();

                final Enchantment enchantment = enchantmentRegistry.getOrThrow(TypedKey.create(
                        RegistryKey.ENCHANTMENT, Key.key("minecraft:" + enchantmentData.name().toLowerCase())));

                itemStack.addEnchantment(enchantment, enchantmentData.level());
            }
            equipment.setItemInOffHand(itemStack);
        }
    }

    private void placeVaults(Trial trial) {
        final Locale locale = localeLoader.getLocale();

        for(Map.Entry<Integer, TrialConfig.VaultData> vaultDataEntry : trial.getVaultBlocksConfig().entrySet()) {
            TrialConfig.VaultData vaultBlockData = vaultDataEntry.getValue();
            TrialConfig.LocationData locData = vaultBlockData.locationData();

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));
            placeholders.add(Placeholder.parsed("block_id", vaultDataEntry.getKey().toString()));

            World world = Bukkit.getWorld(vaultBlockData.locationData().world());
            if(world == null) {
                logger.error(FormatUtil.format(locale.invalidBlockWorld(), placeholders));
                return;
            }

            Location location = new Location(world, vaultBlockData.locationData().x(), vaultBlockData.locationData().y(), vaultBlockData.locationData().z());

            NamespacedKey key = NamespacedKey.fromString(vaultBlockData.lootTable(), null);
            LootTable lootTable = null;
            if (key != null) {
                lootTable = skyTrials.getServer().getLootTable(key);
            }
            ResourceKey<net.minecraft.world.level.storage.loot.LootTable> nmsLootTable = CraftLootTable.bukkitToMinecraft(lootTable);

            BlockState blockState = world.getBlockState(location);
            blockState.setType(Material.VAULT);
            blockState.update(true);

            blockState = world.getBlockState(location);
            BlockData blockData = blockState.getBlockData();

            CraftWorld craftWorld = (CraftWorld) world;
            ServerLevel level = craftWorld.getHandle();
            BlockPos blockPos = new BlockPos(vaultBlockData.locationData().x(), vaultBlockData.locationData().y(), vaultBlockData.locationData().z());

            if(blockData instanceof Vault vault) {
                if(vaultBlockData.type().equals("OMINOUS")) {
                    vault.setOminous(true);
                    blockState.setBlockData(vault);
                    blockState.update(true);
                }

                if(vault.isOminous()) {
                    ItemStack ominousKey = new ItemStack(Material.OMINOUS_TRIAL_KEY);

                    BlockEntity blockEntity = level.getBlockEntity(blockPos);
                    if(blockEntity instanceof VaultBlockEntity vaultBlockEntity) {
                        VaultConfig vaultConfig = vaultBlockEntity.getConfig();
                        if (nmsLootTable != null) {
                            VaultConfig newVaultConfig = new VaultConfig(nmsLootTable, vaultConfig.activationRange(), vaultConfig.deactivationRange(), net.minecraft.world.item.ItemStack.fromBukkitCopy(ominousKey), Optional.of(nmsLootTable), vaultConfig.playerDetector(), vaultConfig.entitySelector());
                            vaultBlockEntity.setConfig(newVaultConfig);
                        }
                    }
                } else {
                    BlockEntity blockEntity = level.getBlockEntity(blockPos);
                    if(blockEntity instanceof VaultBlockEntity vaultBlockEntity) {
                        VaultConfig vaultConfig = vaultBlockEntity.getConfig();
                        if (nmsLootTable != null) {
                            VaultConfig newVaultConfig = new VaultConfig(nmsLootTable, vaultConfig.activationRange(), vaultConfig.deactivationRange(), vaultConfig.keyItem(), Optional.of(nmsLootTable), vaultConfig.playerDetector(), vaultConfig.entitySelector());
                            vaultBlockEntity.setConfig(newVaultConfig);
                        }
                    }
                }
            }
        }
    }

    public void removeSpawners(Trial trial) {
        final Locale locale = localeLoader.getLocale();

        for(Map.Entry<Integer, TrialConfig.TrialSpawnerData> trialSpawnerDataEntry : trial.getTrialSpawnersConfig().entrySet()) {
            TrialConfig.TrialSpawnerData trialSpawnerData = trialSpawnerDataEntry.getValue();

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));
            placeholders.add(Placeholder.parsed("block_id", trialSpawnerDataEntry.getKey().toString()));

            // Set Block to Trial Spawner
            World world = Bukkit.getWorld(trialSpawnerData.locationData().world());
            if (world == null) {
                logger.error(FormatUtil.format(locale.invalidBlockWorld(), placeholders));
                return;
            }

            int x = trialSpawnerData.locationData().x();
            int y = trialSpawnerData.locationData().y();
            int z = trialSpawnerData.locationData().z();
            Location location = new Location(world, x, y, z);

            BlockState blockState = world.getBlockState(location);
            blockState.setType(Material.AIR);
            blockState.update(true);
        }
    }

    public void removeVaults(Trial trial) {
        final Locale locale = localeLoader.getLocale();

        for(Map.Entry<Integer, TrialConfig.VaultData> vaultDataEntry : trial.getVaultBlocksConfig().entrySet()) {
            TrialConfig.VaultData vaultBlockData = vaultDataEntry.getValue();

            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));
            placeholders.add(Placeholder.parsed("block_id", vaultDataEntry.getKey().toString()));

            World world = Bukkit.getWorld(vaultBlockData.locationData().world());
            if(world == null) {
                logger.error(FormatUtil.format(locale.invalidBlockWorld(), placeholders));
                return;
            }

            Location location = new Location(world, vaultBlockData.locationData().x(), vaultBlockData.locationData().y(), vaultBlockData.locationData().z());

            BlockState blockState = world.getBlockState(location);
            blockState.setType(Material.AIR);
            blockState.update(true);
        }
    }

    private void removeEntities(Trial trial) {
        final Locale locale = localeLoader.getLocale();

        // Placeholders
        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));

        TrialConfig.LocationData startLoc = trial.getStartLocation();
        World world = Bukkit.getWorld(startLoc.world());
        if(world == null) {
            logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
            return;
        }
        Location startLocation = new Location(world, startLoc.x(), startLoc.y(), startLoc.z());

        Collection<Entity> entities = world.getNearbyEntities(startLocation, 30, 30, 30);
        for(Entity entity : entities) {
            if(!(entity instanceof Player)) {
                entity.remove();
            }
        }
    }

    /**
     * Gets the Trial a Player is currently in or null if not.
     * @param uuid The UUID of the Player.
     * @return A Trial object or null.
     */
    @CheckForNull
    private Trial getTrialByPlayer(@NotNull UUID uuid) {
        for(Trial trial : trialsList) {
            if(trial.getPlayerMap().containsKey(uuid)) {
                return trial;
            }
        }

        return null;
    }

    /**
     * Gets a Trial based on it's Trial ID or null.
     * @param trialId The Trial ID of a Trial
     * @return A Trial object or null
     */
    @CheckForNull
    private Trial getTrialById(String trialId) {
        for(Trial trial : trialsList) {
            if(trial.getTrialId().equals(trialId)) {
                return trial;
            }
        }
        return null;
    }

    /**
     * Clears all trials of players and teleports them to exit areas.
     * Is used on reloads.
     */
    public void clearTrials() {
        Locale locale = localeLoader.getLocale();
        for(Trial trial : trialsList) {
            // Placeholders
            List<TagResolver.Single> placeholders = new ArrayList<>();
            placeholders.add(Placeholder.parsed("trial_id", trial.getTrialId()));

            for(Map.Entry<UUID, Boolean> entry : trial.getPlayerMap().entrySet()) {
                UUID uuid = entry.getKey();
                Player player = skyTrials.getServer().getPlayer(uuid);

                TrialConfig.LocationData exitLoc = trial.getExitLocation();
                World world = Bukkit.getWorld(exitLoc.world());
                if(world == null) {
                    logger.error(FormatUtil.format(locale.invalidWorld(), placeholders));
                    return;
                }
                Location exitLocation = new Location(world, exitLoc.x(), exitLoc.y(), exitLoc.z());

                if(player != null) {
                    player.sendMessage(FormatUtil.format(locale.prefix() + locale.reloadWhileInTrial(), new ArrayList<>()));
                    player.teleportAsync(exitLocation);
                }

                trial.removePlayer(uuid);
            }

            removeSpawners(trial);
            removeVaults(trial);
        }

        trialsList.clear();
    }

    /**
     * Loads any active player cooldowns for the current trials.
     * Any trial ids that don't match or cooldowns that have expired will be removed from that player's config.
     * @param player The Player to load cooldowns for.
     */
    public void loadPlayerCooldowns(Player player) {
        final Locale locale = localeLoader.getLocale();
        final UUID uuid = player.getUniqueId();
        PlayerData playerData = playerLoader.loadPlayerData(uuid);

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("player_name", player.getName()));

        if(playerData == null) {
            logger.error(FormatUtil.format(locale.invalidPlayerData(), placeholders));
            return;
        }

        HashMap<String, Long> playerCooldowns = new HashMap<>();
        for(Map.Entry<String, Long> playerCooldownsEntry : playerData.cooldowns().entrySet()) {
            String trialId = playerCooldownsEntry.getKey();
            Long cooldown = playerCooldownsEntry.getValue();
            for(Trial trial : trialsList) {
                if(trial.getTrialId().equals(trialId)) {
                    if(cooldown > System.currentTimeMillis()) {
                        trial.addCooldown(uuid);
                        playerCooldowns.put(trialId, cooldown);
                    }
                }
            }
        }

        // Only save cooldowns that haven't been completed
        playerLoader.savePlayerData(uuid, new PlayerData(playerData.configVersion(), playerCooldowns));
    }

    /**
     * Save a cooldown when it's added.
     * @param uuid The UUID of the player
     * @param trialId The ID of the Trial
     * @param cooldown The cooldown
     */
    private void savePlayerCooldown(UUID uuid, String trialId, Long cooldown) {
        final Locale locale = localeLoader.getLocale();
        PlayerData playerData = playerLoader.loadPlayerData(uuid);

        List<TagResolver.Single> placeholders = new ArrayList<>();
        placeholders.add(Placeholder.parsed("uuid", uuid.toString()));

        if(playerData == null) {
            logger.error(FormatUtil.format(locale.invalidUuidData(), placeholders));
            return;
        }

        HashMap<String, Long> playerCooldowns = playerData.cooldowns();
        playerCooldowns.put(trialId, cooldown);
        playerLoader.savePlayerData(uuid, new PlayerData(playerData.configVersion(), playerCooldowns));
    }
}
