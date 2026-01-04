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

    public boolean isSameAxis(Direction2D d) {
        return d != null &&  (d == this || d == this.negate());
    }

    public int toIndex() {
        switch (this) {
            case NORTH -> {
                return 0;
            }

            case EAST -> {
                return 1;
            }

            case WEST -> {
                return 2;
            }

            case SOUTH -> {
                return 3;
            }
        }
        return -1;
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
