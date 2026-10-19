package com.bill.measurement.render;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

/** Draws {@link SelectionShapes} with Minecraft 26.1 rendering. */
public final class SelectionRenderer {
    private static final MultiBufferSource.BufferSource BUFFERS = MultiBufferSource.immediate(new ByteBufferBuilder(4096));

    private SelectionRenderer() {
    }

    public static void register() {
        // END_MAIN comes after terrain, translucent terrain, entities and particles
        LevelRenderEvents.END_MAIN.register(context ->
                render(context.poseStack(), Minecraft.getInstance().gameRenderer.getMainCamera()));
    }

    private static void render(PoseStack poseStack, Camera camera) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }
        Vec3 cameraPos = camera.position();
        SelectionShapes shapes = SelectionShapes.forFrame(minecraft.level.dimension(), cameraPos.x, cameraPos.y, cameraPos.z,
                camera.rotation(), minecraft.font::width);
        if (shapes == null) {
            return;
        }

        Matrix4f base = poseStack.last().pose();
        RenderType seeThrough = RenderTypes.textBackgroundSeeThrough();
        RenderType depthTested = RenderTypes.textBackground();
        // Separate draws, each over the previous one: the faint lines behind blocks, the lines in front of blocks,
        // the label backgrounds and the label text
        shapes.lines(sink(BUFFERS.getBuffer(seeThrough), base), SelectionShapes.HIDDEN_ALPHA, 0);
        BUFFERS.endBatch(seeThrough);
        shapes.lines(sink(BUFFERS.getBuffer(depthTested), base), 1, SelectionShapes.DEPTH_PULL);
        BUFFERS.endBatch(depthTested);
        shapes.labelBackgrounds(sink(BUFFERS.getBuffer(seeThrough), base));
        BUFFERS.endBatch(seeThrough);
        for (SelectionShapes.Label label : shapes.labels()) {
            minecraft.font.drawInBatch(label.text(), label.x(), label.y(), SelectionShapes.TEXT_COLOR, false,
                    new Matrix4f(base).mul(label.pose()), BUFFERS, Font.DisplayMode.SEE_THROUGH, 0, LightCoordsUtil.FULL_BRIGHT);
        }
        BUFFERS.endBatch();
    }

    private static SelectionShapes.VertexSink sink(VertexConsumer consumer, Matrix4f pose) {
        return (x, y, z, argb) -> consumer.addVertex(pose, x, y, z).setColor(argb).setLight(LightCoordsUtil.FULL_BRIGHT);
    }
}
