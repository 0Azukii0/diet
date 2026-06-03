package com.azukii.diet.network;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import com.azukii.diet.data.ModFoodData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Syncs diet values from server to client.
 */
public record FoodSyncPacket(float[] values) implements CustomPacketPayload {
    public static final Type<FoodSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DietMod.MODID, "food_sync"));

    private static final StreamCodec<RegistryFriendlyByteBuf, float[]> FLOAT_ARRAY_CODEC =
            new StreamCodec<>() {
                @Override
                public float[] decode(RegistryFriendlyByteBuf buf) {
                    int length = buf.readVarInt();
                    float[] values = new float[length];
                    for (int i = 0; i < length; i++) {
                        values[i] = buf.readFloat();
                    }
                    return values;
                }

                @Override
                public void encode(RegistryFriendlyByteBuf buf, float[] values) {
                    buf.writeVarInt(values.length);
                    for (float value : values) {
                        buf.writeFloat(value);
                    }
                }
            };

    public static final StreamCodec<RegistryFriendlyByteBuf, FoodSyncPacket> STREAM_CODEC =
            StreamCodec.composite(
                    FLOAT_ARRAY_CODEC,
                    FoodSyncPacket::values,
                    FoodSyncPacket::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FoodSyncPacket packet, Player player) {
        ModFoodData data = player.getData(ModAttachments.DIET_DATA);
        data.setAll(packet.values());
    }
}