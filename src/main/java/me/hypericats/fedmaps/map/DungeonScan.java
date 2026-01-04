package me.hypericats.fedmaps.map;

import me.hypericats.fedmaps.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.*;
import java.util.List;

public class DungeonScan {
    private static final UnitRoom[] orderedRooms = new UnitRoom[121];
    private static final HashMap<Long, UnitRoom> unitRooms = new HashMap<>();
    private static UnitRoom currentRoom = null;
    private static int tick = 0;


    private DungeonScan() {

    }

    public static void init() {

    }

    public static void onRenderLast(MatrixStack matrices, VertexConsumerProvider vertices, float partialTicks) {
        if (currentRoom != null) {
            RenderUtils.drawOutlinedAABB(matrices, vertices, currentRoom.getBox().to3dCentered(70, 20), 0xFFFFFFFF, 2.0f);
        }
    }

    public static void onWorldLoad(ClientWorld world) {
        // dungeons check
        unitRooms.clear();
        currentRoom = null;

        for (int x = 0; x <= 10; x++) {
            for (int z = 0; z <= 10; z++) {
                int xPos = UnitRoom.startX + x * UnitRoom.roomSize;
                int zPos = UnitRoom.startZ + z * UnitRoom.roomSize;

                UnitRoom unit = new UnitRoom(new BlockPos(xPos, 70, zPos));

                unitRooms.put(encodeIndex(x, z), unit);
                orderedRooms[x * 11 + z] = unit;
                boolean bl = unit.loadData(x, z);
            }
        }

    }

    public static UnitRoom getRoomFromOrderedIndex(int index) {
        if (index < 0 || index >= orderedRooms.length) return null;
        return orderedRooms[index];
    }

    public static UnitRoom getRoomFromOrderedIndex(int x, int y) {
        return getRoomFromOrderedIndex(x * 11 + y);
    }


    public static long encodeIndex(int x, int z) {
        return (long) x | (((long) z) << 32);
    }

    public static long encodeIndex(Point p) {
        return encodeIndex(p.x, p.y);
    }

    public static Point getPlayerRelativePosition() {
        if (MinecraftClient.getInstance().player == null) return null;
        BlockPos playerPos = MinecraftClient.getInstance().player.getBlockPos();
        return new Point(playerPos.getX() - UnitRoom.startX, playerPos.getZ() - UnitRoom.startZ);
    }

    public static Point getMapEndPosition() {
        return new Point(UnitRoom.startX + 11 * UnitRoom.roomSize, 1);
    }


    public static Point coordToIndexPoint(BlockPos pos) {
        return new Point(Math.round((pos.getX() - UnitRoom.startX) / (float) UnitRoom.roomSize), Math.round((pos.getZ() - UnitRoom.startZ) / (float) UnitRoom.roomSize)); // Doesnt work, maybe because it always rounds down?
    }

    public static Iterator<UnitRoom> iterateRooms() {
        return unitRooms.values().iterator();
    }

    public static UnitRoom getRoomFromPos(BlockPos pos) {
        return unitRooms.get(encodeIndex(coordToIndexPoint(pos)));
    }

    public static void onClientTick() {
        // dungeons check
        tick++;
        if ((tick & 0b111) != 0b111) return;


        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || MinecraftClient.getInstance().world == null) return;

        currentRoom = getRoomFromPos(player.getBlockPos());

        for (int i = 0; i < orderedRooms.length; i++) {
//            if (orderedRooms[i] == currentRoom) {
//                int xPos = i / 11;
//                int yPos = i % 11;
//                if (currentRoom.hasData())
//                    MinecraftClient.getInstance().player.sendMessage(Text.of("Current room : " + xPos + ", " + yPos + " -> " + currentRoom.getRoomData().name()), false);
//                debug(xPos, yPos, Direction2D.NORTH);
//                debug(xPos, yPos, Direction2D.EAST);
//                debug(xPos, yPos, Direction2D.SOUTH);
//                debug(xPos, yPos, Direction2D.WEST);
//            }
            if (orderedRooms[i].hasData()) continue;
            orderedRooms[i].loadData(i / 11, i % 11);
        }
    }

    private static void debug(int xPos, int yPos, Direction2D dir) {
        UnitRoom room = UnitRoom.getOffsetRoomFromIndexPos(xPos, yPos, dir);
        String ex = "null";
        if (room != null && room.hasData()) ex = room.getData().name();
        MinecraftClient.getInstance().player.sendMessage(Text.of(dir.toString() + " : " + ex), false);
    }
}
