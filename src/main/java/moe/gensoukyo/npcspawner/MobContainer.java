package moe.gensoukyo.npcspawner;

public class MobContainer {

    private final MobPrototype mob;
    private final int weight;
    private int count;

    public MobContainer(MobPrototype mob, int weight) {
        this.weight = weight;
        this.count = weight;
        this.mob = mob;
    }

    public int getCount() {
        return count;
    }

    public void reduceCount() {
        count--;
    }

    public void resetCount() {
        this.count = weight;
    }

    public MobPrototype getMob() {
        return mob;
    }
}
