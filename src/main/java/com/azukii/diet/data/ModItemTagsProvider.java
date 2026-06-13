package com.azukii.diet.data;

import com.azukii.diet.DietMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagsProvider extends ItemTagsProvider {
    public static final TagKey<Item> FRUITS = register("fruits");
    public static final TagKey<Item> GRAINS = register("grains");
    public static final TagKey<Item> PROTEIN = register("proteins");
    public static final TagKey<Item> VEGETABLE = register("vegetables");
    public static final TagKey<Item> SUGAR = register("sugars");

    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DietMod.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(FRUITS);
        this.tag(GRAINS);
        this.tag(PROTEIN);
        this.tag(VEGETABLE);
        this.tag(SUGAR);
    }

    public static TagKey<Item> register(String name) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(DietMod.MODID, name));
    }
}
