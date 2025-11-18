package me.juusk.meteorextras.utils;

import meteordevelopment.meteorclient.mixininterface.IPlayerMoveC2SPacket;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class ModuleUtils {

    public static void maceExploit(Vec3d previousPos, double height, boolean preventDeath, boolean packetDisable) {
        PlayerMoveC2SPacket movepacket = new PlayerMoveC2SPacket.PositionAndOnGround(
            MinecraftClient.getInstance().player.getX(), height, MinecraftClient.getInstance().player.getZ(), false, MinecraftClient.getInstance().player.horizontalCollision);
        PlayerMoveC2SPacket homepacket = new PlayerMoveC2SPacket.PositionAndOnGround(
            previousPos.getX(), previousPos.getY(), previousPos.getZ(),
            false, MinecraftClient.getInstance().player.horizontalCollision);
        if (preventDeath) {
            homepacket = new PlayerMoveC2SPacket.PositionAndOnGround(
                previousPos.getX(), previousPos.getY() + 0.25, previousPos.getZ(),
                false, MinecraftClient.getInstance().player.horizontalCollision);
        }
        ((IPlayerMoveC2SPacket) homepacket).meteor$setTag(1337);
        ((IPlayerMoveC2SPacket) movepacket).meteor$setTag(1337);
        MinecraftClient.getInstance().player.networkHandler.sendPacket(movepacket);
        MinecraftClient.getInstance().player.networkHandler.sendPacket(homepacket);
        if (preventDeath) {
            MinecraftClient.getInstance().player.setVelocity(MinecraftClient.getInstance().player.getVelocity().x, 0.1, MinecraftClient.getInstance().player.getVelocity().z);
            MinecraftClient.getInstance().player.fallDistance = 0;
        }
    }


    public static void splitTeleport(Vec3d from, Vec3d to, double perBlink, double extraDistance) {
        Vec3d playerPos = from;
        Vec3d targetPos = to;
        Vec3d toTarget = targetPos.subtract(from);

        double distance = toTarget.length() - extraDistance;

        toTarget = toTarget.normalize();


        toTarget = toTarget.multiply(distance);
        targetPos = playerPos.add(toTarget);

        double ceiledDistance = Math.ceil(distance / perBlink);
        for(int i = 1; i <= ceiledDistance; i++) {
            Vec3d tempPos = playerPos.lerp(targetPos, i / ceiledDistance);
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(tempPos.x, tempPos.y, tempPos.z, true, MinecraftClient.getInstance().player.horizontalCollision));

        }
    }


    public static void placeWithOffhand(BlockPos blockPos, FindItemResult findItemResult, boolean rotate, int rotationPriority, boolean checkEntities) {

    }

}
