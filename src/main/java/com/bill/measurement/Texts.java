package com.bill.measurement;

import com.bill.measurement.render.SelectionShapes;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

final class Texts {
    private Texts() {
    }

    /** The prefix is a sibling, not the parent, so its color doesn't spread to what is appended later. */
    static MutableComponent prefixed(Component body) {
        return Component.empty().append(Component.translatable("measurement.prefix").withStyle(ChatFormatting.GOLD))
                .append(body);
    }

    static MutableComponent number(long value) {
        return Component.literal(String.format(Locale.ROOT, "%,d", value)).withStyle(ChatFormatting.GOLD);
    }

    static MutableComponent pos(BlockPos pos) {
        return Component.literal("(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")");
    }

    static MutableComponent corner(int corner, BlockPos pos) {
        int color = corner == 1 ? SelectionShapes.FIRST_COLOR : SelectionShapes.SECOND_COLOR;
        return pos(pos).withStyle(style -> style.withColor(color & 0xFFFFFF));
    }

    static Component cornerSet(int corner, Selection selection) {
        BlockPos pos = corner == 1 ? selection.first() : selection.second();
        MutableComponent text = Component.translatable("measurement.corner." + corner, corner(corner, pos));
        if (selection.complete()) {
            text.append("  ").append(Component.translatable("measurement.corner.size", number(selection.length()),
                    number(selection.width()), number(selection.height()), number(selection.volume())));
        }
        return text;
    }
}
