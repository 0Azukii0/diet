package com.azukii.diet.attachments;

import com.azukii.diet.DietMod;
import com.azukii.diet.data.ModFoodData;
import com.azukii.diet.data.PlayerActivityData;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, DietMod.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<ModFoodData>> FOOD_DATA =
            ATTACHMENT_TYPES.register("food_data", () -> AttachmentType.serializable(ModFoodData::new).build());
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<PlayerActivityData>> PLAYER_ACTIVITY =
            ATTACHMENT_TYPES.register("player_activity_data", () -> AttachmentType.serializable(PlayerActivityData::new).build());

    public static void register(IEventBus bus) {
        ATTACHMENT_TYPES.register(bus);
    }
}
