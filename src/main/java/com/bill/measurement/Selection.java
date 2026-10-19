package com.bill.measurement;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

/**
 * The two corners picked with the wooden shovel. Either corner may still be missing (null).
 * Rendering reads it every frame, so the whole selection is replaced instead of changed in place.
 */
public record Selection(BlockPos first, BlockPos second, ResourceKey<Level> dimension) {
    private static volatile Selection current;

    public static Selection current() {
        return current;
    }

    /** Sets corner 1 or 2; returns false if it already was at that block. */
    public static boolean setCorner(int corner, ResourceKey<Level> dimension, BlockPos pos) {
        Selection old = current;
        if (old == null || old.dimension != dimension) {
            // Corners in different dimensions make no area
            old = new Selection(null, null, dimension);
        }
        Selection updated = corner == 1
                ? new Selection(pos.immutable(), old.second, dimension)
                : new Selection(old.first, pos.immutable(), dimension);
        if (updated.equals(current)) {
            return false;
        }
        current = updated;
        return true;
    }

    public static void clear() {
        current = null;
    }

    public boolean complete() {
        return first != null && second != null;
    }

    public BlockPos min() {
        return new BlockPos(Math.min(first.getX(), second.getX()), Math.min(first.getY(), second.getY()),
                Math.min(first.getZ(), second.getZ()));
    }

    public BlockPos max() {
        return new BlockPos(Math.max(first.getX(), second.getX()), Math.max(first.getY(), second.getY()),
                Math.max(first.getZ(), second.getZ()));
    }

    /** Length along X, in blocks. */
    public int length() {
        return Math.abs(first.getX() - second.getX()) + 1;
    }

    /** Width along Z, in blocks. */
    public int width() {
        return Math.abs(first.getZ() - second.getZ()) + 1;
    }

    /** Height along Y, in blocks. */
    public int height() {
        return Math.abs(first.getY() - second.getY()) + 1;
    }

    public long volume() {
        return (long) length() * width() * height();
    }
}
