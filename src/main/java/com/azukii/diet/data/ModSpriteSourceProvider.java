package com.azukii.diet.data;

import com.azukii.diet.DietMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.data.SpriteSourceProvider;
import net.neoforged.neoforge.client.textures.NamespacedDirectoryLister;

import java.util.concurrent.CompletableFuture;

public class ModSpriteSourceProvider extends SpriteSourceProvider {
    public ModSpriteSourceProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, DietMod.MODID);
    }

    @Override
    protected void gather() {
        SourceList gui = atlas(Identifier.fromNamespaceAndPath(DietMod.MODID, "gui"));
        gui.addSource(new NamespacedDirectoryLister(DietMod.MODID,"gui/sprites",""));
    }
}
