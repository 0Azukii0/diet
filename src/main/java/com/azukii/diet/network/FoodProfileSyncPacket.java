package com.azukii.diet.network;

import com.azukii.diet.DietClientMod;
import com.azukii.diet.DietMod;
import com.azukii.diet.data.FoodCategories;
import com.azukii.diet.profile.ClientFoodProfileCache;
import com.azukii.diet.profile.FoodProfile;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;

public record FoodProfileSyncPacket(Map<Identifier, FoodProfile> profiles) implements CustomPacketPayload {
    public static final Type<FoodProfileSyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DietMod.MODID, "food_profile_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FoodProfileSyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    FoodProfileSyncPacket::encode,
                    FoodProfileSyncPacket::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, FoodProfileSyncPacket packet) {
        buf.writeVarInt(packet.profiles.size());
        for (Map.Entry<Identifier, FoodProfile> entry : packet.profiles.entrySet()) {
            buf.writeIdentifier(entry.getKey());
            float[] values = entry.getValue().values();
            for (float value : values) {
                buf.writeFloat(value);
            }
        }
    }

    private static FoodProfileSyncPacket decode(RegistryFriendlyByteBuf buf) {
        int size = buf.readVarInt();
        Map<Identifier, FoodProfile> profiles = new HashMap<>();
        for (int i = 0; i < size; i++) {
            Identifier id = buf.readIdentifier();
            float[] values = new float[FoodCategories.COUNT];
            for (int j = 0; j < FoodCategories.COUNT; j++) {
                values[j] = buf.readFloat();
            }
            profiles.put(id, new FoodProfile(values));
        }
        return new FoodProfileSyncPacket(profiles);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(FoodProfileSyncPacket packet, Player player) {
        ClientFoodProfileCache clientCache = DietClientMod.getClientDietCache();
        if (clientCache != null) {
            clientCache.updateFromServer(packet.profiles());
        }
    }
}