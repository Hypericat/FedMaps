package me.hypericats.fedmaps.map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class MutableBlockPos extends BlockPos {
    public MutableBlockPos(int i, int j, int k) {
        super(i, j, k);
    }

    public MutableBlockPos(Vec3i pos) {
        super(pos);
    }

    public void set(int x, int y, int z) {
        this.setX(x);
        this.setY(y);
        this.setZ(z);
    }

    @Override
    public Vec3i setX(int x) {
        return super.setX(x);
    }

    @Override
    protected Vec3i setY(int y) {
        return super.setY(y);
    }

    @Override
    protected Vec3i setZ(int z) {
        return super.setZ(z);
    }
}
