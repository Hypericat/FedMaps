package me.hypericats.fedmaps.map;

import me.hypericats.fedmaps.render.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.map.MapDecoration;
import net.minecraft.item.map.MapDecorationTypes;
import net.minecraft.item.map.MapState;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.MapUpdateS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.awt.*;
import java.util.*;

public class DungeonScan {
    private static final UnitRoom[] orderedRooms = new UnitRoom[36];
    private static final HashMap<String, DungeonPlayer> dungeonPlayers = new HashMap<>(4);
    private static final HashMap<Long, UnitRoom> unitRooms = new HashMap<>();
    private static UnitRoom currentRoom = null;
    private static int tick = 0;
    private static boolean dungeonStarted;
    private static MapInfo mapInfo;


    private DungeonScan() {

    }

    public static void init() {

    }

    public static void onRenderLast(MatrixStack matrices, VertexConsumerProvider vertices, float partialTicks) {
        if (currentRoom != null) {
            RenderUtils.drawOutlinedAABB(matrices, vertices, currentRoom.getBox().to3dCentered(70, 20), 0xFFFFFFFF, 2.0f);
        }
    }

    public static void onWordLoad() {
        unitRooms.clear();
        dungeonPlayers.clear();
        mapInfo = null;
        dungeonStarted = false;
        currentRoom = null;
    }

    public static void onPostLocationUpdate() {
        if (StateManager.getLocation() != Location.Dungeon) return;

        for (int x = 0; x < 6; x++) {
            for (int z = 0; z < 6; z++) {
                int xPos = UnitRoom.startX + x * UnitRoom.roomSize;
                int zPos = UnitRoom.startZ + z * UnitRoom.roomSize;

                UnitRoom unit = new UnitRoom(new BlockPos(xPos, 70, zPos));

                unitRooms.put(encodeIndex(x, z), unit);
                orderedRooms[x * 6 + z] = unit;
                boolean bl = unit.loadData(x, z);
            }
        }
    }

    public static Iterable<DungeonPlayer> getDungeonPlayers() {
        return dungeonPlayers.values();
    }

    public static void preloadSkins() {
        if (MinecraftClient.getInstance().getNetworkHandler() == null) return;
        for (PlayerListEntry p : MinecraftClient.getInstance().getNetworkHandler().getPlayerList()) {
            if (p.getDisplayName() != null && p.getDisplayName().getString().startsWith("[")) {
                p.getSkinTextures();
            }
        }
    }

    // Be able to call mid run of rejoin
    public static void onDungeonStart() {
        if (MinecraftClient.getInstance().getNetworkHandler() == null) return;

        dungeonPlayers.clear();
        int icon = 0;
        for (PlayerListEntry p : MinecraftClient.getInstance().getNetworkHandler().getPlayerList()) {
            if (p.getDisplayName() == null) continue;
            String name = p.getDisplayName().getString();

            if (name.startsWith("[")) {
                name = name.substring(name.indexOf("]") + 2).split(" ")[0];
                if (name.isEmpty()) continue;

                if (!name.equals(MinecraftClient.getInstance().getSession().getUsername())) {
                    dungeonPlayers.put(name, new DungeonPlayer(name, icon, p.getSkinTextures()));
                }

                icon++;
            }
        }
    }

    public static UnitRoom getRoomFromOrderedIndex(int index) {
        if (index < 0 || index >= orderedRooms.length) return null;
        return orderedRooms[index];
    }

    public static UnitRoom getRoomFromOrderedIndex(int x, int y) {
        return getRoomFromOrderedIndex(x * 6 + y);
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


    public static Point coordToIndexPoint(BlockPos pos) {
        return new Point(Math.round((pos.getX() - UnitRoom.startX) / (float) UnitRoom.roomSize), Math.round((pos.getZ() - UnitRoom.startZ) / (float) UnitRoom.roomSize)); // Doesnt work, maybe because it always rounds down?
    }

    public static Iterator<UnitRoom> iterateRooms() {
        return unitRooms.values().iterator();
    }

    public static UnitRoom getRoomFromPos(BlockPos pos) {
        return unitRooms.get(encodeIndex(coordToIndexPoint(pos)));
    }

    public static void onReceivePacket(Packet<?> packet) {
        if (StateManager.getLocation() != Location.Dungeon) return;
        if (packet instanceof GameMessageS2CPacket messagePacket) {
            String message = messagePacket.content().getString();

            switch (message) {
                case "Starting in 4 seconds." -> preloadSkins();
                case "§e[NPC] §bMort§f: Here, I found this map when I first entered the dungeon." ->  {
                    onDungeonStart();
                    dungeonStarted = true;
                }
            }
        }

        if (!(packet instanceof MapUpdateS2CPacket mapPacket) || ((mapPacket.mapId().id() & 1000) != 0) || mapPacket.decorations().isEmpty() || MinecraftClient.getInstance().world == null) return;
        dungeonStarted = true;
        if (mapInfo == null) {
            //MapState mapState = FilledMapItem.getMapState(mapPacket.mapId(), MinecraftClient.getInstance().world);
            //if (mapState != null)
            if (mapPacket.updateData().isPresent())
                mapInfo = new MapInfo(mapPacket.updateData().get().colors());
        }

        java.util.Iterator<DungeonPlayer> dungeonPlayerIterator = dungeonPlayers.values().iterator();
        for (MapDecoration deco : mapPacket.decorations().get()) {
            if (!dungeonPlayerIterator.hasNext()) break;
            if (deco.type() != MapDecorationTypes.BLUE_MARKER) continue;

            DungeonPlayer player = dungeonPlayerIterator.next();
            if (player == null) continue;
            player.updatePosition(deco);
        }
    }

    public static MapInfo getMapInfo() {
        return mapInfo;
    }

    public static void onClientTick() {
        //System.out.println("Current loc : " + StateManager.getLocation());
        if (StateManager.getLocation() != Location.Dungeon) return;
        if (MinecraftClient.getInstance().inGameHud != null && currentRoom != null) {
            MinecraftClient.getInstance().inGameHud.setTitle(Text.of(String.valueOf(currentRoom.getCore())));
        }

        tick++;
        if ((tick & 0b111) != 0b111) return;
        if(dungeonStarted && dungeonPlayers.isEmpty()) onDungeonStart(); // If join mid run


        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || MinecraftClient.getInstance().world == null) return;

        currentRoom = getRoomFromPos(player.getBlockPos());

        for (int i = 0; i < orderedRooms.length; i++) {
            if (orderedRooms[i] == null || orderedRooms[i].hasData()) continue;
            orderedRooms[i].loadData(i / 6, i % 6);
        }
    }



    private static void debug(int xPos, int yPos, Direction2D dir) {
        UnitRoom room = UnitRoom.getOffsetRoomFromIndexPos(xPos, yPos, dir);
        String ex = "null";
        if (room != null && room.hasData()) ex = room.getData().name();
        MinecraftClient.getInstance().player.sendMessage(Text.of(dir.toString() + " : " + ex), false);
    }
}
