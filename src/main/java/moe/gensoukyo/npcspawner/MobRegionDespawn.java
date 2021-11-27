package moe.gensoukyo.npcspawner;

public class MobRegionDespawn extends MobRegionBase {

    private String[] mob;
    public MobRegionDespawn(String key, MobRegionBase.WorldCheck wc, GeometryRegion[] region, GeometryRegion[] exclude, String[] mob) {
        super(key, wc, region, exclude);
        this.mob = mob;
    }
}
