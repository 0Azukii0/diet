package com.azukii.diet.data;

import com.azukii.diet.DietMod;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = DietMod.MODID, value = Dist.CLIENT)
public class DataGenerators {

    @SubscribeEvent
    static void gatherData(GatherDataEvent.Client event) {
        event.createProvider(ModItemTagsProvider::new);
        event.createProvider(ModSpriteSourceProvider::new);
    }
}
