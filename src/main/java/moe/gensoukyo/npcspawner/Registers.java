package moe.gensoukyo.npcspawner;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import moe.gensoukyo.npcspawner.location.LocationProvider;
import moe.gensoukyo.npcspawner.location.SurfaceProvider;
import moe.gensoukyo.npcspawner.mob.CustomNpcsProvider;
import moe.gensoukyo.npcspawner.mob.MobProvider;
import moe.gensoukyo.npcspawner.region.AnyRegion2d;
import moe.gensoukyo.npcspawner.region.RectRegion3d;
import moe.gensoukyo.npcspawner.region.RegionProvider;

import java.util.HashMap;
import java.util.Map;

public class Registers {

    private static final Map<String, MobProvider> mobProvMap = new HashMap<>();
    public static MobProvider getMobProv(String key) {
        return mobProvMap.get(key);
    }

    private static final Map<String, LocationProvider> locProvMap = new HashMap<>();
    public static LocationProvider getLocProv(String key) {
        return locProvMap.get(key);
    }

    private static final Int2ObjectArrayMap<RegionProvider<?>> regionProvMap = new Int2ObjectArrayMap<>();
    public static GeometryRegion generateRegion(int region, double[] params) {
        RegionProvider<?> prov = regionProvMap.get(region);
        if (prov == null) return null;
        GeometryRegion rg = prov.parse(params);
        return rg == null || !rg.isValid() ? null : rg;
    }

    private static void register(MobProvider provider) {
        if (provider.testDependency()) mobProvMap.put(provider.getRegisterKey(), provider);
    }

    private static void register(String key, LocationProvider provider) {
        locProvMap.put(key, provider);
    }

    private static void register(int id, RegionProvider<?> provider) {
        regionProvMap.put(id, provider);
    }

    public static void init() {
        // register MobProviders
        register(new CustomNpcsProvider());     // cnpc

        // register LocationProviders
        register("surface", new SurfaceProvider());

        // register RegionProviders
        register(0, RectRegion3d.Provider);    // 0
        register(1, AnyRegion2d.Provider);     // 1
    }
}
