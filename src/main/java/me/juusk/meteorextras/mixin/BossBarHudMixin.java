package me.juusk.meteorextras.mixin;

import me.juusk.meteorextras.modules.AutoOminous;
import me.juusk.meteorextras.utils.BossBarExtension;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.NoRender;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BossBarS2CPacket;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(BossBarHud.class)
public abstract class BossBarHudMixin {

    @Inject(method = "handlePacket", at = @At("HEAD"))
    private void onPacket(BossBarS2CPacket packet, CallbackInfo ci) {

        packet.accept(new BossBarS2CPacket.Consumer() {
            @Override
            public void add(UUID uuid, Text name, float percent, BossBar.Color color, BossBar.Style style, boolean darkenSky, boolean dragonMusic, boolean thickenFog) {
                Modules.get().get(AutoOminous.class).bossBars.add(new BossBarExtension(uuid, name, ((AutoOminous)Modules.get().get("AutoOminous")).tick));
                BossBarS2CPacket.Consumer.super.add(uuid, name, percent, color, style, darkenSky, dragonMusic, thickenFog);
            }
            @Override
            public void remove(UUID uuid) {
                for(BossBarExtension extension : Modules.get().get(AutoOminous.class).bossBars) {
                    if(extension.uuid == uuid) {
                        Modules.get().get(AutoOminous.class).bossBars.remove(extension);
                    }
                }
                BossBarS2CPacket.Consumer.super.remove(uuid);
            }
        });
    }

}
