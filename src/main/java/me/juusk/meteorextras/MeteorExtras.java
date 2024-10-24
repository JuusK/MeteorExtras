package me.juusk.meteorextras;

import me.juusk.meteorextras.modules.InfAura;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.systems.hud.Hud;
import meteordevelopment.meteorclient.systems.hud.HudGroup;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class MeteorExtras extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Extras");
    public static final HudGroup HUD_GROUP = new HudGroup("Extras");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Extras");

        // Modules
        Modules.get().add(new InfAura());

        // Commands

        // HUD
    }

    @Override
    public void onRegisterCategories() {
        Modules.registerCategory(CATEGORY);
    }

    @Override
    public String getPackage() {
        return "me.juusk.meteorextras";
    }

    //@Override
    //public GithubRepo getRepo() {
        //return new GithubRepo("MeteorDevelopment", "meteor-addon-template");
    //}
}
