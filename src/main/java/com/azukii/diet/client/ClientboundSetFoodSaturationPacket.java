package com.azukii.diet.client;

import com.azukii.diet.DietMod;
import com.azukii.diet.registry.FoodDataCategories;
import com.azukii.diet.registry.FoodDataMixinAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.NonNull;

public record ClientboundSetFoodSaturationPacket(float[] categoriesSaturation,
                                                 ItemStack lastFoodEaten) implements CustomPacketPayload {
    public static final Type<ClientboundSetFoodSaturationPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DietMod.MODID, "sync_food_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundSetFoodSaturationPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        for (float value : payload.categoriesSaturation()) {
                            buf.writeFloat(value);
                        }
                        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, payload.lastFoodEaten());
                    },
                    buf -> {
                        float[] values = new float[FoodDataCategories.COUNT];
                        for (int i = 0; i < FoodDataCategories.COUNT; i++) {
                            values[i] = buf.readFloat();
                        }
                        ItemStack lastFoodEaten = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
                        return new ClientboundSetFoodSaturationPacket(values, lastFoodEaten);
                    }
            );

    public static void handle(ClientboundSetFoodSaturationPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            FoodData foodData = mc.player.getFoodData();
            if (foodData instanceof FoodDataMixinAccess access) {
                access.diet$setAllCategorySaturationLevels(payload.categoriesSaturation());
                access.diet$setLastFoodEaten(payload.lastFoodEaten());
            }
        });
    }

    @Override
    public @NonNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
