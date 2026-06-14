package com.azukii.diet.data;

import com.azukii.diet.DietMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public static final TagKey<Item> FOODS_CATEGORIES = register("foods_categories");
    private static final TagKey<Item> FRUITS = register("fruits");
    private static final TagKey<Item> GRAINS = register("grains");
    private static final TagKey<Item> PROTEINS = register("proteins");
    private static final TagKey<Item> VEGETABLES = register("vegetables");
    private static final TagKey<Item> SUGARS = register("sugars");

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DietMod.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(FOODS_CATEGORIES)
                .addTag(FRUITS)
                .addTag(GRAINS)
                .addTag(PROTEINS)
                .addTag(VEGETABLES)
                .addTag(SUGARS);
        this.tag(FRUITS)
                .addTag(Tags.Items.FOODS_FRUIT)
                .addTag(Tags.Items.FOODS_BERRY);
        this.tag(GRAINS)
                .addTag(Tags.Items.SEEDS)
                .addTag(Tags.Items.FOODS_BREAD)
                .add(Items.WHEAT);
        this.tag(PROTEINS)
                .addTag(ItemTags.MEAT)
                .addTag(ItemTags.FISHES);
        this.tag(VEGETABLES)
                .addTag(Tags.Items.FOODS_VEGETABLE)
                .add(Items.BAKED_POTATO)
                .add(Items.POISONOUS_POTATO)
                .add(Items.DRIED_KELP)
                .add(Items.SEA_PICKLE);
        this.tag(SUGARS)
                .addTag(Tags.Items.FOODS_COOKIE)
                .add(Items.SUGAR)
                .add(Items.HONEY_BOTTLE)
                .add(Items.CAKE)
                .add(Items.PUMPKIN_PIE);
    }

    public static TagKey<Item> register(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DietMod.MODID, name));
    }
}
