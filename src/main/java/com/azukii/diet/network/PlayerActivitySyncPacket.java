package com.azukii.diet.network;

import com.azukii.diet.DietMod;
import com.azukii.diet.attachments.ModAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

/**
 * Syncs diet values from server to client.
 */
public record PlayerActivitySyncPacket(Identifier lastFood) implements CustomPacketPayload {
    public static final Type<PlayerActivitySyncPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(DietMod.MODID, "player_activity_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerActivitySyncPacket> STREAM_CODEC =
            StreamCodec.of(
                    PlayerActivitySyncPacket::encode,
                    PlayerActivitySyncPacket::decode
            );

    private static void encode(RegistryFriendlyByteBuf buf, PlayerActivitySyncPacket packet) {
        buf.writeIdentifier(packet.lastFood());
    }

    private static PlayerActivitySyncPacket decode(RegistryFriendlyByteBuf buf) {
        return new PlayerActivitySyncPacket(buf.readIdentifier());
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(PlayerActivitySyncPacket packet, Player player) {
        player.getData(ModAttachments.PLAYER_ACTIVITY).setLastFood(packet.lastFood());
    }
}