package me.juusk.meteorextras.modules;

import com.jcraft.jorbis.Block;
import me.juusk.meteorextras.MeteorExtras;
import me.juusk.meteorextras.utils.ModuleUtils;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.render.debug.DebugRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;

public class ReachPlus extends Module {

    private final SettingGroup sgGeneral = settings.getDefaultGroup();


    private final Setting<Double> perBlink = sgGeneral.add(new DoubleSetting.Builder()
        .name("per-blink")
        .description("After how many blocks it teleports")
        .defaultValue(8.5)
        .min(2)
        .sliderMax(20)
        .build()
    );
    private final Setting<Double> distance = sgGeneral.add(new DoubleSetting.Builder()
        .name("distance")
        .description("The distance you tp to from the target.")
        .defaultValue(2.5)
        .min(0)
        .sliderMax(6)
        .build()
    );

    private final Setting<Double> entityReach = sgGeneral.add(new DoubleSetting.Builder()
        .name("entity-reach")
        .description("The maximum reach the entity can be.")
        .defaultValue(20)
        .min(0)
        .sliderMax(100)
        .build()
    );
    private final Setting<Double> blockReach = sgGeneral.add(new DoubleSetting.Builder()
        .name("block-reach")
        .description("The maximum range the block can be.")
        .defaultValue(20)
        .min(0)
        .sliderMax(100)
        .build()
    );







    public ReachPlus() {
        super(MeteorExtras.CATEGORY, "Reach+", "Teleports to extend reach");
    }

    @EventHandler
    public void onTick(TickEvent.Pre event) {
        if (mc.player == null) return;
        Entity targetedEntity = DebugRenderer.getTargetedEntity(mc.player, entityReach.get().intValue()).isPresent() ? DebugRenderer.getTargetedEntity(mc.player, entityReach.get().intValue()).get() : null;
        if (targetedEntity != null) {
            if (mc.options.attackKey.isPressed()) {
                ModuleUtils.splitTeleport(mc.player.getPos(), targetedEntity.getPos(), perBlink.get(), distance.get());
                mc.interactionManager.attackEntity(mc.player, targetedEntity);
                mc.player.swingHand(Hand.MAIN_HAND);
                ModuleUtils.splitTeleport(targetedEntity.getPos(), mc.player.getPos(), perBlink.get(), distance.get());
            }
        } else if(mc.player.getMainHandStack().getItem() instanceof BlockItem) {
            HitResult result = mc.player.raycast(blockReach.get(), 1f / 20f, false);
            if (result != null && result.getType() == HitResult.Type.BLOCK) {
                if(mc.options.useKey.isPressed()) {
                    BlockPos resultPos = ((BlockHitResult) result).getBlockPos().add(0, 1, 0);
                    ModuleUtils.splitTeleport(mc.player.getPos(), resultPos.toCenterPos(), perBlink.get(), distance.get());
                    //BlockUtils.place(resultPos, Hand.MAIN_HAND, mc.player.getInventory().getSlotWithStack(mc.player.getMainHandStack()), true, 1, true, true, true);
                    BlockUtils.interact((BlockHitResult)result, Hand.MAIN_HAND, true);
                    ModuleUtils.splitTeleport(resultPos.toCenterPos(), mc.player.getPos(), perBlink.get(), distance.get());
                } else if(mc.options.attackKey.isPressed()) {
                    BlockPos resultPos = ((BlockHitResult) result).getBlockPos().add(0, 1, 0);
                    ModuleUtils.splitTeleport(mc.player.getPos(), resultPos.toCenterPos(), perBlink.get(), distance.get());
                    BlockUtils.breakBlock(((BlockHitResult)result).getBlockPos(), true);
                    ModuleUtils.splitTeleport(resultPos.toCenterPos(), mc.player.getPos(), perBlink.get(), distance.get());
                }
            }
        }
    }


}
