package moe.gensoukyo.npcspawner.region;

import moe.gensoukyo.npcspawner.GeometryRegion;

public interface RegionProvider<T extends GeometryRegion> {
    T parse(double[] ary);
}
