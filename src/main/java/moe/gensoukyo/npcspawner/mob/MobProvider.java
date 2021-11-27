package moe.gensoukyo.npcspawner.mob;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public interface MobProvider {
    String getRegisterKey();
    boolean testDependency();
    boolean spawn(World world, Vec3d vec3d, SpawnParamSet paramSet);
    int countIn(World world, Vec3d p1, Vec3d p2);
}
