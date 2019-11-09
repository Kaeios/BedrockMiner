package me.rellynn.plugins.bedrockminer;

import org.bukkit.Location;

public class WorldSetting {

    private final String worldName;
    private boolean enabled;
    private final int min;
    private final int max;

    public WorldSetting(String worldName, int min, int max, final boolean enabled) {
        this.worldName = worldName;
        this.min = min;
        this.max = max;
        this.enabled = enabled;
    }

    public LocationStatus isValidLocation(final Location location){
        if(!location.getWorld().getName().equals(worldName)) return LocationStatus.DEFAULT;
        if(!enabled) return LocationStatus.DENY;
        if(location.getY() < min) return LocationStatus.DENY;
        if(location.getY() > max) return LocationStatus.DENY;

        return LocationStatus.ALLOW;
    }

    public enum LocationStatus {

        ALLOW,
        DENY,
        DEFAULT

    }

}
