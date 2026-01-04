package me.hypericats.fedmaps.map;

import net.minecraft.util.math.BlockPos;

public enum Direction2D {
    NORTH,
    EAST,
    SOUTH,
    WEST;

    private static final BlockPos NORTH_POS = new BlockPos(0, 0, -1);
    private static final BlockPos EAST_POS = new BlockPos(1, 0, 0);
    private static final BlockPos SOUTH_POS = new BlockPos(0, 0, +1);
    private static final BlockPos WEST_POS = new BlockPos(-1, 0, 0);

    public Direction2D negate() {
        switch (this) {
            case NORTH -> {
                return SOUTH;
            }

            case EAST -> {
                return WEST;
            }

            case WEST -> {
                return EAST;
            }

            case SOUTH -> {
                return NORTH;
            }
        }
        return null;
    }

    public BlockPos toBlockPos() {
        switch (this) {
            case NORTH -> {
                return NORTH_POS;
            }

            case EAST -> {
                return EAST_POS;
            }

            case WEST -> {
                return WEST_POS;
            }

            case SOUTH -> {
                return SOUTH_POS;
            }
        }
        return null;
    }
}
