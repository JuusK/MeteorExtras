package me.juusk.meteorextras.modules;

import me.juusk.meteorextras.MeteorExtras;
import meteordevelopment.meteorclient.events.entity.player.ItemUseCrosshairTargetEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.pathing.PathManagers;
import meteordevelopment.meteorclient.settings.*;
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
import meteordevelopment.meteorclient.utils.player.SlotUtils;
import meteordevelopment.orbit.EventHandler;
import meteordevelopment.orbit.EventPriority;
import net.minecraft.component.Component;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.item.PotionItem;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;

import java.util.ArrayList;
import java.util.List;

public class AutoDrink extends Module {
    private static final Class<? extends Module>[] AURAS = new Class[]{KillAura.class, CrystalAura.class, AnchorAura.class, BedAura.class};

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    // General

    private final Setting<List<StatusEffect>> whitelist = sgGeneral.add(new StatusEffectListSetting.Builder()
        .name("whitelist")
        .description("Which items to drink.")
        .defaultValue(
            StatusEffects.BAD_OMEN.value()
        )
        .build()
    );

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

    private final List<Class<? extends Module>> wasAura = new ArrayList<>();
    private boolean wasBaritone = false;

    public AutoDrink() {
        super(MeteorExtras.CATEGORY, "AutoDrink", "Automatically drinks potions.");
    }

    @Override
    public void onDeactivate() {
        if (drinking) stopDrinking();
    }

    @EventHandler(priority = EventPriority.LOW)
    private void onTick(TickEvent.Pre event) {
        if (Modules.get().get(AutoGap.class).isEating()) return;
        if(Modules.get().get(AutoEat.class).eating) return;
        int slot = findSlot();
        if (drinking) {
            if(slot < 0) {
                stopDrinking();
                return;
            }

            if (!(slot < 0) && mc.player.getInventory().getStack(slot).get(DataComponentTypes.POTION_CONTENTS) != null) {

                for(StatusEffectInstance s : (mc.player.getInventory().getStack(slot).get(DataComponentTypes.POTION_CONTENTS)).getEffects()) {
                    if(!whitelist.get().contains(s.getEffectType().value())) return;


                    if (slot == -1) {
                        stopDrinking();
                        return;
                    }
                    // Otherwise change to the new slot
                    else {
                        changeSlot(slot);
                    }
                }

            }
            drink();
        } else{
            // Try to find a valid slot
            slot = findSlot();

            // If slot was found then start eating
            if (slot != -1) startDrinking();
        }
    }

    @EventHandler
    private void onItemUseCrosshairTarget(ItemUseCrosshairTargetEvent event) {
        if (drinking) event.target = null;
    }

    private void startDrinking() {
        prevSlot = mc.player.getInventory().getSelectedSlot();
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

        // Pause baritone
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
        for (int i = 0; i < 9; i++) {
            Item item = mc.player.getInventory().getStack(i).getItem();
            PotionContentsComponent potionComponent = item.getComponents().get(DataComponentTypes.POTION_CONTENTS);
            if (potionComponent == null) continue;

            slot = i;
        }

        Item offHandItem = mc.player.getOffHandStack().getItem();
        if (offHandItem.getComponents().get(DataComponentTypes.POTION_CONTENTS) != null && whitelist.get().contains(offHandItem))
            slot = SlotUtils.OFFHAND;

        return slot;
    }

}
