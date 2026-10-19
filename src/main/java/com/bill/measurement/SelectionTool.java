package com.bill.measurement;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Left click a block with a wooden shovel for corner 1, right click for corner 2. */
public final class SelectionTool {
    private SelectionTool() {
    }

    static void register() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> pick(player, level, pos, 1));
        UseBlockCallback.EVENT.register((player, level, hand, hit) ->
                hand == InteractionHand.MAIN_HAND ? pick(player, level, hit.getBlockPos(), 2) : InteractionResult.PASS);
    }

    private static InteractionResult pick(Player player, Level level, BlockPos pos, int corner) {
        // The integrated server fires these callbacks too, but selecting only happens on the client
        if (!level.isClientSide() || !MeasurementConfig.enabled() || player.isSpectator()
                || !player.getMainHandItem().is(Items.WOODEN_SHOVEL)) {
            return InteractionResult.PASS;
        }
        if (Selection.setCorner(corner, level.dimension(), pos)) {
            player.sendOverlayMessage(Texts.cornerSet(corner, Selection.current()));
        }
        // FAIL stops the block from being broken or used, and nothing is sent to the server
        return InteractionResult.FAIL;
    }
}
