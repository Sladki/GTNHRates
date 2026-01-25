package com.github.sladki.gtnhrates.mixins.late;

import static com.github.sladki.gtnhrates.Utils.applyRate;

import net.minecraftforge.common.util.ForgeDirection;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.github.sladki.gtnhrates.ModConfig;

import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.MTEBasicMachine;
import gregtech.common.tileentities.machines.basic.MTEMiner;

@Mixin(value = MTEMiner.class, remap = false)
public abstract class GTMiner {

    @Shadow(remap = false)
    static final int[] ENERGY = { 8, 8, 32, 128, 512 };

    @Shadow
    public abstract int getMachineSpeed();

    @Shadow
    @Final
    private int mSpeed;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void onMTEMinerClassInit(CallbackInfo ci) {
        for (int i = 0; i < ENERGY.length; i++) {
            ENERGY[i] = applyRate(ENERGY[i], ModConfig.Rates.gtSimpleMinersEnergyDiscount);
        }
    }

    @Inject(
        method = "onPostTick",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/common/tileentities/machines/basic/MTEMiner;hasFreeSpace()Z",
            shift = At.Shift.AFTER),
        cancellable = true)
    private void activateIfStackedAbove(IGregTechTileEntity aBaseMetaTileEntity, long aTick, CallbackInfo ci) {
        if (!ModConfig.Misc.enableMinerStacking) {
            return;
        }

        MTEBasicMachine asMTEBasicMachine = (MTEBasicMachine) (Object) this;
        int progressTime = -1;
        int maxProgressTime = 0;
        IGregTechTileEntity tTileEntity;
        // check for the first and fifth below
        for (int i = 1; i < 5; i++) {
            if (i == 1 || i >= 4) {
                tTileEntity = asMTEBasicMachine.getBaseMetaTileEntity()
                    .getIGregTechTileEntityAtSideAndDistance(ForgeDirection.DOWN, i);
                if (i == 1 && tTileEntity != null && (tTileEntity.getMetaTileEntity() instanceof MTEMiner)) {
                    progressTime = tTileEntity.isActive() ? tTileEntity.getProgress() : -1;
                    maxProgressTime = tTileEntity.getMaxProgress();
                } else if (tTileEntity != null) {
                    progressTime = -1;
                    break;
                }
            }
        }
        if (progressTime > -1) {
            asMTEBasicMachine.mProgresstime = progressTime;
            asMTEBasicMachine.mMaxProgresstime = maxProgressTime;
            asMTEBasicMachine.getBaseMetaTileEntity()
                .setActive(true);
            ci.cancel();
        }
    }

    @Redirect(
        method = "onPostTick",
        at = @At(
            value = "INVOKE",
            target = "Lgregtech/api/interfaces/tileentity/IGregTechTileEntity;decreaseStoredEnergyUnits(JZ)Z"))
    private boolean applyStackedMinersBonus(IGregTechTileEntity instance, long aEnergy, boolean aIgnoreTooLessEnergy) {
        if (!ModConfig.Misc.enableMinerStacking) {
            return instance.decreaseStoredEnergyUnits(aEnergy, aIgnoreTooLessEnergy);
        }

        MTEBasicMachine asMTEBasicMachine = (MTEBasicMachine) (Object) this;
        int stackedMinersCount = 1;
        IGregTechTileEntity tTileEntity;
        for (int i = 1; (i < 4) && ((tTileEntity = asMTEBasicMachine.getBaseMetaTileEntity()
            .getIGregTechTileEntityAtSideAndDistance(ForgeDirection.UP, i)) != null)
            && ((tTileEntity.getMetaTileEntity() instanceof MTEMiner)); i++) {
            // Apparently someone might stack 4 miners on top of each other, so let's check for that
            if (this.getMachineSpeed() == ((MTEMiner) tTileEntity.getMetaTileEntity()).getMachineSpeed()) {
                stackedMinersCount += 1;
            } else {
                break;
            }
        }
        // The more miners we have stacked, the faster this one go
        asMTEBasicMachine.mMaxProgresstime = this.mSpeed / stackedMinersCount;
        if (asMTEBasicMachine.mProgresstime >= asMTEBasicMachine.mMaxProgresstime - 1) {
            asMTEBasicMachine.mProgresstime = this.mSpeed - 1;
        }
        return instance
            .decreaseStoredEnergyUnits((long) (aEnergy * (1 + (stackedMinersCount - 1) * 0.8)), aIgnoreTooLessEnergy);
    }

}
