package com.azukii.diet;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DietMod.MODID)
public class DietMod {
    public static final String MODID = "diet";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DietMod(IEventBus modEventBus) {
        LOGGER.info("Diet System Loaded{}", modEventBus);
    }
}
