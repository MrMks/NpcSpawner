package moe.gensoukyo.npcspawner.mob;

import java.util.Arrays;

public class SpawnParamSet {
    // 0 to 6, 6 in sum.
    private final String[] srcAry;
    private final int[] intAry;
    private final boolean[] bAry;
    private final int size;

    public SpawnParamSet(String[] src) {
        if (src == null) throw new NullPointerException();
        size = src.length;
        if (size > 6) throw new IllegalArgumentException("SpawnParamSet can only accept 6 params, but given " + size);

        srcAry = src;
        intAry = new int[size];
        bAry = new boolean[size];
        Arrays.fill(bAry, false);
    }

    public int size() {
        return size;
    }

    public int getInt(int index) throws NumberFormatException {
        if (!bAry[index]) {
            intAry[index] = Integer.parseInt(srcAry[index]);
        }
        return intAry[index];
    }

    public String getString(int index) {
        return srcAry[index];
    }

    public String toReadString() {
        return Arrays.toString(srcAry);
    }

    @Override
    public String toString() {
        return "SpawnParamSet{" +
                "srcAry=" + Arrays.toString(srcAry) +
                '}';
    }
}
