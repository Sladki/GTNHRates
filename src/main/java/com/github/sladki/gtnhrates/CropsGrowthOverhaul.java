package com.github.sladki.gtnhrates;

import java.util.Arrays;
import java.util.HashMap;

import net.minecraft.world.World;

public class CropsGrowthOverhaul {

    // Store crops estimated growth time as: world hash, y, (x << 32) + z, {ticks}
    private static final HashMap<World, HashMap<Integer, HashMap<Long, long[]>>> cropsMatureETA = new HashMap<>();

    public static int newMetadata(World world, int x, int y, int z, int ticksToMature, int[] stagesMetadata) {
        int meta = world.getBlockMetadata(x, y, z);
        int stage = Arrays.binarySearch(stagesMetadata, meta);
        long[] etaContainer = cropETAContainer(world, x, y, z);
        // Linear growth, compute eta to mature from current stage (metadata)
        if (etaContainer[0] == 0) {
            etaContainer[0] = world.getTotalWorldTime()
                + (long) (ticksToMature * (1 - (float) stage / stagesMetadata.length));
        }
        int result = stagesMetadata[(int) ((stagesMetadata.length - 1)
            * (1 - (Math.max(0, (etaContainer[0] - world.getTotalWorldTime()) / (float) ticksToMature))))];
        // The crop is mature, reset eta for future growth
        if (stagesMetadata[stagesMetadata.length - 1] == result) {
            resetCropGrowth(world, x, y, z);
        }
        return result;
    }

    public static void resetCropGrowth(World world, int x, int y, int z) {
        cropETAContainer(world, x, y, z)[0] = 0;
    }

    private static long[] cropETAContainer(World world, int x, int y, int z) {
        long posHash = ((long) x << 32) + z;
        if (!cropsMatureETA.containsKey(world)) {
            cropsMatureETA.put(world, new HashMap<>(128));
        }
        HashMap<Integer, HashMap<Long, long[]>> yCrops = cropsMatureETA.get(world);
        if (!yCrops.containsKey(y)) {
            yCrops.put(y, new HashMap<>());
        }
        HashMap<Long, long[]> xzCrops = yCrops.get(y);
        if (!xzCrops.containsKey(posHash)) {
            xzCrops.put(posHash, new long[1]);
        }
        return xzCrops.get(posHash);
    }
}
