package com.bill.measurement.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Draws {@link SelectionShapes} with Minecraft 26.3 rendering. 26.3 has no plain quad render types left that shader
 * mods know, so every piece of line is drawn as a text background rectangle, the way name tags draw theirs.
 */
public final class SelectionRenderer {
    /** Submits are drawn by order, so these come after everything vanilla submits. */
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
        // The depth tested lines are drawn with the other translucent features. Everything see-through is drawn later
        // in one pass after translucent terrain, in the order submitted: faint lines, label backgrounds, label text.
        OrderedSubmitNodeCollector ordered = collector.order(ORDER);
        shapes.lines((a, b, c, d, argb) -> rectangle(ordered, poseStack, a, b, c, argb, Font.DisplayMode.NORMAL),
                1, SelectionShapes.DEPTH_PULL);
        shapes.lines((a, b, c, d, argb) -> rectangle(ordered, poseStack, a, b, c, argb, Font.DisplayMode.SEE_THROUGH),
                SelectionShapes.HIDDEN_ALPHA, 0);
        for (SelectionShapes.Label label : shapes.labels()) {
            poseStack.pushPose();
            poseStack.last().pose().mul(label.pose());
            ordered.submitTextBackground(poseStack, label.left(), label.top(), label.right(), label.bottom(),
                    SelectionShapes.BOX_COLOR, Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT);
            poseStack.popPose();
        }
        for (SelectionShapes.Label label : shapes.labels()) {
            poseStack.pushPose();
            poseStack.last().pose().mul(label.pose());
            ordered.submitText(poseStack, label.x(), label.y(), FormattedCharSequence.forward(label.text(), Style.EMPTY),
                    false, Font.DisplayMode.SEE_THROUGH, LightCoordsUtil.FULL_BRIGHT, SelectionShapes.TEXT_COLOR, 0, 0);
            poseStack.popPose();
        }
    }

    /**
     * Submits the parallelogram spanned by {@code b -> c} and {@code b -> a}. Line pieces are short, so this is
     * close enough to their quad. Text quads are only drawn from the front, so it is turned to face the camera.
     */
    private static void rectangle(OrderedSubmitNodeCollector collector, PoseStack poseStack, Vector3f a, Vector3f b,
                                  Vector3f c, int argb, Font.DisplayMode mode) {
        Vector3f origin = b;
        Vector3f along = new Vector3f(c).sub(b);
        Vector3f across = new Vector3f(a).sub(b);
        Vector3f normal = new Vector3f(along).cross(across);
        if (normal.dot(origin) < 0) {
            origin = a;
            across.negate();
            normal.negate();
        }
        normal.normalize();
        Matrix4f rectangle = new Matrix4f().set(
                along.x, along.y, along.z, 0,
                across.x, across.y, across.z, 0,
                normal.x, normal.y, normal.z, 0,
                origin.x, origin.y, origin.z, 1);
        poseStack.pushPose();
        poseStack.last().pose().mul(rectangle);
        collector.submitTextBackground(poseStack, 0, 0, 1, 1, argb, mode, LightCoordsUtil.FULL_BRIGHT);
        poseStack.popPose();
    }
}
