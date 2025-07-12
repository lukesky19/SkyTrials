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

import com.github.lukesky19.skytrials.SkyTrials;
import com.github.lukesky19.skytrials.data.config.misc.LocationConfig;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

/**
 * This class contains a utility method to create a {@link Location} from a {@link LocationConfig}.
 */
public class LocationUtil {
    /**
     * Default Constructor.
     * Use of the default constructor is not allowed. This class only contains static methods.
     * @deprecated Use of the default constructor is not allowed. This class only contains static methods.
     */
    @Deprecated
    public LocationUtil() {
        throw new RuntimeException("The use of the default constructor is not allowed.");
    }

    /**
     * Create a {@link Location} from {@link LocationConfig}.
     * @param skyTrials A {@link SkyTrials} instance.
     * @param locationConfig A {@link LocationConfig}.
     * @return A {@link Location} or null if creation failed.
     */
    public static @Nullable Location getLocation(@NotNull SkyTrials skyTrials, @NotNull LocationConfig locationConfig) {
        if(locationConfig.world() == null
                || locationConfig.x() == null
                || locationConfig.y() == null
                || locationConfig.z() == null) return null;

        float yaw = 0;
        if(locationConfig.yaw() != null) {
            yaw = locationConfig.yaw();
        }

        float pitch = 0;
        if(locationConfig.pitch() != null) {
            pitch = locationConfig.pitch();
        }

        World world = skyTrials.getServer().getWorld(locationConfig.world());
        if(world != null) {
            return new Location(world, locationConfig.x(), locationConfig.y(), locationConfig.z(), yaw, pitch);
        }

        return null;
    }
}