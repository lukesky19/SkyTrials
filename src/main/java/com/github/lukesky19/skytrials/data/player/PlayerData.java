package com.github.lukesky19.skytrials.data.player;

import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains player cooldowns and grace periods for trial ids.
 */
public class PlayerData {
    private @NonNull Map<String, Long> cooldowns = new HashMap<>();
    private final @NonNull Map<String, Long> gracePeriods = new HashMap<>();

    /**
     * Constructor
     */
    public PlayerData() {}

    /**
     * Constructor
     * @param cooldowns A {@link Map} mapping trial ids to cooldown time.
     */
    public PlayerData(@NonNull Map<String, Long> cooldowns) {
        this.cooldowns = cooldowns;
    }

    /**
     * Get the {@link Map} of trial ids to cooldown time in seconds.
     * @return A {@link Map} mapping trial ids to cooldown time in seconds.
     */
    public @NonNull Map<String, Long> getCooldownsMap() {
        return cooldowns;
    }

    /**
     * Get the {@link Map} of trial ids to grace period time in seconds.
     * @return A {@link Map} mapping trial ids to grace period time in seconds.
     */
    public @NonNull Map<String, Long> getGracePeriodsMap() {
        return gracePeriods;
    }
}
