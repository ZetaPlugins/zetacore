package com.zetaplugins.zetacore.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Utility class for serializing and deserializing Location objects to and from string format.
 */
public final class LocationSerializer {
    private LocationSerializer() {}

    public interface WorldProvider {
        World getWorld(String name);
    }

    public static final WorldProvider DEFAULT_PROVIDER = Bukkit::getWorld;

    /**
     * Serializes a location into a string format: "worldName,x,y,z,yaw,pitch"
     * @param worldName The name of the world
     * @param x The x coordinate
     * @param y The y coordinate
     * @param z The z coordinate
     * @param yaw The yaw rotation
     * @param pitch The pitch rotation
     * @return The serialized location string
     */
    public static String serializeLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        return worldName + "," + x + "," + y + "," + z + "," + yaw + "," + pitch;
    }

    /**
     * Serializes a Location object into a string format: "worldName,x,y,z,yaw,pitch"
     * @param location The Location object to serialize
     * @return The serialized location string
     */
    public static String serializeLocation(Location location) {
        return serializeLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ(),
                location.getYaw(), location.getPitch());
    }

    /**
     * Deserializes a location string back into a Location object. Expects format: "worldName,x,y,z,yaw,pitch"
     * @param serializedLocation The serialized location string
     * @return The deserialized Location object
     * @throws LocationDeserializationException if the string is malformed or the world does not exist
     */
    public static Location deserializeLocation(String serializedLocation) throws LocationDeserializationException {
        return deserializeLocation(serializedLocation, DEFAULT_PROVIDER);
    }

    /**
     * Deserializes a location string back into a Location object. Expects format: "worldName,x,y,z,yaw,pitch"
     * @param serializedLocation The serialized location string
     * @param provider The WorldProvider to use for fetching worlds
     * @return The deserialized Location object
     * @throws LocationDeserializationException if the string is malformed or the world does not exist
     */
    public static Location deserializeLocation(String serializedLocation, WorldProvider provider) throws LocationDeserializationException {
        if (serializedLocation.isEmpty()) throw new LocationDeserializationException("Serialized location string is empty.");

        String[] parts = serializedLocation.split(",");
        if (parts.length != 6) {
            throw new LocationDeserializationException("Invalid serialized location format. Expected 6 parts but got " + parts.length);
        }

        String worldName = parts[0];
        World world = provider.getWorld(worldName);
        if (world == null) throw new LocationDeserializationException("World not found: " + worldName);

        double x = Double.parseDouble(parts[1]);
        double y = Double.parseDouble(parts[2]);
        double z = Double.parseDouble(parts[3]);
        float yaw = Float.parseFloat(parts[4]);
        float pitch = Float.parseFloat(parts[5]);

        return new Location(world, x, y, z, yaw, pitch);
    }
}
