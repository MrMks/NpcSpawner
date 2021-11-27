package moe.gensoukyo.npcspawner;

import moe.gensoukyo.npcspawner.location.LocationProvider;
import moe.gensoukyo.npcspawner.mob.MobProvider;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;

import java.util.Arrays;
import java.util.stream.Stream;

public class MobRegionSpawn extends MobRegionBase {
    private LocationProvider locProv;
    private double[] locProvParams;
    private MobContainer[] mobs;
    private int density;

    private Stream<MobProvider> mobProvs;

    private int[] weightBake;

    public MobRegionSpawn(
            String key,
            MobRegionBase.WorldCheck world,
            LocationProvider locProv,
            double[] locProvParams,
            GeometryRegion[] region,
            GeometryRegion[] exclude,
            MobContainer[] mobs,
            int density)
    {
        super(key, world, region, exclude);

        this.locProv = locProv;
        this.locProvParams = locProvParams;
        this.mobs = mobs;
        this.density = density;

        mobProvs = Arrays.stream(mobs).map(mobContainer -> mobContainer.getMob().getProvider());
    }

    public LocationProvider getLocProv() {
        return locProv;
    }

    public double[] getLocProvParams() {
        return locProvParams;
    }

    public boolean isMobsOverDensity(WorldServer world, Vec3d p1) {
        return mobProvs.mapToInt(mobProv -> mobProv.countIn(world, p1.addVector(50,50,50), p1.addVector(-50,-50,-50))).sum() >= density;
    }

    public MobPrototype nextMob(double factor) {
        int[] weightList = new int[mobs.length + 1];
        int index = 0;
        weightList[0] = 0;
        for (MobContainer mob : mobs) {
            ++index;
            weightList[index] = weightList[index - 1] + mob.getCount();
        }
        int sum = weightList[index];
        if (sum == 0) {
            index = 0;
            for (MobContainer mob : mobs) {
                ++ index;
                mob.resetCount();
                weightList[index] = weightList[index - 1] + mob.getCount();
            }
            sum = weightList[index];
        }
        int w = (int) Math.floor(weightList[index] * factor);
        while (w > weightList[index - 1]) index--;
        MobContainer mc = mobs[index - 1];
        mc.reduceCount();
        if (sum == 1) {
            for (MobContainer mob : mobs) mob.resetCount();
        }
        return mc.getMob();
    }

}
