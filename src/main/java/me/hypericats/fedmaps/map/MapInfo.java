package me.hypericats.fedmaps.map;

import net.minecraft.item.map.MapState;

import java.awt.*;

public class MapInfo {
    private int roomSize;
    private int halfRoomSize;
    private Point startCorner;


    public MapInfo(byte[] colors) {
        calibrateMap(colors);
    }

    private void calibrateMap(byte[] colors) {
        Point result = findEntranceCorner(colors);

        if (result.y == 16 || result.y == 18) {
            roomSize = result.y;
            halfRoomSize = roomSize / 2;

            startCorner = switch (StateManager.getFloor()) {
                case 0 -> new Point(22, 22);
                case 1 -> new Point(22, 11);
                case 2, 3 -> new Point(11, 11);
                default -> {
                    int startX = result.x & 127;
                    int startZ = result.x >> 7;
                    yield new Point(
                            startX % (roomSize + 4),
                            startZ % (roomSize + 4)
                    );
                }
            };
        }

    }

    private Point findEntranceCorner(byte[] colors) {
        int start = 0;
        int currLength = 0;

        if (colors != null) {
            for (int index = 0; index < colors.length; index++) {
                if ((colors[index] & 0xFF) == 30) {
                    if (currLength == 0) {
                        start = index;
                    }
                    currLength++;
                } else {
                    if (currLength >= 16) {
                        return new Point(start, currLength);
                    }
                    currLength = 0;
                }
            }
        }
        return new Point(start, currLength);
    }

    public int getRoomSize() {
        return roomSize;
    }

    public int getHalfRoomSize() {
        return halfRoomSize;
    }

    public Point getStartCorner() {
        return startCorner;
    }
}
