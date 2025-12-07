package me.juusk.meteorextras;

import me.juusk.meteorextras.commands.Coords;
import me.juusk.meteorextras.modules.*;

import com.mojang.logging.LogUtils;
import meteordevelopment.meteorclient.addons.GithubRepo;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import org.slf4j.Logger;

public class MeteorExtras extends MeteorAddon {
    public static final Logger LOG = LogUtils.getLogger();
    public static final Category CATEGORY = new Category("Extras");

    @Override
    public void onInitialize() {
        LOG.info("Initializing Meteor Extras");

        // Modules
        Modules.get().add(new InfAura());
        Modules.get().add(new LoginCommand());
        Modules.get().add(new FlightPlus());
        Modules.get().add(new ReachPlus());
        Modules.get().add(new WGBypass());
        Modules.get().add(new AutoDrink());
        Modules.get().add(new AutoOminous());
        Modules.get().add(new Prefix());
        Modules.get().add(new XPAura());
        Modules.get().add(new TooManyPackets());
        Modules.get().add(new AutoFarmPlus());
        Modules.get().add(new AutoPause());
        Modules.get().add(new DeathSpots());

        // Commands
        Commands.add(new Coords());
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

    @Override
    public GithubRepo getRepo() {
        return new GithubRepo("JuusK", "MeteorExtras");
    }
}
