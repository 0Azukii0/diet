package com.azukii.diet;

import com.azukii.diet.attachments.ModAttachments;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(DietMod.MODID)
public class DietMod {
    public static final String MODID = "diet";
    public static final Logger LOGGER = LogUtils.getLogger();

    public DietMod(IEventBus modEventBus) {
        ModAttachments.register(modEventBus);

        LOGGER.info("Diet System Loaded");
    }
}
