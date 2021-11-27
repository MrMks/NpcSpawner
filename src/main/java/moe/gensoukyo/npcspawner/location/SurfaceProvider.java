package moe.gensoukyo.npcspawner.location;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public class SurfaceProvider implements LocationProvider {

    @Override
    public Vec3d findLocationToSpawn(WorldServer ws, Vec3d playerPos, double disX, double dixZ, double[] params) {
        Vec3d tar = playerPos.addVector(disX, 0, dixZ);
        double dy_min = -5, dy_max = 10;
        if (params.length > 0) dy_min = (int) params[0];
        if (params.length > 1) dy_max = (int) params[1];

        dy_min += tar.y;
        dy_max += tar.y;

        int iy_min, iy_max;
        if (dy_min < 0) iy_min = 0; else iy_min = MathHelper.floor(dy_min);
        if (dy_max > 255) iy_max = 255; else iy_max = MathHelper.floor(dy_max);

        BlockPos bp = new BlockPos(tar.x, iy_min, tar.z);
        boolean solid = false;
        while (iy_min++ <= iy_max) {
        }

        return null;
    }
}
