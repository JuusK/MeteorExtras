package me.juusk.meteorextras.modules;

import me.juusk.meteorextras.MeteorExtras;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;

public class TooManyPackets extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Integer> threshold = sgGeneral.add(new IntSetting.Builder()
        .name("packet-threshold")
        .description("The maximum packets per second for it to cancel packets.")
        .defaultValue(300)
        .min(0)
        .sliderMax(800)
        .build()
    );



    public TooManyPackets() {
        super(MeteorExtras.CATEGORY, "Cancels packets to stop you from getting kicked for 'Too many packets'", "TooManyPackets");
    }
    int packetCounter = 0;
    int tick = 0;


    @EventHandler
    public void onTick(TickEvent.Pre e) {
        if(tick >= 20) {
            packetCounter = 0;
            tick = 0;
        }
        tick++;
    }

    @EventHandler
    public void onPacket(PacketEvent.Send event) {
        if(packetCounter >= threshold.get()) {
            event.cancel();
        }
        packetCounter++;
    }
}
