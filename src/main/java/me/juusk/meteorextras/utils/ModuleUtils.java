package me.juusk.meteorextras.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.math.Vec3d;

public class ModuleUtils {



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

}
