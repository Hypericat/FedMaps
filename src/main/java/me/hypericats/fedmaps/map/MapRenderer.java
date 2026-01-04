package me.hypericats.fedmaps.map;

import me.hypericats.fedmaps.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.Arrays;

public class MapRenderer {
    private static int mapX = 10;
    private static int mapY = 10;

    private static int borderWidth = 1;
    private static int markerSize = 5;
    private static int textSize = 8;

    private static int bloodColor = 0xFFFF0000;
    private static int entranceColor = 0xFF148500;
    private static int fairyColor = 0xFFe000FF;
    private static int yellowColor = 0xFFFEDF00;
    private static int normalColor = 0xFF6B3A11;
    private static int mimicColor = 0xFFBA4234;
    private static int puzzleColor = 0xFF750085;
    private static int rareColor = 0xFFFFCB59;
    private static int trapColor = 0xFFFF7500;

    private static final Identifier MAP_MARKER = Identifier.of("fedmaps", "marker.png");


    public static void onRenderGui(DrawContext context, RenderTickCounter counter) {
        RenderUtils.drawOutlinedRectangle(context, mapX, mapY, getMapSize(), getMapSize(), 0xFF000000, borderWidth);
        context.fill(mapX, mapY, mapX + getMapSize(), mapY + getMapSize(), 0x34FFFFFF);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(0.1f); // gives us more precision

        int scaledConnectorSize = (int) (3F / 10.0F * getMapSize()); // 7 connectors
        int scaledRoomSize = (int) (13.16666667F / 10.0F * getMapSize()); // 6 rooms

        // Need to render first
        for (int x = 0; x <= 10; x++) {
            for (int y = 0; y <= 10; y++) {
                UnitRoom room = DungeonScan.getRoomFromOrderedIndex(x, y);
                if (!room.hasData()) continue;
                int xOffset = ((int) (10.0f * mapX)) + scaledConnectorSize + scaledRoomSize * x + scaledConnectorSize * x;
                int yOffset = ((int) (10.0f * mapY)) + scaledConnectorSize + scaledRoomSize * y + scaledConnectorSize * y;

                if (room.hasDoorConnections()) {
                    Arrays.stream(Direction2D.values()).forEach(d -> {
                        if (room.hasDoorConnection(d)) {
                            MutableBox2D box2D = new MutableBox2D(new Point(xOffset + scaledRoomSize / 2, yOffset + scaledRoomSize / 2), scaledConnectorSize / 3 * 2, scaledConnectorSize / 3 * 2);
                            box2D.expandTowards(d, (scaledRoomSize + scaledConnectorSize) / 2);
                            context.fill(box2D.minX, box2D.minY, box2D.maxX, box2D.maxY, getRoomColor(room));
                        }
                    });
                }
            }
        }

        for (int x = 0; x <= 10; x++) {
            for (int y = 0; y <= 10; y++) {
                UnitRoom room = DungeonScan.getRoomFromOrderedIndex(x, y);
                if (!room.hasData()) continue;
                int xOffset = ((int) (10.0f * mapX)) + scaledConnectorSize + scaledRoomSize * x + scaledConnectorSize * x;
                int yOffset = ((int) (10.0f * mapY)) + scaledConnectorSize + scaledRoomSize * y + scaledConnectorSize * y;

                if (!room.hasRoomConnections()) {
                    context.fill(xOffset, yOffset, xOffset + scaledRoomSize, yOffset + scaledRoomSize, getRoomColor(room));
                } else {
                    MutableBox2D renderBox = new MutableBox2D(xOffset, yOffset, xOffset + scaledRoomSize, yOffset + scaledRoomSize);
                    Arrays.stream(Direction2D.values()).forEach(d -> {
                        if (room.hasRoomConnection(d)) renderBox.expandTowards(d, scaledConnectorSize);
                    });
                    context.fill(renderBox.minX, renderBox.minY, renderBox.maxX, renderBox.maxY, getRoomColor(room));
                }

                matrices.pushMatrix();
                matrices.scale(textSize);
                if (room.shouldDrawText()) {
                    int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(room.getData().name());
                    context.drawText(MinecraftClient.getInstance().textRenderer, room.getData().name(), xOffset / textSize + scaledRoomSize / textSize - textWidth, yOffset / textSize + scaledRoomSize / textSize / 8, 0xFFFFFFFF, true);
                }
                matrices.popMatrix();
            }
        }

        Point start = DungeonScan.getPlayerRelativePosition();
        if (start != null && MinecraftClient.getInstance().player != null) {
            int xOffset = (int) ((10.0f * mapX) + (scaledRoomSize) / 2.0f + scaledConnectorSize + ((start.x / 32.0f) * (scaledRoomSize + scaledConnectorSize)));
            int yOffset = (int) ((10.0f * mapY) + (scaledRoomSize) / 2.0f + scaledConnectorSize + ((start.y / 32.0f) * (scaledRoomSize + scaledConnectorSize)));
            int scaledMarkerSize = (int) (markerSize / 10.0f * getMapSize());
            float halfMarker = scaledMarkerSize / 2.0f;

            matrices.pushMatrix();
            matrices.rotateAbout((float) Math.toRadians(180f + MinecraftClient.getInstance().player.getYaw(counter.getTickProgress(true))), xOffset, yOffset);

            context.drawTexture(RenderPipelines.GUI_TEXTURED, MAP_MARKER, (int) (xOffset - halfMarker), (int) (yOffset - halfMarker), 0, 0, scaledMarkerSize, scaledMarkerSize, scaledMarkerSize, scaledMarkerSize);
            matrices.popMatrix();
        }

        matrices.popMatrix();
    }

    private static int getRoomColor(UnitRoom room) {
        if (room.getRoomData() == null) return 0xFF000000;
        switch (room.getRoomData().type()) {
            case NORMAL -> {
                return normalColor;
            }

            case RARE -> {
                return rareColor;
            }

            case TRAP -> {
                return trapColor;
            }

            case BLOOD -> {
                return bloodColor;
            }

            case FAIRY -> {
                return fairyColor;
            }

            case CHAMPION -> {
                return yellowColor;
            }

            case PUZZLE -> {
                return puzzleColor;
            }

            case ENTRANCE -> {
                return entranceColor;
            }

            default -> {
                return 0xFF000000;
            }
        }
    }

    private static int getMapSize() {
        return 200;
    }
}
