package me.juusk.meteorextras.modules;

import me.juusk.meteorextras.MeteorExtras;
import me.juusk.meteorextras.utils.BossBarExtension;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.render.RenderBossBarEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixin.BossBarHudMixin;
import meteordevelopment.meteorclient.mixin.InGameHudMixin;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.combat.AnchorAura;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.systems.modules.combat.CrystalAura;
import meteordevelopment.meteorclient.systems.modules.combat.KillAura;
import meteordevelopment.meteorclient.systems.modules.player.AutoEat;
import meteordevelopment.meteorclient.systems.modules.player.AutoGap;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class AutoOminous extends Module {

    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class, InfAura.class};

    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<Boolean> pauseAuras = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-auras")
        .description("Pauses all auras when eating.")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> pauseBaritone = sgGeneral.add(new BoolSetting.Builder()
        .name("pause-baritone")
        .description("Pause baritone when eating.")
        .defaultValue(true)
        .build()
    );

    // Threshold



    public boolean drinking;
    private int slot, prevSlot;
    public ArrayList<BossBarExtension> bossBars = new ArrayList<>();
    private final List<Class<? extends Module>> wasAura = new ArrayList<>();
    private boolean wasBaritone = false;
    private int tick = 0;

    public AutoOminous() {
        super(MeteorExtras.CATEGORY, "AutoOminous", "Automatically drinks Ominous Bottles.");
    }
    @Override
    public void onDeactivate() {
        if (drinking) stopDrinking();
        bossBars.clear();
    }

    @EventHandler()
    private void onBossBar(RenderBossBarEvent.BossText event) {
        boolean contains = false;
        for(BossBarExtension extension : bossBars) {
            if(extension.uuid == event.bossBar.getUuid()) {
                contains = true;
                stopDrinking();
            }
        }
        if(!contains) {
            bossBars.add(new BossBarExtension(event.bossBar.getUuid(), event.bossBar.getName()));
        }
    }


    @EventHandler(priority = EventPriority.LOW)
    private void onTick(TickEvent.Pre event) {
        if(tick >= 20) { bossBars.clear(); tick = 0; }
        if (Modules.get().get(AutoGap.class).isEating()) return;
        if(Modules.get().get(AutoEat.class).eating) return;
        int slot = findSlot();
        if (drinking) {
            if(slot < 0) {
                stopDrinking();
                return;
            }


            changeSlot(slot);

            drink();
        } else{
            // Try to find a valid slot
            slot = findSlot();

            // If slot was found then start eating
            if (slot != -1) startDrinking();
        }
        tick++;
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (drinking) event.target = null;
    }

    private void startDrinking() {
        prevSlot = mc.player.getInventory().selectedSlot;
        drink();

        // Pause auras
        wasAura.clear();
        if (pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);

                if (module.isActive()) {
                    wasAura.add(klass);
                    module.toggle();
                }
            }
        }

        if (pauseBaritone.get() && PathManagers.get().isPathing() && !wasBaritone) {
            wasBaritone = true;
            PathManagers.get().pause();
        }
    }

    private void drink() {
        changeSlot(slot);
        setPressed(true);
        if (!mc.player.isUsingItem()) Utils.rightClick();

        drinking = true;
    }

    private void stopDrinking() {
        changeSlot(prevSlot);
        setPressed(false);

        drinking = false;

        // Resume auras
        if (pauseAuras.get()) {
            for (Class<? extends Module> klass : AURAS) {
                Module module = Modules.get().get(klass);

                if (wasAura.contains(klass) && !module.isActive()) {
                    module.toggle();
                }
            }
        }

        // Resume baritone
        if (pauseBaritone.get() && wasBaritone) {
            wasBaritone = false;
            PathManagers.get().resume();
        }
    }

    private void setPressed(boolean pressed) {
        mc.options.useKey.setPressed(pressed);
    }

    private void changeSlot(int slot) {
        InvUtils.swap(slot, false);
        this.slot = slot;
    }


    private int findSlot() {
        int slot = -1;

        boolean raidActive = false;
        for(BossBarExtension bossBar : bossBars) {
            if(bossBar.getName().getString().toLowerCase().contains("raid")) {
                raidActive = true;
            }
        }

        for(StatusEffectInstance effect : mc.player.getStatusEffects()) {
            if (effect.getEffectType() == StatusEffects.BAD_OMEN || effect.getEffectType() == StatusEffects.TRIAL_OMEN || effect.getEffectType() == StatusEffects.RAID_OMEN) {
                raidActive = true;
            }
        }
        if(raidActive) return -1;





        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            if (item != Items.OMINOUS_BOTTLE) continue;



            slot = i;
        }


        return slot;
    }

}

