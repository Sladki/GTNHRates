package com.github.sladki.gtnhrates.mixins.early;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.ItemInWorldManager;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.github.sladki.gtnhrates.ModConfig;

@Mixin(value = ItemInWorldManager.class)
public class PersistentBlocks {

    @Redirect(
        method = "tryHarvestBlock",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/block/Block;harvestBlock(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/EntityPlayer;IIII)V"))
    private static void restoreHarvestedBlock(Block instance, World worldIn, EntityPlayer player, int x, int y, int z,
        int meta) {
        if (ModConfig.Misc.persistentBlocks) {
            if (com.github.sladki.gtnhrates.mixins.extras.PersistentBlocks
                .tryReduceDurability(player, instance, x, y, z)) {
                worldIn.setBlock(x, y, z, instance, meta, 3);
            }
        }
        instance.harvestBlock(worldIn, player, x, y, z, meta);
    }

}
