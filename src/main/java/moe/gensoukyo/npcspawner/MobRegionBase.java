package moe.gensoukyo.npcspawner;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class MobRegionBase {

    private String key;
    private WorldCheck worldCheck;
    private GeometryRegion[] regions, excludes;

    protected MobRegionBase(String key, WorldCheck wc, GeometryRegion[] regions, GeometryRegion[] excludes) {
        this.key = key;
        this.worldCheck = wc;
        this.regions = regions;
        this.excludes = excludes;
    }

    public boolean isVecIn(World world, Vec3d vec3d) {
        if (!this.worldCheck.checkWorld(world)) return false;
        for (GeometryRegion gr : excludes) if (gr.isIn(vec3d.x, vec3d.y, vec3d.z)) return false;
        for (GeometryRegion gr : regions) if (gr.isIn(vec3d.x, vec3d.y, vec3d.z)) return true;
        return false;
    }

    public static class WorldCheck {
        private final boolean useDim;
        private int dim;
        private String name;
        public WorldCheck(int dim) {
            useDim = true;
            this.dim = dim;
        }

        public WorldCheck(String name) {
            useDim = false;
            this.name = name;
        }

        public boolean checkWorld(World world) {
            if (useDim) {
                return world.provider.getDimension() == dim;
            } else {
                return world.getWorldInfo().getWorldName().equals(name);
            }
        }
    }
}
