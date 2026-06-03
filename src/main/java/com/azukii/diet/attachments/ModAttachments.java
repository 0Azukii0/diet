package com.azukii.diet.attachments;

import com.azukii.diet.DietMod;
import com.azukii.diet.data.ModFoodData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DietMod.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ModFoodData>> DIET_DATA =
            ATTACHMENT_TYPES.register("diet_data", () -> AttachmentType.serializable(ModFoodData::new).build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
