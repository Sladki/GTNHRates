package com.github.sladki.gtnhrates.mixins.late;

import static gregtech.common.ores.OreManager.getOreInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;

import gregtech.api.enums.Materials;
import gregtech.api.items.GTGenericBlock;
import gregtech.common.blocks.GTBlockOre;
import gregtech.common.ores.GTOreAdapter;
import gregtech.common.ores.OreInfo;
import gregtech.common.ores.OreManager;

@Mixin(value = GTBlockOre.class, remap = false)
public abstract class GTOres extends GTGenericBlock {

    @Inject(method = "getDrops", at = @At(value = "RETURN"), cancellable = true)
    private void onGetDrops(World world, int x, int y, int z, int metadata, int fortune,
        CallbackInfoReturnable<ArrayList<ItemStack>> cir) {
        try (OreInfo<Materials> info = GTOreAdapter.INSTANCE.getOreInfo(this, metadata)) {
            if (info == null) return;

            EntityPlayer harvester = this.harvesters.get();
            boolean doSilktouch = harvester != null && EnchantmentHelper.getSilkTouchModifier(harvester);

            if (doSilktouch) return;

            float mult = ModConfig.Rates.gtOresDrops;
            if (info.material == Materials.Coal) mult = ModConfig.Rates.gtCoalOreDrops;

            cir.setReturnValue(Utils.multiplyItemStacksSize(cir.getReturnValue(), mult));
        }
    }

    protected GTOres(Class<? extends ItemBlock> aItemClass, String aName, Material aMaterial) {
        super(aItemClass, aName, aMaterial);
    }

    // Miners
    @Mixin(value = OreManager.class, remap = false)
    public abstract static class GTOreManager {

        @Inject(method = "mineBlock", at = @At(value = "RETURN"), cancellable = true)
        private static void onMineBlock(Random random, World world, int x, int y, int z, boolean silktouch, int fortune,
            boolean simulate, boolean replaceWithCobblestone, CallbackInfoReturnable<List<ItemStack>> cir) {
            Block ore = world.getBlock(x, y, z);
            int meta = world.getBlockMetadata(x, y, z);

            try (OreInfo<?> info = getOreInfo(ore, meta)) {
                if (info == null) return;
                if (silktouch && ore.canSilkHarvest(world, null, x, y, z, meta)) return;

                float mult = ModConfig.Rates.gtOresDrops;
                if (info.material == Materials.Coal) mult = ModConfig.Rates.gtCoalOreDrops;

                cir.setReturnValue(Utils.multiplyItemStacksSize(cir.getReturnValue(), mult));
            }
        }
    }
}
