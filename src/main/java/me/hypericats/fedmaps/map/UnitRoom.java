package me.hypericats.fedmaps.map;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

import java.awt.*;

public class UnitRoom {
    public static final int roomSize = 32;
    public static final int haveRoomSize = roomSize / 2;

    private final BlockPos center;
    private RoomData data;
    private int nameHash;
    private int core = -1;
    private byte connections;
    private byte specialDoors;
    private boolean isTopRightMost;

    private final Box2D box;

    public UnitRoom(BlockPos center) {
        this.box = new Box2D(new Point(center.getX(), center.getZ()), 15 ,15);
        this.center = center;
    }

    public Box2D getBox() {
        return this.box;
    }

    // May and will return null be careful
    public BlockPos toLocalPosition(BlockPos absolutePos) {
        if (!box.to3d(0, 256).contains((absolutePos.toCenterPos()))) return null;
        return absolutePos.subtract(new Vec3i(box.getMin().x, 0, box.getMin().y));
    }

    public BlockPos toAbsolutePosition(BlockPos localPos) {
        return localPos.add(new Vec3i(box.getMin().x, 0, box.getMin().y));
    }

    public RoomData getRoomData() {
        return this.data;
    }

    public boolean isChunkLoaded() {
        return (MinecraftClient.getInstance().world != null) && MinecraftClient.getInstance().world.getChunkManager().isChunkLoaded(center.getX() >> 4, center.getZ() >> 4);
    }

    public short getConnections() {
        return connections;
    }

    public boolean hasData() {
        return data != null;
    }

    public boolean shouldDrawText() {
        return isTopRightMost;
    }

    public boolean loadData(int roomX, int roomY) {
        if (!this.isChunkLoaded()) return false;
        int core = getCore();
        this.data = RoomTypeHandler.getFromCore(core); // Might get bad core but we don't care it will be null either way so
        if (this.data == null) return false;
        this.nameHash = this.data.name().hashCode();
        this.connections = 0;
        this.connections |= (byte) (shouldConnect(roomX, roomY, Direction2D.NORTH) ? 0b1 : 0); // check for already set, negate
        this.connections |= (byte) (shouldConnect(roomX, roomY, Direction2D.EAST) ? 0b10 : 0);
        this.connections |= (byte) (shouldConnect(roomX, roomY, Direction2D.SOUTH) ? 0b100 : 0);
        this.connections |= (byte) (shouldConnect(roomX, roomY, Direction2D.WEST) ? 0b1000 : 0);
        this.connections |= (byte) (shouldHaveDoorConnection(roomX, roomY, Direction2D.NORTH) ? 0b10000 : 0);
        this.connections |= (byte) (shouldHaveDoorConnection(roomX, roomY, Direction2D.EAST) ? 0b100000 : 0);
        this.connections |= (byte) (shouldHaveDoorConnection(roomX, roomY, Direction2D.SOUTH) ? 0b1000000 : 0);
        this.connections |= (byte) (shouldHaveDoorConnection(roomX, roomY, Direction2D.WEST) ? 0b10000000 : 0);

        if (!this.hasRoomConnections()) isTopRightMost = true;
        return true;
    }

    private boolean shouldHaveDoorConnection(int roomX, int roomY, Direction2D dir) {
        if (!isDoor(dir)) return false;
        UnitRoom room = getOffsetRoomFromIndexPos(roomX, roomY, dir);
        if (room == null || !room.hasData() || !room.hasDoorConnection(dir.negate())) {
            checkSpecialDoors(dir);
            return true;
        }

        if (this.getData().type() == RoomType.NORMAL) return false;
        room.clearDoorStatus(dir.negate());
        checkSpecialDoors(dir);
        return true;
    }

    private void checkSpecialDoors(Direction2D dir) {
        if (isBlockDoorWither(dir)) {
            specialDoors |= (byte) ((0b1) << (dir.toIndex()));
            return;
        }
        if (isBlockDoorBlood(dir)) specialDoors |= (byte) ((0b1) << (dir.toIndex() + 4));
    }

    private void clearDoorStatus(Direction2D dir) {
        switch (dir) {
            case NORTH -> {
                connections &= ~(byte)0b10000;
                return;
            }

            case EAST -> {
                connections &= ~(byte)0b100000;
                return;
            }

            case SOUTH -> {
                connections &= ~(byte)0b1000000;
                return;
            }

            case WEST -> {
                connections &= ~((byte)0b10000000);
                return;
            }
        }
    }



    public boolean hasRoomConnections() {
        return (connections & 0b1111) != 0;
    }

    public boolean hasDoorConnections() {
        return (connections & 0b11110000) != 0;
    }

    public boolean hasRoomConnection(Direction2D dir) {
        switch (dir) {
            case NORTH -> {
                return (connections & 0b1) != 0;
            }

            case EAST -> {
                return (connections & 0b10) != 0;
            }

            case SOUTH -> {
                return (connections & 0b100) != 0;
            }

            case WEST -> {
                return (connections & 0b1000) != 0;
            }
        }
        return false;
    }

    public boolean hasDoorConnection(Direction2D dir) {
        switch (dir) {
            case NORTH -> {
                return (connections & 0b10000) != 0;
            }

            case EAST -> {
                return (connections & 0b100000) != 0;
            }

            case SOUTH -> {
                return (connections & 0b1000000) != 0;
            }

            case WEST -> {
                return (connections & 0b10000000) != 0;
            }
        }
        return false;
    }

    public boolean isDoorWither(Direction2D dir) {
        return (specialDoors & (((byte)0b1) << (dir.toIndex()))) != 0;
    }

    public boolean isDoorBlood(Direction2D dir) {
        return (specialDoors & (((byte)0b1) << (dir.toIndex() + 4))) != 0;
    }

    private boolean isBlockDoorWither(Direction2D dir) {
        if (MinecraftClient.getInstance().world == null) return false;
        BlockPos offset = dir.toBlockPos().multiply(haveRoomSize);
        int x = center.getX() + offset.getX();
        int z = center.getZ() + offset.getZ();
        return MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, 70, z)).getBlock() == Blocks.COAL_BLOCK;
    }

    private boolean isBlockDoorBlood(Direction2D dir) {
        if (MinecraftClient.getInstance().world == null) return false;
        BlockPos offset = dir.toBlockPos().multiply(haveRoomSize);
        int x = center.getX() + offset.getX();
        int z = center.getZ() + offset.getZ();
        return MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, 70, z)).getBlock() == Blocks.RED_CONCRETE;
    }


    public boolean isDoor(Direction2D dir) {
        if (MinecraftClient.getInstance().world == null || !MinecraftClient.getInstance().world.getChunkManager().isChunkLoaded(center.getX() >> 4, center.getZ() >> 4)) return false;
        BlockPos offset = dir.toBlockPos().multiply(haveRoomSize);
        int x = center.getX() + offset.getX();
        int z = center.getZ() + offset.getZ();
        return MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, 68, z)).getBlock() != Blocks.AIR && MinecraftClient.getInstance().world.getBlockState(new BlockPos(x, this.getMaxHeight(center), z)).getBlock() == Blocks.AIR;
    }


    public boolean shouldConnect(int xIndex, int yIndex, Direction2D dir) {
        UnitRoom otherRoom = getOffsetRoomFromIndexPos(xIndex, yIndex, dir);
        boolean bl = otherRoom != null && isSameRoom(otherRoom) && (this.getRoomData().cores().length == 4 || !otherRoom.hasRoomConnection(dir.negate())); // 4 because 2x2 look weird this is ad hoc fix
        if (bl && otherRoom.isTopRightMost && (dir == Direction2D.WEST || dir == Direction2D.SOUTH)) {
            otherRoom.isTopRightMost = false;
            this.isTopRightMost = true;
        }
        return bl;
    }

    public static UnitRoom getOffsetRoomFromIndexPos(int xIndex, int yIndex, Direction2D dir) {
        switch (dir) {
            case NORTH -> {
                return DungeonScan.getRoomFromOrderedIndex(xIndex, yIndex - 1);
            }

            case EAST -> {
                return DungeonScan.getRoomFromOrderedIndex(xIndex + 1, yIndex);
            }

            case SOUTH -> {
                return DungeonScan.getRoomFromOrderedIndex(xIndex, yIndex + 1);
            }

            case WEST -> {
                return DungeonScan.getRoomFromOrderedIndex(xIndex - 1, yIndex);
            }
        }

        return null;
    }

    public boolean isSameRoom(UnitRoom room) {
        return room != null && room.hasData() && room.getNameHash() == this.getNameHash();
    }

    public RoomData getData() {
        return data;
    }

    public int getNameHash() {
        return this.nameHash;
    }


    // Stolen form odin
    public int getCore() {
        if (core != -1) return core;

        if (MinecraftClient.getInstance().world == null) return -1;
        StringBuilder sb = new StringBuilder(150);

        int roomHeight = getMaxHeight(center);
        roomHeight = Math.max(11, Math.min(140, roomHeight));

        for (int i = 0; i < 140 - roomHeight; i++) {
            sb.append('0');
        }

        int bedrock = 0;
        MutableBlockPos mutableBlockPos = new MutableBlockPos(center);

        for (int y = roomHeight; y >= 12; y--) {
            mutableBlockPos.setY(y);
            Block block = MinecraftClient.getInstance().world.getBlockState(mutableBlockPos).getBlock();

            if (block == Blocks.AIR && bedrock >= 2 && y < 69) {
                for (int i = 0; i < y - 11; i++) {
                    sb.append('0');
                }
                break;
            }

            if (block == Blocks.BEDROCK) {
                bedrock++;
            } else {
                bedrock = 0;
                if (block == Blocks.OAK_PLANKS || block == Blocks.TRAPPED_CHEST || block == Blocks.CHEST) continue;
            }

            sb.append(block);
        }

        core = sb.toString().hashCode();
        if (core == -318865360) return -1; // Empty room core
        return core;
    }

    public int getMaxHeight(BlockPos b) {
        if (MinecraftClient.getInstance().world == null) return -1;
        MutableBlockPos pos = new MutableBlockPos(b);
        for (int y = 160; y >= 12; y--) {
            pos.setY(y);
            BlockState state = MinecraftClient.getInstance().world.getBlockState(pos);

            if (state != null && !state.isAir()) {
                return state.getBlock() == Blocks.GOLD_BLOCK ? y - 1 : y;
            }
        }
        return 0;
    }

    public boolean hasRoomData() {
        return this.data != null;
    }



}
