package io.fredde.chestExecute;

import java.util.List;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class Chest {
    private Vector location;
    private World world;
    private List<String> commands;
    private double chance;
    private boolean active;

    public Chest(Vector location, World world, List<String> commands, double chance, boolean active) {
        this.location = location;
        this.world = world;
        this.commands = commands;
        this.chance = chance;
        this.active = active;
    }

    public Vector getLocation() {
        return this.location;
    }

    public void setLocation(Vector location) {
        this.location = location;
    }

    public World getWorld() {
        return this.world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public List<String> getCommands() {
        return this.commands;
    }

    public void setCommands(List<String> commands) {
        this.commands = commands;
    }

    public double getChance() {
        return this.chance;
    }

    public void setChance(double chance) {
        this.chance = chance;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
