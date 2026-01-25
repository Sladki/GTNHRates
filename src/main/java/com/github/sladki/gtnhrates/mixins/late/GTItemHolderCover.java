package com.github.sladki.gtnhrates.mixins.late;

import java.util.ArrayList;

import net.minecraftforge.common.util.ForgeDirection;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.sladki.gtnhrates.ModConfig;
import com.gtnewhorizons.modularui.api.forge.IItemHandlerModifiable;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.widget.Column;
import com.gtnewhorizons.modularui.common.widget.MultiChildWidget;
import com.gtnewhorizons.modularui.common.widget.SlotGroup;

import gregtech.api.metatileentity.BaseTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.common.covers.Cover;
import gregtech.common.covers.CoverChest;

@Mixin(value = CoverableTileEntity.class, remap = false)
public abstract class GTItemHolderCover extends BaseTileEntity {

    @Shadow
    public abstract @NotNull Cover getCoverAtSide(ForgeDirection var1);

    @Inject(method = "addCoverTabs", at = @At("TAIL"))
    private void onCreatingCoverButtons(ModularWindow.Builder builder, UIBuildContext buildContext, CallbackInfo ci) {
        if (!ModConfig.Misc.gtItemHolderCoverOpenAuto) {
            return;
        }

        ArrayList<IItemHandlerModifiable> itemHandlers = new ArrayList<>(9);
        int slotsCountTotal = 0;

        for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
            Cover cover = this.getCoverAtSide(direction);
            if (cover instanceof CoverChest) {
                CoverChest coverChest = (CoverChest) cover;
                itemHandlers.add(coverChest.getItems());
                slotsCountTotal += coverChest.getSlotCount();
            }
        }

        if (slotsCountTotal == 0) {
            return;
        }

        final int SLOT_SIZE = 18;
        final int SLOTS_OFFSET = 2;

        final MultiChildWidget windowWidget = new MultiChildWidget();
        builder.widget(windowWidget);
        windowWidget.setPos(-SLOT_SIZE * 4 - SLOTS_OFFSET, SLOT_SIZE * (4 - slotsCountTotal / 3 / 2) - SLOTS_OFFSET)
            .setSize(3 * SLOT_SIZE + SLOTS_OFFSET * 2, slotsCountTotal / 3 * SLOT_SIZE + SLOTS_OFFSET * 2)
            .setBackground(getGUITextureSet().getMainBackground());

        final Column columnWidget = new Column();
        columnWidget.setPos(SLOTS_OFFSET, SLOTS_OFFSET);
        windowWidget.addChild(columnWidget);

        for (IItemHandlerModifiable itemHandler : itemHandlers) {
            columnWidget.addChild(
                SlotGroup.ofItemHandler(itemHandler, 3)
                    .shiftClickPriority(Integer.MIN_VALUE) // disable shift clicking
                    .build());
        }
    }
}
