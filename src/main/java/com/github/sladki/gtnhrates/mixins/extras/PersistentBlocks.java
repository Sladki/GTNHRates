package com.github.sladki.gtnhrates.mixins.extras;

import java.util.HashMap;
import java.util.UUID;
import java.util.WeakHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.BlockClay;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockGravel;
import net.minecraft.block.BlockHardenedClay;
import net.minecraft.block.BlockSand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.world.EnumSkyBlock;

import com.github.sladki.gtnhrates.Utils;

import betterquesting.api.api.ApiReference;
import betterquesting.api.api.QuestingAPI;
import betterquesting.api.questing.IQuest;

public class PersistentBlocks {

    private final PersistentBlock GRAVEL = new PersistentBlock(256, new UUID(0, 474)); // T0 - Upgrade 2.0
    private final PersistentBlock SAND = new PersistentBlock(256, new UUID(0, 53)); // T05 - Hammer time v2
    private final PersistentBlock CLAY = new PersistentBlock(512, new UUID(0, 77)); // Multi - EBF time
    private final PersistentBlock HARDENED_CLAY = new PersistentBlock(256, new UUID(0, 77)); // Multi - EBF time
    private final PersistentBlock WATER = new PersistentBlock(16, new UUID(0, 40)); // T0 - You shall proceed

    public static final Utils.Sided<PersistentBlocks> SIDED = new Utils.Sided<>(PersistentBlocks::new);

    public static class PersistentBlock {

        private final HashMap<UUID, Integer> playerMined = new HashMap<>(6);
        private final WeakHashMap<EntityPlayer, Boolean> playerCheckedQuest = new WeakHashMap<>(6);
        private final int blockDurabilityMax;
        private final UUID questUuid;

        public PersistentBlock(int blockDurabilityMax, UUID questUuid) {
            this.blockDurabilityMax = blockDurabilityMax;
            this.questUuid = questUuid;
        }

        public boolean tryReduceDurability(EntityPlayer player) {
            final int mined = playerMined.getOrDefault(player.getUniqueID(), 0);
            if (mined >= blockDurabilityMax) return false;

            final boolean questChecked = playerCheckedQuest.getOrDefault(player, false);
            if (!questChecked) {
                playerCheckedQuest.put(player, true);
                final IQuest quest = QuestingAPI.getAPI(ApiReference.QUEST_DB)
                    .get(questUuid);
                if (quest != null && quest.isComplete(player.getUniqueID())) {
                    playerMined.put(player.getUniqueID(), blockDurabilityMax);
                    return false;
                }
            }

            if (player.capabilities.isCreativeMode) return false;

            playerMined.put(player.getUniqueID(), mined + 1);
            return true;
        }
    }

    public static boolean tryReduceDurability(EntityPlayer player, Block block, int x, int y, int z) {
        final PersistentBlocks pblocks = SIDED.get(player.worldObj);
        PersistentBlock pblock = null;
        if (block instanceof BlockGravel && (y >= 61 && y <= 63)
            && player.worldObj.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) > 10) pblock = pblocks.GRAVEL;
        else if (block instanceof BlockSand && (y >= 61 && y <= 63)
            && player.worldObj.getSavedLightValue(EnumSkyBlock.Sky, x, y + 1, z) > 10) pblock = pblocks.SAND;
        else if (block instanceof BlockClay) pblock = pblocks.CLAY;
        else if (block instanceof BlockHardenedClay) pblock = pblocks.HARDENED_CLAY;
        else if (block instanceof BlockColored && block.getUnlocalizedName()
            .equals("tile.clayHardenedStained")) pblock = pblocks.HARDENED_CLAY;
        else if (block == Blocks.water) pblock = pblocks.WATER;
        return pblock != null && pblock.tryReduceDurability(player);
    }

}
