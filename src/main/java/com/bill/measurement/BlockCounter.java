package com.bill.measurement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;

/** Counts the blocks in an area that are not air, using the chunks the client has loaded. */
final class BlockCounter {
    record Result(long solid, long unloaded) {
    }

    private BlockCounter() {
    }

    static Result count(Level level, BlockPos min, BlockPos max) {
        long solid = 0;
        long unloaded = 0;
        int height = max.getY() - min.getY() + 1;
        for (int chunkX = min.getX() >> 4; chunkX <= max.getX() >> 4; chunkX++) {
            int x0 = Math.max(min.getX(), chunkX << 4);
            int x1 = Math.min(max.getX(), (chunkX << 4) + 15);
            for (int chunkZ = min.getZ() >> 4; chunkZ <= max.getZ() >> 4; chunkZ++) {
                int z0 = Math.max(min.getZ(), chunkZ << 4);
                int z1 = Math.min(max.getZ(), (chunkZ << 4) + 15);
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    unloaded += (long) (x1 - x0 + 1) * (z1 - z0 + 1) * height;
                    continue;
                }
                // Blocks above or below the world are air, so only the chunk's own sections matter
                LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    int sectionY = chunk.getSectionYFromSectionIndex(i) << 4;
                    int y0 = Math.max(min.getY(), sectionY);
                    int y1 = Math.min(max.getY(), sectionY + 15);
                    if (y0 > y1 || section.hasOnlyAir()) {
                        continue;
                    }
                    for (int y = y0; y <= y1; y++) {
                        for (int z = z0; z <= z1; z++) {
                            for (int x = x0; x <= x1; x++) {
                                if (!section.getBlockState(x & 15, y & 15, z & 15).isAir()) {
                                    solid++;
                                }
                            }
                        }
                    }
                }
            }
        }
        return new Result(solid, unloaded);
    }
}
