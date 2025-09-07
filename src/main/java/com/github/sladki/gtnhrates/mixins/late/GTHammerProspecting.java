package com.github.sladki.gtnhrates.mixins.late;

import java.util.ArrayList;
import java.util.stream.Collectors;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.github.sladki.gtnhrates.ModConfig;
import com.gtnewhorizon.gtnhlib.blockpos.BlockPos;

import gregtech.api.GregTechAPI;
import gregtech.api.enums.Materials;
import gregtech.api.items.MetaBaseItem;
import gregtech.api.objects.ItemData;
import gregtech.api.util.GTOreDictUnificator;
import gregtech.api.util.GTUtility;
import gregtech.common.blocks.BlockOresAbstract;
import gregtech.common.blocks.TileEntityOres;
import gregtech.common.items.behaviors.BehaviourProspecting;

@Mixin(value = BehaviourProspecting.class, remap = false)
public abstract class GTHammerProspecting {

    @Inject(
        method = "onItemUseFirst(Lgregtech/api/items/MetaBaseItem;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;IIILnet/minecraftforge/common/util/ForgeDirection;FFF)Z",
        at = @At(value = "INVOKE", target = "Lgregtech/api/objects/XSTR;<init>(J)V"),
        cancellable = true)
    private void replaceOreProspecting(MetaBaseItem aItem, ItemStack aStack, EntityPlayer aPlayer, World aWorld, int aX,
        int aY, int aZ, ForgeDirection side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (!ModConfig.Rates.gtHammerOreProspectingOverhaul) {
            return;
        }

        ArrayList<String> oresFound = new ArrayList<>(8);

        int radius = ModConfig.Rates.gtHammerOreProspectingRadius;
        for (BlockPos blockPos : BlockPos
            .getAllInBox(aX - radius, aY - radius, aZ - radius, aX + radius, aY + radius, aZ + radius)) {
            Block block = aWorld.getBlock(blockPos.getX(), blockPos.getY(), blockPos.getZ());

            if (block instanceof BlockOresAbstract) {
                TileEntity tileEntity = aWorld.getTileEntity(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                if (tileEntity instanceof TileEntityOres) {
                    Materials material = GregTechAPI.sGeneratedMaterials[((TileEntityOres) tileEntity).mMetaData
                        % 1000];
                    if (material != null && material != Materials._NULL) {
                        oresFound.add(material.mDefaultLocalName);
                    }
                }
            } else {
                int metadata = aWorld.getBlockMetadata(blockPos.getX(), blockPos.getY(), blockPos.getZ());
                ItemData association = GTOreDictUnificator.getAssociation(new ItemStack(block, 1, metadata));
                if (association != null && association.mPrefix != null
                    && association.mMaterial != null
                    && association.mPrefix.toString()
                        .startsWith("ore")) {
                    oresFound.add(association.mMaterial.mMaterial.mDefaultLocalName);
                }
            }
        }

        if (!oresFound.isEmpty()) {
            GTUtility.sendChatToPlayer(
                aPlayer,
                StatCollector.translateToLocal("Found traces of ") + oresFound.stream()
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining(", ")) + StatCollector.translateToLocal(" Ores."));
            cir.setReturnValue(true);
        } else {
            GTUtility.sendChatToPlayer(aPlayer, StatCollector.translateToLocal("No Ores found."));
        }

        cir.setReturnValue(true);
        cir.cancel();
    }
}
