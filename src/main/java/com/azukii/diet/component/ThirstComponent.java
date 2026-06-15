package com.azukii.diet.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;

import java.util.function.Consumer;

public record ThirstComponent(int value) implements TooltipProvider {
    public static final Codec<ThirstComponent> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            Codec.INT.fieldOf("value").forGetter(ThirstComponent::value)
    ).apply(builder, ThirstComponent::new));

    public static final StreamCodec<ByteBuf, ThirstComponent> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, ThirstComponent::value,
            ThirstComponent::new
    );

    @Override
    public void addToTooltip(Item.TooltipContext tooltipContext, Consumer<Component> consumer, TooltipFlag tooltipFlag, DataComponentGetter dataComponentGetter) {
        consumer.accept(Component.translatable("tooltip.diet.thirst.level_added", this.value));
    }


}
