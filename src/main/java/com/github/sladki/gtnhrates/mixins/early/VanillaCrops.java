package com.github.sladki.gtnhrates.mixins.early;

import java.util.ArrayList;
import java.util.Random;

import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockCrops;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.CropsGrowthOverhaul;
import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;

@Mixin(value = BlockCrops.class, remap = false)
public abstract class VanillaCrops extends BlockBush {

    @Unique
    private final int[] METADATA_GROWTH_STAGES = new int[] { 0, 1, 2, 3, 4, 5, 6, 7 };

    @Inject(method = "getDrops", at = @At(value = "TAIL"), cancellable = true)
    private void onGetDrops(World world, int x, int y, int z, int metadata, int fortune,
        CallbackInfoReturnable<ArrayList<ItemStack>> cir) {
        if (metadata >= 7) {
            cir.setReturnValue(Utils.multiplyItemStacksSize(cir.getReturnValue(), ModConfig.Rates.cropsYield));
        }
    }

    @Inject(method = "updateTick", at = @At(value = "HEAD"), cancellable = true)
    private void onUpdateTick(World worldIn, int x, int y, int z, Random random, CallbackInfo ci) {
        super.updateTick(worldIn, x, y, z, random);
        if (ModConfig.Misc.cropsGrowthOverhaul) {
            if (worldIn.getBlockLightValue(x, y + 1, z) >= 9) {
                int metadata = worldIn.getBlockMetadata(x, y, z);
                if (metadata < 7) {
                    int newMetadata = CropsGrowthOverhaul
                        .newMetadata(worldIn, x, y, z, 20 * ModConfig.Misc.cropsTimeToMature, METADATA_GROWTH_STAGES);
                    if (newMetadata > metadata) {
                        worldIn.setBlockMetadataWithNotify(x, y, z, newMetadata, 2);
                        ci.cancel();
                    }
                }
            }
        }
    }
}
