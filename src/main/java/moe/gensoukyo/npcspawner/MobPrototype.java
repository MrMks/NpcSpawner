package moe.gensoukyo.npcspawner;

import moe.gensoukyo.npcspawner.mob.MobProvider;
import moe.gensoukyo.npcspawner.mob.SpawnParamSet;

public class MobPrototype {

    private final String key;
    private final MobProvider provider;
    private final SpawnParamSet params;

    public MobPrototype(String key, MobProvider provider, SpawnParamSet set) {
        this.key = key;
        this.provider = provider;
        this.params = set;
    }

    public String getKey() {
        return key;
    }

    public MobProvider getProvider() {
        return provider;
    }

    public SpawnParamSet getParams() {
        return params;
    }

    @Override
    public String toString() {
        return "MobPrototype{" +
                "key='" + key + '\'' +
                ", provider=" + provider.getRegisterKey() +
                ", params=" + params.toReadString() +
                '}';
    }
}
