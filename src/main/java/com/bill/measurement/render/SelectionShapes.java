package com.bill.measurement.render;

import com.bill.measurement.MeasurementConfig;
import com.bill.measurement.Selection;
import java.util.ArrayList;
import java.util.List;
import java.util.function.ToIntFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * What is drawn for the selection in one frame, in coordinates relative to the camera.
 *
 * <p>Lines are built as thin quads facing the camera instead of GL lines, so the whole selection only needs the
 * vanilla quad render types that name tag backgrounds use, which shader mods such as Iris already support. The lines
 * are drawn twice: faintly without depth test, so the box shows through blocks, then at full strength with depth
 * test, so the parts in front of blocks stand out and the box keeps its depth. Labels always go on top.
 * This class only does the math; each Minecraft version's {@link SelectionRenderer} feeds it into the game.
 */
public final class SelectionShapes {
    public static final int BOX_COLOR = 0xFFFF9A1F;
    public static final int FIRST_COLOR = 0xFF33CCFF;
    public static final int SECOND_COLOR = 0xFFFF55CC;
    public static final int TEXT_COLOR = 0xFF000000;

    /** How strongly the parts of the box that are behind blocks are drawn. */
    public static final float HIDDEN_ALPHA = 0.45F;
    /**
     * The visible parts are depth tested. The box edges lie on block faces, so they are pulled slightly towards the
     * camera, or blocks would cut half of every line away.
     */
    public static final float DEPTH_PULL = 0.01F;

    /** Half the line width as a fraction of the distance to the camera, about 2.5 pixels on a 1080p screen. */
    private static final float HALF_WIDTH = 0.0012F;
    /** Corner markers are a bit bigger than a block so they don't hide the edges of the box. */
    private static final float CORNER_INFLATE = 0.03F;
    private static final float LABEL_PADDING = 2.0F;

    public interface VertexSink {
        void vertex(float x, float y, float z, int argb);
    }

    /** A size label. {@code pose} maps label space, where one unit is one font pixel, to camera space. */
    public record Label(Matrix4f pose, String text, float x, float y, int textWidth) {
    }

    private final Selection selection;
    private final double cameraX;
    private final double cameraY;
    private final double cameraZ;
    private final List<Label> labels = new ArrayList<>();

    private SelectionShapes(Selection selection, double cameraX, double cameraY, double cameraZ,
                            Quaternionf cameraRotation, ToIntFunction<String> textWidth) {
        this.selection = selection;
        this.cameraX = cameraX;
        this.cameraY = cameraY;
        this.cameraZ = cameraZ;
        if (selection.complete()) {
            addLabels(cameraRotation, textWidth);
        }
    }

    /** Returns null when there is nothing to draw in this dimension. */
    public static SelectionShapes forFrame(ResourceKey<Level> dimension, double cameraX, double cameraY, double cameraZ,
                                           Quaternionf cameraRotation, ToIntFunction<String> textWidth) {
        Selection selection = Selection.current();
        if (!MeasurementConfig.enabled() || selection == null || selection.dimension() != dimension) {
            return null;
        }
        return new SelectionShapes(selection, cameraX, cameraY, cameraZ, cameraRotation, textWidth);
    }

    /**
     * The corner markers and the box, as quads, with each color's alpha multiplied by {@code alpha} and every vertex
     * moved the fraction {@code towardCamera} of the way to the camera.
     */
    public void lines(VertexSink sink, float alpha, float towardCamera) {
        float scale = 1 - towardCamera;
        lines((x, y, z, argb) -> sink.vertex(x * scale, y * scale, z * scale,
                ((int) ((argb >>> 24) * alpha) << 24) | (argb & 0xFFFFFF)));
    }

    private void lines(VertexSink sink) {
        if (selection.first() != null) {
            corner(sink, selection.first(), FIRST_COLOR);
        }
        if (selection.second() != null) {
            corner(sink, selection.second(), SECOND_COLOR);
        }
        if (selection.complete()) {
            BlockPos min = selection.min();
            BlockPos max = selection.max();
            box(sink, min.getX(), min.getY(), min.getZ(), max.getX() + 1, max.getY() + 1, max.getZ() + 1, 0, BOX_COLOR);
        }
    }

    /** The colored squares behind the label text, as quads. Draw them after {@link #lines} and before the text. */
    public void labelBackgrounds(VertexSink sink) {
        for (Label label : labels) {
            float x0 = label.x() - LABEL_PADDING;
            float x1 = label.x() + label.textWidth() - 1 + LABEL_PADDING;
            float y0 = label.y() - LABEL_PADDING;
            float y1 = label.y() + 7 + LABEL_PADDING;
            Vector3f a = label.pose().transformPosition(new Vector3f(x0, y0, 0));
            Vector3f b = label.pose().transformPosition(new Vector3f(x0, y1, 0));
            Vector3f c = label.pose().transformPosition(new Vector3f(x1, y1, 0));
            Vector3f d = label.pose().transformPosition(new Vector3f(x1, y0, 0));
            quad(sink, a, b, c, d, BOX_COLOR);
        }
    }

    /** The text of each label goes on top of {@link #labelBackgrounds}, drawn in {@link #TEXT_COLOR}. */
    public List<Label> labels() {
        return labels;
    }

    private void corner(VertexSink sink, BlockPos pos, int color) {
        box(sink, pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1, CORNER_INFLATE, color);
    }

    private void box(VertexSink sink, double x0, double y0, double z0, double x1, double y1, double z1, float inflate, int color) {
        float ax = (float) (x0 - cameraX) - inflate;
        float ay = (float) (y0 - cameraY) - inflate;
        float az = (float) (z0 - cameraZ) - inflate;
        float bx = (float) (x1 - cameraX) + inflate;
        float by = (float) (y1 - cameraY) + inflate;
        float bz = (float) (z1 - cameraZ) + inflate;
        for (int i = 0; i < 4; i++) {
            float u = (i & 1) == 0 ? ay : by;
            float v = (i & 2) == 0 ? az : bz;
            line(sink, ax, u, v, bx, u, v, color);
        }
        for (int i = 0; i < 4; i++) {
            float u = (i & 1) == 0 ? ax : bx;
            float v = (i & 2) == 0 ? az : bz;
            line(sink, u, ay, v, u, by, v, color);
        }
        for (int i = 0; i < 4; i++) {
            float u = (i & 1) == 0 ? ax : bx;
            float v = (i & 2) == 0 ? ay : by;
            line(sink, u, v, az, u, v, bz, color);
        }
    }

    private static void line(VertexSink sink, float ax, float ay, float az, float bx, float by, float bz, int color) {
        float dx = bx - ax;
        float dy = by - ay;
        float dz = bz - az;
        float lengthSq = dx * dx + dy * dy + dz * dz;
        if (lengthSq < 1.0E-8F) {
            return;
        }
        // The width is right at both ends of a piece and interpolated in between, so a long edge passing close to
        // the camera is split up, or it would look thick where it is nearest.
        float t = Math.clamp(-(ax * dx + ay * dy + az * dz) / lengthSq, 0.0F, 1.0F);
        float nearest = Vector3f.length(ax + dx * t, ay + dy * t, az + dz * t);
        int pieces = Math.clamp((int) Math.ceil(Math.sqrt(lengthSq) / Math.max(0.5F, nearest * 0.25F)), 1, 64);
        for (int i = 0; i < pieces; i++) {
            float t0 = (float) i / pieces;
            float t1 = (float) (i + 1) / pieces;
            Vector3f p = new Vector3f(ax + dx * t0, ay + dy * t0, az + dz * t0);
            Vector3f q = new Vector3f(ax + dx * t1, ay + dy * t1, az + dz * t1);
            Vector3f sideP = side(dx, dy, dz, p);
            Vector3f sideQ = side(dx, dy, dz, q);
            if (sideP == null || sideQ == null) {
                continue;
            }
            quad(sink, new Vector3f(p).add(sideP), new Vector3f(p).sub(sideP),
                    new Vector3f(q).sub(sideQ), new Vector3f(q).add(sideQ), color);
        }
    }

    /** Points across the line, facing the camera, long enough for the same width on screen at any distance. */
    private static Vector3f side(float dx, float dy, float dz, Vector3f point) {
        Vector3f side = new Vector3f(dx, dy, dz).cross(point);
        float length = side.length();
        if (length < 1.0E-6F) {
            return null;
        }
        return side.mul(HALF_WIDTH * point.length() / length);
    }

    /** Emits the quad with both windings so it shows whichever way culling is set up. */
    private static void quad(VertexSink sink, Vector3f a, Vector3f b, Vector3f c, Vector3f d, int color) {
        sink.vertex(a.x, a.y, a.z, color);
        sink.vertex(b.x, b.y, b.z, color);
        sink.vertex(c.x, c.y, c.z, color);
        sink.vertex(d.x, d.y, d.z, color);
        sink.vertex(d.x, d.y, d.z, color);
        sink.vertex(c.x, c.y, c.z, color);
        sink.vertex(b.x, b.y, b.z, color);
        sink.vertex(a.x, a.y, a.z, color);
    }

    private void addLabels(Quaternionf cameraRotation, ToIntFunction<String> textWidth) {
        BlockPos min = selection.min();
        BlockPos max = selection.max();
        float x0 = (float) (min.getX() - cameraX);
        float y0 = (float) (min.getY() - cameraY);
        float z0 = (float) (min.getZ() - cameraZ);
        float x1 = (float) (max.getX() + 1 - cameraX);
        float y1 = (float) (max.getY() + 1 - cameraY);
        float z1 = (float) (max.getZ() + 1 - cameraZ);
        Vector3f alongX = new Vector3f(x1 - x0, 0, 0);
        Vector3f alongY = new Vector3f(0, y1 - y0, 0);
        Vector3f alongZ = new Vector3f(0, 0, z1 - z0);
        // Each size goes on the middle of whichever of its four parallel edges is closest to the camera
        Vector3f length = nearest(new Vector3f((x0 + x1) / 2, y0, z0), alongY, alongZ);
        Vector3f height = nearest(new Vector3f(x0, (y0 + y1) / 2, z0), alongX, alongZ);
        Vector3f width = nearest(new Vector3f(x0, y0, (z0 + z1) / 2), alongX, alongY);
        labels.add(label(length, Integer.toString(selection.length()), cameraRotation, textWidth));
        labels.add(label(height, Integer.toString(selection.height()), cameraRotation, textWidth));
        labels.add(label(width, Integer.toString(selection.width()), cameraRotation, textWidth));
    }

    /** Returns whichever of start, start + u, start + v and start + u + v is nearest to the camera. */
    private static Vector3f nearest(Vector3f start, Vector3f u, Vector3f v) {
        Vector3f best = null;
        for (int i = 0; i < 4; i++) {
            Vector3f point = new Vector3f(start);
            if ((i & 1) != 0) {
                point.add(u);
            }
            if ((i & 2) != 0) {
                point.add(v);
            }
            if (best == null || point.lengthSquared() < best.lengthSquared()) {
                best = point;
            }
        }
        return best;
    }

    private static Label label(Vector3f at, String text, Quaternionf cameraRotation, ToIntFunction<String> textWidth) {
        // Labels keep their size on screen once they are a few blocks away
        float scale = Math.max(0.025F, at.length() * 0.0035F);
        Matrix4f pose = new Matrix4f().translation(at).rotate(cameraRotation).scale(scale, -scale, scale);
        int width = textWidth.applyAsInt(text);
        return new Label(pose, text, -(width - 1) / 2.0F, -3.5F, width);
    }
}
