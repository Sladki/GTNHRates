package com.github.sladki.gtnhrates.mixins.late;

import java.util.ArrayList;

import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;
import com.github.sladki.gtnhrates.Utils;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.items.GTGenericBlock;
import gregtech.common.blocks.BlockOresAbstract;
import gregtech.common.blocks.TileEntityOres;

@Mixin(value = BlockOresAbstract.class, remap = false)
public abstract class GTOres extends GTGenericBlock {

    @Unique
    public short SMALL_ORES_META_START = 16000;

    @Inject(method = "getDrops", at = @At(value = "RETURN"), cancellable = true)
    private void onGetDrops(World aWorld, int aX, int aY, int aZ, int aMeta, int aFortune,
        CallbackInfoReturnable<ArrayList<ItemStack>> cir) {
        short oreMeta = 0;
        TileEntity tTileEntity = aWorld.getTileEntity(aX, aY, aZ);
        if ((tTileEntity instanceof TileEntityOres)) {
            oreMeta = ((TileEntityOres) tTileEntity).mMetaData;
        } else if (BlockOresAbstract.mTemporaryTileEntity.get() != null) {
            oreMeta = BlockOresAbstract.mTemporaryTileEntity.get().mMetaData;
        }
        if (TileEntityOresAccessor.getShouldSilkTouch()) {
            if (oreMeta < SMALL_ORES_META_START) {
                return;
            }
        }
        float mult = ModConfig.Rates.gtOresDrops;
        // COAL
        if (GregTechAPI.sGeneratedMaterials[(oreMeta % 1000)] == Materials.Coal) {
            mult = ModConfig.Rates.gtCoalOreDrops;
        }
        cir.setReturnValue(Utils.multiplyItemStacksSize(cir.getReturnValue(), mult));
    }

    protected GTOres(Class<? extends ItemBlock> aItemClass, String aName, Material aMaterial) {
        super(aItemClass, aName, aMaterial);
    }
}
