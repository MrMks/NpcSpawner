package moe.gensoukyo.npcspawner.location;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

public interface LocationProvider {
    Vec3d findLocationToSpawn(WorldServer ws, Vec3d playerPos, double disX, double dixZ, double[] params);
}
