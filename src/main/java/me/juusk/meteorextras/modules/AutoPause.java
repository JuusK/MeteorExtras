package me.juusk.meteorextras.modules;

import me.juusk.meteorextras.MeteorExtras;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;

import java.util.List;

public class AutoPause extends Module {


    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Module>> whitelist = sgGeneral.add(new ModuleListSetting.Builder()
        .name("whitelist")
        .description("Which modules to pause.")
        .defaultValue(
            KillAura.class
        )
        .build()
    );

    private final Setting<Integer> pause = sgGeneral.add(new IntSetting.Builder()
        .name("pause-ticks")
        .description("After how many ticks the module(s) should pause.")
        .sliderMax(1000)
        .defaultValue(100)
        .build()
    );

    private final Setting<Integer> resume = sgGeneral.add(new IntSetting.Builder()
        .name("resume-ticks")
        .description("After how many ticks the module(s) should resume.")
        .sliderMax(1000)
        .defaultValue(20)
        .build()
    );
    public int tick = 0;
    public boolean paused = false;

    public AutoPause() {
        super(MeteorExtras.CATEGORY, "AutoPause", "Automatically pauses and resumes modules after a certain threshold.");
    }

    @Override
    public void onActivate() {
        tick = 0;
        paused = false;
        super.onActivate();
    }

    @Override
    public void onDeactivate() {
        tick = 0;
        paused = false;
        super.onDeactivate();
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        tick++;
        if(!paused) {
            if(tick >= pause.get()) {
                paused = true;
                tick = 0;
            }
        } else {
            if(tick >= resume.get()) {
                paused = false;
                tick = 0;
            }
        }



        if(paused) {
            for(Module m : whitelist.get()) {
                if(m.isActive()) {
                    m.toggle();
                }
            }
        } else {
            for(Module m : whitelist.get()) {
                if(!m.isActive()) {
                    m.toggle();
                }
            }
        }
    }
}
