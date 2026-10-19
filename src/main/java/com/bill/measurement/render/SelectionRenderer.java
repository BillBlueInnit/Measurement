package com.bill.measurement.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

/**
 * Draws {@link SelectionShapes} with Minecraft 26.2 rendering, where the level is extracted first and drawing only
 * happens through submits.
 */
public final class SelectionRenderer {
    /** Submits are drawn by order, so these come after everything vanilla submits, one step after another. */
    private static final int ORDER = 1000;

    /** Built while the frame is extracted, on the game thread, then submitted for drawing. */
    private static volatile SelectionShapes frame;

    private SelectionRenderer() {
    }

    public static void register() {
        LevelExtractionEvents.END_EXTRACTION.register(context -> {
            Camera camera = context.camera();
            Vec3 cameraPos = camera.position();
            frame = SelectionShapes.forFrame(context.level().dimension(), cameraPos.x, cameraPos.y, cameraPos.z,
                    camera.rotation(), Minecraft.getInstance().font::width);
        });
        LevelRenderEvents.COLLECT_SUBMITS.register(context -> submit(context.submitNodeCollector(), context.poseStack()));
    }

    private static void submit(SubmitNodeCollector collector, PoseStack poseStack) {
        SelectionShapes shapes = frame;
        if (shapes == null) {
            return;
        }
        // Each order is drawn over the previous one: the faint lines behind blocks, the lines in front of blocks,
        // the label backgrounds and the label text
        collector.order(ORDER).submitCustomGeometry(poseStack, RenderTypes.textBackgroundSeeThrough(),
                (pose, consumer) -> shapes.lines(sink(consumer, pose), SelectionShapes.HIDDEN_ALPHA, 0));
        collector.order(ORDER + 1).submitCustomGeometry(poseStack, RenderTypes.textBackground(),
                (pose, consumer) -> shapes.lines(sink(consumer, pose), 1, SelectionShapes.DEPTH_PULL));
        collector.order(ORDER + 2).submitCustomGeometry(poseStack, RenderTypes.textBackgroundSeeThrough(),
                (pose, consumer) -> shapes.labelBackgrounds(sink(consumer, pose)));
        for (SelectionShapes.Label label : shapes.labels()) {
            poseStack.pushPose();
            poseStack.last().pose().mul(label.pose());
            collector.order(ORDER + 3).submitText(poseStack, label.x(), label.y(),
                    FormattedCharSequence.forward(label.text(), Style.EMPTY), false, Font.DisplayMode.SEE_THROUGH,
                    LightCoordsUtil.FULL_BRIGHT, SelectionShapes.TEXT_COLOR, 0, 0);
            poseStack.popPose();
        }
    }

    private static SelectionShapes.VertexSink sink(VertexConsumer consumer, PoseStack.Pose pose) {
        return (x, y, z, argb) -> consumer.addVertex(pose, x, y, z).setColor(argb).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
