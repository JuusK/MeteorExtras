package me.juusk.meteorextras.utils;

import net.minecraft.text.Text;

import java.util.UUID;

public class BossBarExtension {
    public UUID uuid;
    public Text name;
    public int lastSeenTick;

    public BossBarExtension(UUID uuid, Text name, int currentTick) {
        this.uuid = uuid;
        this.name = name;
        this.lastSeenTick = currentTick;
    }



    public Text getName() {
        return name;
    }

}
