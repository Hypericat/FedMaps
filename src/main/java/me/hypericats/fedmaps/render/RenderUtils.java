package me.hypericats.fedmaps.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

import java.util.Collection;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Function;

public class RenderUtils {

    public static final RenderLayer POS_COL_QUADS_NO_DEPTH_TEST = RenderLayer.of(

            "renderer/always_depth_pos_color",

            1024,

            false, true,

            RenderPipelines.register(RenderPipeline.builder(RenderPipelines.POSITION_COLOR_SNIPPET)

                    .withLocation(Identifier.of("renderer", "pipeline/pos_col_quads_nodepth"))

                    .withCull(true)

                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)

                    .withDepthWrite(true)

                    .build()

            ),

            RenderLayer.MultiPhaseParameters.builder()

                    .build(false));

    private static final RenderPipeline LINES_NODEPTH_PIPELINE = RenderPipelines.register(RenderPipeline.builder(RenderPipelines.RENDERTYPE_LINES_SNIPPET)
            .withLocation(Identifier.of("renderer", "pipeline/lines_nodepth"))
            .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
            .withDepthWrite(true)
            .withVertexFormat(VertexFormats.POSITION_COLOR_NORMAL, VertexFormat.DrawMode.LINES)

            .build()
    );

    public static final Function<Double, RenderLayer> LINES_NO_DEPTH_TEST = Util.memoize(width -> RenderLayer.of(
            "renderer/always_depth_lines",
            1024,
            false, true, LINES_NODEPTH_PIPELINE,
            RenderLayer.MultiPhaseParameters.builder()
                    .lineWidth(new RenderPhase.LineWidth(width == 0d ? OptionalDouble.empty() : OptionalDouble.of(width)))
                    .build(false)
    ));



    public static void drawFilledAABB(MatrixStack matrices, VertexConsumerProvider vertices, Box box, int color) {
        matrices.push();
        float alpha = ((color >> 24) & 0xFF) / 255.0F;
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        VertexConsumer buffer = vertices.getBuffer(POS_COL_QUADS_NO_DEPTH_TEST);
        MatrixStack.Entry entry = matrices.peek();
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        Matrix4f matrix = entry.getPositionMatrix().translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z); // move outside loop


        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        buffer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha);
        matrices.pop();
    }

    public static void drawOutlinedAABB(MatrixStack matrices, VertexConsumerProvider vertices, Box box, int color, float thickness) {
        matrices.push();
        float alpha = ((color >> 24) & 0xFF) / 255.0F;
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;

        VertexConsumer consumer = vertices.getBuffer(LINES_NO_DEPTH_TEST.apply((double) thickness));
        MatrixStack.Entry entry = matrices.peek();
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        Matrix4f matrix = entry.getPositionMatrix().translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z); // move outside loop


        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).normal(-1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).normal(-1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, -1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, -1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, -1.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, -1.0f);
        consumer.vertex(matrix, (float) box.minX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).normal(1.0f, 0.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.minY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, 1.0f, 0.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.minZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        consumer.vertex(matrix, (float) box.maxX, (float) box.maxY, (float) box.maxZ).color(red, green, blue, alpha).normal(0.0f, 0.0f, 1.0f);
        matrices.pop();
    }

    public static void drawOutlinedRectangle(DrawContext context, int x, int y, int width, int height, int color, int borderWidth) {
        context.fill(x, y, x + width, y + borderWidth, color);
        context.fill(x, y + height - borderWidth, x + width, y + height, color);
        context.fill(x, y + borderWidth, x + borderWidth, y + height - borderWidth, color);
        context.fill(x + width - borderWidth, y + borderWidth, x + width, y + height - borderWidth, color);
    }

    public static void drawLines(MatrixStack matrices, VertexConsumerProvider vertices, List<Vec3d> points, int color, float width, boolean depth) {
        if (points.size() < 2) return;
        matrices.push();

        float a = ((color >> 24) & 0xFF) / 255.0F;
        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;



        RenderLayer layer = depth ? RenderLayer.getLines() : LINES_NO_DEPTH_TEST.apply((double) width);


        VertexConsumer consumer = vertices.getBuffer(layer);
        MatrixStack.Entry entry = matrices.peek();
        Vec3d cameraPos = MinecraftClient.getInstance().gameRenderer.getCamera().getCameraPos();
        Matrix4f matrix = entry.getPositionMatrix().translate((float) -cameraPos.x, (float) -cameraPos.y, (float) -cameraPos.z); // move this outside loop

        for (int i = 0; i < points.size() - 1; i++) {
            Vec3d p1 = points.get(i);
            Vec3d p2 = points.get(i + 1);

            Vec3d normals = p2.subtract(p1).normalize();


            consumer.vertex(matrix, (float) p1.x, (float) p1.y, (float) p1.z)
                    .color(r, g, b, a)
                    .normal(entry, (float) normals.x, (float) normals.y, (float) normals.z);

            consumer.vertex(matrix, (float) p2.x, (float) p2.y, (float) p2.z)
                    .color(r, g, b, a)
                    .normal(entry, (float) normals.x, (float) normals.y, (float) normals.z);
        }
        matrices.pop();
    }

    public static Box getPartialEntityBoundingBox(Entity entity, float partialTicks) {
        double lerpX = entity.lastX + (entity.getX() - entity.lastX) * partialTicks;
        double lerpY = entity.lastY + (entity.getY() - entity.lastY) * partialTicks;
        double lerpZ = entity.lastZ + (entity.getZ() - entity.lastZ) * partialTicks;

        return entity.getBoundingBox().offset(lerpX - entity.getX(), lerpY - entity.getY(), lerpZ - entity.getZ());
    }


}
