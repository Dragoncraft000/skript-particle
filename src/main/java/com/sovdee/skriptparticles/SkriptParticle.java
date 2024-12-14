package com.sovdee.skriptparticles;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

@SuppressWarnings("unused")
public class SkriptParticle extends JavaPlugin {

    private static SkriptParticle instance;
    private static Logger logger;


    // todo, next release
    // custom shapes
    // icosphere
    // expressions for particles
    // todo, later versions
    // beziers
    // better triangle filling (basically allow any 3d model)
    // gradients
    // text rendering

    @Nullable
    public static SkriptParticle getInstance() {
        return instance;
    }

    public static void info(String message) {
        if (logger == null)
            return;
        logger.info(message);
    }

    public static void warning(String message) {
        if (logger == null)
            return;
        logger.warning(message);
    }

    public static void severe(String message) {
        if (logger == null)
            return;
        logger.severe(message);
    }

    @Override
    public void onEnable() {
        instance = this;
        logger = this.getLogger();
        instance = this;
        SkriptParticle.info("Successfully enabled skript-particle.");
    }

    @Override
    public void onDisable() {
        instance = null;
    }
}
