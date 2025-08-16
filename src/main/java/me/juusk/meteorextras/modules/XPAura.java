package me.juusk.meteorextras.modules;

import me.juusk.meteorextras.MeteorExtras;
import me.juusk.meteorextras.utils.ModuleUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.compress.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class XPAura extends Module {

    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class, InfAura.class};

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("distance")
        .description("The distance you detect the orbs from.")
        .defaultValue(50)
        .min(0)
        .sliderMax(100)
        .build()
    );

    private final Setting<Boolean> pauseAuras = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-auras")
        .description("Pauses all auras when eating.")
        .defaultValue(true)
        .build()
    );

    public XPAura() {
        super(MeteorExtras.CATEGORY, "XPAura", "Teleports to xp orbs to pick them up and then tps back");
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onTick(TickEvent.Pre event) {
        for (Entity e : mc.world.getEntities()) {
            if(e instanceof ExperienceOrbEntity) {
                if(mc.player.distanceTo(e) <= distance.get() && PlayerUtils.canSeeEntity(e)) {
                    final List<Class<? extends Module>> wasAura = new ArrayList<>();
                    if(pauseAuras.get()) {
                        for (Class<? extends Module> klass : AURAS) {
                            Module module = Modules.get().get(klass);

                            if (module.isActive()) {
                                wasAura.add(klass);
                                module.toggle();
                            }
                        }
                    }

                    Vec3d startPos = mc.player.getPos();
                    Vec3d endPos = e.getPos();


                    ModuleUtils.splitTeleport(startPos, endPos, 8.5D, 0);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException exc) {
                        exc.printStackTrace();
                    }
                    ModuleUtils.splitTeleport(endPos, startPos, 8.5D, 0);
                    if(pauseAuras.get()) {
                        for (Class<? extends Module> klass : wasAura) {
                            Module module = Modules.get().get(klass);
                            module.toggle();
                        }
                    }
                }
            }
        }
    }


}
