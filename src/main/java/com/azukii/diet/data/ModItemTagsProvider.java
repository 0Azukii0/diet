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
                .add(Items.APPLE)
                .add(Items.GOLDEN_APPLE)
                .add(Items.ENCHANTED_GOLDEN_APPLE)
                .add(Items.MELON_SLICE)
                .add(Items.CHORUS_FRUIT)
                .add(Items.SWEET_BERRIES)
                .add(Items.GLOW_BERRIES);
        this.tag(GRAINS)
                .add(Items.WHEAT)
                .add(Items.WHEAT_SEEDS);
        this.tag(PROTEINS)
                .addTag(ItemTags.MEAT)
                .add(Items.COD)
                .add(Items.SALMON)
                .add(Items.TROPICAL_FISH)
                .add(Items.PUFFERFISH)
                .add(Items.COOKED_COD)
                .add(Items.COOKED_SALMON);
        this.tag(VEGETABLES)
                .add(Items.CARROT)
                .add(Items.GOLDEN_CARROT)
                .add(Items.POTATO)
                .add(Items.BAKED_POTATO)
                .add(Items.POISONOUS_POTATO)
                .add(Items.BEETROOT)
                .add(Items.DRIED_KELP)
                .add(Items.SEA_PICKLE);
        this.tag(SUGARS)
                .add(Items.SUGAR)
                .add(Items.HONEY_BOTTLE)
                .add(Items.COOKIE)
                .add(Items.CAKE)
                .add(Items.PUMPKIN_PIE);
    }

    public static TagKey<Item> register(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DietMod.MODID, name));
    }
}
