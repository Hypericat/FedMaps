package me.hypericats.fedmaps.map;

import me.hypericats.fedmaps.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.Identifier;
import org.joml.Matrix3x2fStack;

import java.awt.*;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class MapRenderer {
    private static int mapX = 10;
    private static int mapY = 10;

    private static int borderWidth = 1;
    private static int markerSize = 8; // 5
    private static int textSize = 4;
    private static boolean showSecretCount = true;
    private static boolean renderOtherPlayerHead = true;
    private static boolean renderPlayerHead = true;

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
        if (StateManager.getLocation() != Location.Dungeon) return;

        RenderUtils.drawOutlinedRectangle(context, mapX, mapY, getMapSize(), getMapSize(), 0xFF000000, borderWidth);
        context.fill(mapX, mapY, mapX + getMapSize(), mapY + getMapSize(), 0x34FFFFFF);

        Matrix3x2fStack matrices = context.getMatrices();
        matrices.pushMatrix();
        matrices.scale(0.1f); // gives us more precision

        int scaledConnectorSize = (int) (3F / 10.0F * getMapSize()); // 7 connectors
        int scaledRoomSize = (int) (13.16666667F / 10.0F * getMapSize()); // 6 rooms

        // Need to render first
        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 6; y++) {
                UnitRoom room = DungeonScan.getRoomFromOrderedIndex(x, y);
                if (room == null || !room.hasData()) continue;
                int xOffset = ((int) (10.0f * mapX)) + scaledConnectorSize + scaledRoomSize * x + scaledConnectorSize * x;
                int yOffset = ((int) (10.0f * mapY)) + scaledConnectorSize + scaledRoomSize * y + scaledConnectorSize * y;

                if (room.hasDoorConnections()) {
                    for (Direction2D d : Direction2D.values()) {
                        if (room.hasDoorConnection(d)) {
                            MutableBox2D box2D = new MutableBox2D(new Point(xOffset + scaledRoomSize / 2, yOffset + scaledRoomSize / 2), scaledConnectorSize / 3 * 2, scaledConnectorSize / 3 * 2);
                            box2D.expandTowards(d, (scaledRoomSize + scaledConnectorSize) / 2);
                            context.fill(box2D.minX, box2D.minY, box2D.maxX, box2D.maxY, room.isDoorWither(d) ? 0xFF000000 : getRoomColor(room));
                        }
                    }
                }
            }
        }

        for (int x = 0; x < 6; x++) {
            for (int y = 0; y < 6; y++) {
                UnitRoom room = DungeonScan.getRoomFromOrderedIndex(x, y);
                if (room == null || !room.hasData()) continue;
                int xOffset = ((int) (10.0f * mapX)) + scaledConnectorSize + scaledRoomSize * x + scaledConnectorSize * x;
                int yOffset = ((int) (10.0f * mapY)) + scaledConnectorSize + scaledRoomSize * y + scaledConnectorSize * y;

                if (!room.hasRoomConnections()) {
                    context.fill(xOffset, yOffset, xOffset + scaledRoomSize, yOffset + scaledRoomSize, getRoomColor(room));
                } else {
                    MutableBox2D renderBox = new MutableBox2D(xOffset, yOffset, xOffset + scaledRoomSize, yOffset + scaledRoomSize);
                    Direction2D expanded = null;
                    MutableBox2D back = null;

                    for (Direction2D d : Direction2D.values()) {
                        if (room.hasRoomConnection(d)) {
                            if (expanded == null || expanded.isSameAxis(d) || room.getData().cores().length > 3) { // cancer
                                renderBox.expandTowards(d, scaledConnectorSize);
                                expanded = d;
                            } else {
                                back = new MutableBox2D(xOffset, yOffset, xOffset + scaledRoomSize, yOffset + scaledRoomSize); // To deal with L room, ts is cancer
                                back.expandTowards(d, scaledConnectorSize);
                            }
                        }
                    }

                    context.fill(renderBox.minX, renderBox.minY, renderBox.maxX, renderBox.maxY, getRoomColor(room));
                    if (back != null) context.fill(back.minX, back.minY, back.maxX, back.maxY, getRoomColor(room));
                }

                int scaledTextSize = (int) (textSize / 100.0f * getMapSize());
                matrices.pushMatrix();
                matrices.scale(scaledTextSize);
                if (room.shouldDrawText()) {
                    List<String> lines = new ArrayList<>(List.of(room.getData().name().split("[ -]")));
                    if (showSecretCount && room.getData().secrets() > 0) lines.add(String.valueOf(room.getData().secrets()));
                    if (lines.isEmpty()) continue;

                    int spacing = 3;
                    int height = (MinecraftClient.getInstance().textRenderer.fontHeight * lines.size() + spacing * (lines.size() - 1)) * scaledTextSize;
                    int yCoord = yOffset + (scaledRoomSize - height) / 2;

                    for (String s : lines) {
                        int textWidth = MinecraftClient.getInstance().textRenderer.getWidth(s);
                        context.drawText(MinecraftClient.getInstance().textRenderer, s, (xOffset + (scaledRoomSize - textWidth * scaledTextSize) / 2) / scaledTextSize, yCoord / scaledTextSize, 0xFFFFFFFF, true);
                        yCoord += (MinecraftClient.getInstance().textRenderer.fontHeight + spacing) * scaledTextSize;

                    }
                }
                matrices.popMatrix();
            }
        }

        try {
            for (DungeonPlayer player : DungeonScan.getDungeonPlayers()) {
                int xOffset = (int) ((10.0f * mapX) + scaledConnectorSize + player.getMapX() * (scaledRoomSize));
                int yOffset = (int) ((10.0f * mapY) + scaledConnectorSize + player.getMapZ() * (scaledRoomSize));
                renderPlayer(xOffset, yOffset, player.getYaw(), context, matrices, 0xFF0000FF, renderOtherPlayerHead ? player.getTextures() : null, player.isRenderHat());
            }
        } catch (ConcurrentModificationException e) {
            // Happens sometimes when adding them
            e.printStackTrace();
        }

        Point playerPos = DungeonScan.getPlayerRelativePosition();
        if (playerPos != null && MinecraftClient.getInstance().player != null) {
            int xOffset = (int) ((10.0f * mapX) + (scaledRoomSize) / 2.0f + scaledConnectorSize + playerPos.x / 32.0f * (scaledRoomSize + scaledConnectorSize));
            int yOffset = (int) ((10.0f * mapY) + (scaledRoomSize) / 2.0f + scaledConnectorSize + playerPos.y / 32.0f * (scaledRoomSize + scaledConnectorSize));
            renderPlayer(xOffset, yOffset, (float) Math.toRadians(180f + MinecraftClient.getInstance().player.getYaw(counter.getTickProgress(true))), context, matrices, 0xFF00FF00, renderPlayerHead ? MinecraftClient.getInstance().player.getSkin() : null, MinecraftClient.getInstance().player.isModelPartVisible(PlayerModelPart.HAT));
        }

        matrices.popMatrix();
    }

    private static void renderPlayer(float x, float y, float yaw, DrawContext context, Matrix3x2fStack matrices, int color, SkinTextures skinTextures, boolean hat) {
        matrices.pushMatrix();
        matrices.rotateAbout(yaw, x, y);

        int scaledMarkerSize = (int) (markerSize / 10.0f * getMapSize());
        float halfMarker = scaledMarkerSize / 2.0f;

        if (skinTextures == null) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, MAP_MARKER, (int) (x - halfMarker), (int) (y - halfMarker), 0, 0, scaledMarkerSize, scaledMarkerSize, scaledMarkerSize, scaledMarkerSize, color);
            matrices.popMatrix();
            return;
        }

        RenderUtils.drawOutlinedRectangle(context, (int) (x - halfMarker), (int) (y - halfMarker), scaledMarkerSize, scaledMarkerSize, 0xFF000000, 10);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTextures.body().texturePath(), (int) (x - halfMarker), (int) (y - halfMarker), 8f, 8f, scaledMarkerSize, scaledMarkerSize, 8, 8, 64, 64);
        if (hat)
            context.drawTexture(RenderPipelines.GUI_TEXTURED, skinTextures.body().texturePath(), (int) (x - halfMarker), (int) (y - halfMarker), 40f, 8f, scaledMarkerSize, scaledMarkerSize, 8, 8, 64, 64);
        matrices.popMatrix();
    }

    private static int getRoomColor(UnitRoom room) {
        if (room.getRoomData() == null) return 0xFF000000;
        switch (room.getRoomData().type()) {
            case NORMAL -> {
                return DungeonScan.isMimicRoom(room) ? mimicColor : normalColor;
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
