package moe.gensoukyo.npcspawner;

import com.google.gson.*;
import moe.gensoukyo.npcspawner.location.LocationProvider;
import moe.gensoukyo.npcspawner.mob.MobProvider;
import moe.gensoukyo.npcspawner.mob.SpawnParamSet;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Stream;

public class SpawnerConfig {
    private static GeometryRegion[] workerAry;
    private static String[] strWorkerAry;
    private static final GeometryRegion[] emptyRegionAry = new GeometryRegion[0];
    private static final String[] defDespawnMobs = new String[]{"*"};

    private int distanceMin, distanceBias, distanceSquare, intervalMin, intervalBias;
    private List<MobRegionSpawn> spawnList;
    private List<MobRegionDespawn> despawnList;

    private File file;
    private boolean correctLoaded;

    public SpawnerConfig init(File file) {
        this.file = file;
        this.correctLoaded = reload();
        return this;
    }

    public boolean reload() {
        if (!loadSettings(file)) return false;

        Map<String, MobPrototype> mobMap = loadMobs(file);
        if (mobMap == null) return false;

        JsonElement regionsJson;
        try {
            regionsJson = parse(new File(file, "regions.json"));
        } catch (IOException e) {
            ModMain.logger.warn("Can't load regions.json", e);
            return false;
        }

        List<MobRegionSpawn> spawnList = loadSpawnRegions(regionsJson, mobMap);
        if (this.spawnList == null) return false;

        List<MobRegionDespawn> despawnList = loadDespawnRegions(regionsJson);

        this.spawnList = new ArrayList<>(spawnList);
        this.despawnList = new ArrayList<>(despawnList);

        if (workerAry != null) {
            Arrays.fill(workerAry, null);
            workerAry = null;
        }
        if (strWorkerAry != null) {
            Arrays.fill(strWorkerAry, null);
            strWorkerAry = null;
        }
        return true;
    }

    public boolean isCorrectLoaded() {
        return correctLoaded;
    }

    public int getIntervalMin() {
        return intervalMin;
    }

    public int getIntervalBias() {
        return intervalBias;
    }

    public int getDistanceMin() {
        return distanceMin;
    }

    public int getDistanceBias() {
        return distanceBias;
    }

    public int getSquareDistance() {
        return distanceSquare;
    }

    public Stream<MobRegionDespawn> despawnStream() {
        return despawnList.stream();
    }

    public Stream<MobRegionSpawn> spawnStream() {
        return spawnList.stream();
    }

    private boolean loadSettings(File file) {
        JsonElement je = null;
        try {
            je = parse(new File(file, "settings.json"));
        } catch (IOException e) {
            ModMain.logger.warn("Can't load settings.json", e);
        }
        if (je != null && je.isJsonObject()) {
            JsonObject jo = je.getAsJsonObject();
            OptionalInt optionalInt = loadInt(jo, "distance-min");
            if (!optionalInt.isPresent()) return false;
            distanceMin = optionalInt.getAsInt();
            distanceSquare = distanceMin * distanceMin;

            optionalInt = loadInt(jo, "distance-max");
            if (!optionalInt.isPresent()) return false;
            distanceBias = optionalInt.getAsInt() - distanceMin;

            optionalInt = loadInt(jo, "interval-min");
            if (!optionalInt.isPresent()) return false;
            intervalMin = optionalInt.getAsInt();

            optionalInt = loadInt(jo, "interval-max");
            if (!optionalInt.isPresent()) return false;
            intervalBias = optionalInt.getAsInt() - intervalMin;
        }
        return false;
    }

    private static OptionalInt loadInt(JsonObject jo, String key) {
        JsonElement je;
        if (jo.has(key) && (je = jo.get(key)).isJsonPrimitive() && je.getAsJsonPrimitive().isNumber())
            return OptionalInt.of(je.getAsInt());
        return OptionalInt.empty();
    }

    private static List<MobRegionSpawn> loadSpawnRegions(JsonElement je, Map<String, MobPrototype> mobKeySet) {
        if (je != null && je.isJsonObject() && je.getAsJsonObject().has("spawn")) {
            je = je.getAsJsonObject().get("spawn");
        } else return null;
        if (je != null && je.isJsonObject()) {
            JsonObject jo = je.getAsJsonObject();
            List<MobRegionSpawn> list = new LinkedList<>();
            for (Map.Entry<String, JsonElement> e : jo.entrySet()) {
                if (e.getValue().isJsonObject()) {
                    JsonObject ro = e.getValue().getAsJsonObject();
                    JsonElement te;
                    JsonObject to;

                    MobRegionSpawn.WorldCheck worldCheck = null;
                    if (ro.has("world") && (te = ro.get("world")).isJsonPrimitive()) {
                        JsonPrimitive tp = te.getAsJsonPrimitive();
                        if (tp.isNumber()) worldCheck = new MobRegionSpawn.WorldCheck(tp.getAsInt());
                        else if (tp.isString()) worldCheck = new MobRegionSpawn.WorldCheck(tp.getAsString());
                    }
                    if (worldCheck == null) continue;

                    GeometryRegion[] regions = null;
                    if (ro.has("region") && (te = ro.get("region")).isJsonArray())
                        regions = parseRegion(te.getAsJsonArray());
                    if (regions == null || regions.length == 0) continue;

                    MobContainer[] mobs = null;
                    if (ro.has("mob") && (te = ro.get("mob")).isJsonArray()) {
                        int index = 0;
                        mobs = new MobContainer[te.getAsJsonArray().size()];
                        for (JsonElement mobE : te.getAsJsonArray()) {
                            if (mobE.isJsonObject()) {
                                to = mobE.getAsJsonObject();
                                String prototype = null;
                                if (to.has("prototype") && (te = to.get("prototype")).isJsonPrimitive()) {
                                    prototype = te.getAsJsonPrimitive().getAsString();
                                }
                                if (prototype == null) continue;
                                MobPrototype mP = mobKeySet.get(prototype);
                                if (mP == null) continue;

                                int weight = 10;
                                if (to.has("weight") && (te = to.get("weight")).isJsonPrimitive()) {
                                    JsonPrimitive tp = te.getAsJsonPrimitive();
                                    if (tp.isNumber()) weight = tp.getAsNumber().intValue();
                                }
                                if (weight < 1) continue;

                                mobs[index++] = new MobContainer(mP, weight);
                            }
                        }
                        mobs = Arrays.copyOf(mobs, index);
                    }
                    if (mobs == null || mobs.length == 0) continue;

                    GeometryRegion[] excludes = null;
                    if (ro.has("exclude") && (te = ro.get("exclude")).isJsonArray())
                        excludes = parseRegion(te.getAsJsonArray());
                    if (excludes == null) excludes = new GeometryRegion[0];

                    String type = "surface";
                    if (ro.has("type") && (te = ro.get("type")).isJsonPrimitive()) type = te.getAsString();
                    LocationProvider locProv = Registers.getLocProv(type);
                    if (locProv == null) continue;

                    double[] locProvParams = null;
                    if (ro.has("typeParam") && (te = ro.get("typeParam")).isJsonArray()) {
                        JsonArray ja = te.getAsJsonArray();
                        locProvParams = new double[ja.size()];
                        JsonPrimitive ep;
                        int index = 0;
                        for (JsonElement ee : ja) {
                            if (ee.isJsonPrimitive() && (ep = ee.getAsJsonPrimitive()).isNumber()) {
                                locProvParams[index++] = ep.getAsDouble();
                            }
                        }
                        locProvParams = Arrays.copyOf(locProvParams, index);
                    }
                    if (locProvParams == null) locProvParams = new double[0];

                    int density = 10;
                    if (ro.has("density") && (te = ro.get("density")).isJsonPrimitive()) {
                        JsonPrimitive tp = te.getAsJsonPrimitive();
                        if (tp.isNumber()) density = tp.getAsInt();
                    }

                    list.add(new MobRegionSpawn(e.getKey(), worldCheck, locProv, locProvParams, regions, excludes, mobs, density));
                }
            }
            return list;
        }
        return null;
    }

    private static List<MobRegionDespawn> loadDespawnRegions(JsonElement je) {
        if (je != null && je.isJsonObject() && je.getAsJsonObject().has("despawn")) {
            je = je.getAsJsonObject().get("despawn");
        } else return Collections.emptyList();
        if (je != null && je.isJsonObject()) {
            List<MobRegionDespawn> list = new LinkedList<>();
            for (Map.Entry<String, JsonElement> e : je.getAsJsonObject().entrySet()) {
                JsonElement te = e.getValue();
                if (te.isJsonObject()) {
                    JsonObject to = te.getAsJsonObject();

                    MobRegionSpawn.WorldCheck worldCheck = null;
                    if (to.has("world") && (te = to.get("world")).isJsonPrimitive()) {
                        JsonPrimitive tp = te.getAsJsonPrimitive();
                        if (tp.isNumber()) worldCheck = new MobRegionSpawn.WorldCheck(tp.getAsInt());
                        else if (tp.isString()) worldCheck = new MobRegionSpawn.WorldCheck(tp.getAsString());
                    }
                    if (worldCheck == null) continue;

                    GeometryRegion[] regions = null, excludes = null;
                    if (to.has("region") && (te = to.get("region")).isJsonArray())
                        regions = parseRegion(te.getAsJsonArray());
                    if (regions == null) continue;

                    if (to.has("exclude") && (te = to.get("exclude")).isJsonArray())
                        excludes = parseRegion(te.getAsJsonArray());
                    if (excludes == null) excludes = new GeometryRegion[0];

                    String[] mobs = defDespawnMobs;
                    if (to.has("mob") && (te = to.get("mob")).isJsonArray()) {
                        JsonArray ta = te.getAsJsonArray();
                        if (ta.size() != 0) {
                            if (strWorkerAry == null) strWorkerAry = new String[ta.size() * 2];
                            else if (strWorkerAry.length < ta.size()) {
                                Arrays.fill(strWorkerAry, null);
                                strWorkerAry = new String[ta.size() * 2];
                            }
                            int count = 0;
                            for (JsonElement me : ta) {
                                if (me.isJsonPrimitive()) strWorkerAry[count++] = me.getAsString();
                            }
                            if (count > 0) {
                                mobs = new String[count];
                                System.arraycopy(strWorkerAry, 0, mobs, 0, count);
                            }
                        }
                    }
                    list.add(new MobRegionDespawn(e.getKey(), worldCheck, regions, excludes, mobs));
                }
            }
            return list;
        }
        return Collections.emptyList();
    }

    private static Map<String,MobPrototype> loadMobs(File file) {
        JsonElement je;
        try {
            je = parse(new File(file, "mobs.json"));
        } catch (IOException e) {
            ModMain.logger.warn("Can't parse mobs.json", e);
            return null;
        }
        if (je.isJsonObject()) {
            JsonObject jo = je.getAsJsonObject();
            Map<String, MobPrototype> map = new HashMap<>(jo.size());
            for (Map.Entry<String, JsonElement> e : jo.entrySet()) {
                JsonElement te = e.getValue();
                if (te.isJsonObject()) {
                    JsonObject mo = te.getAsJsonObject();
                    if (mo.has("exec") && mo.has("params")) {
                        String exec = mo.getAsJsonPrimitive("exec").getAsString();
                        JsonArray pa = mo.getAsJsonArray("params");
                        String[] pAry = new String[pa.size()];
                        for (int i = 0; i < pa.size(); i++) {
                            pAry[i] = pa.get(i).getAsString();
                        }
                        SpawnParamSet paramSet = new SpawnParamSet(pAry);
                        MobProvider provider = Registers.getMobProv(exec);
                        if (provider != null) {
                            map.put(e.getKey(), new MobPrototype(e.getKey(), provider, paramSet));
                        }
                    }
                }
            }
            return map;
        }
        return null;
    }

    private static GeometryRegion[] parseRegion(JsonArray ja) {
        if (ja.size() == 0) return emptyRegionAry;
        if (workerAry == null) workerAry = new GeometryRegion[ja.size() * 2];
        else if (workerAry.length < ja.size()) {
            Arrays.fill(workerAry, null);
            workerAry = new GeometryRegion[ja.size() * 2];
        }
        int count = 0;
        for (JsonElement je : ja) {
            if (je.isJsonArray()) {
                JsonArray ra = je.getAsJsonArray();
                JsonElement idE = ra.get(0);
                if (idE.isJsonPrimitive() && idE.getAsJsonPrimitive().isNumber()) {
                    int id = idE.getAsInt();
                    boolean v = true;
                    double[] ary = new double[ra.size() - 1];
                    for (int i = 1; i < ra.size(); i++) {
                        JsonElement te = ra.get(i);
                        if (te.isJsonPrimitive() && te.getAsJsonPrimitive().isNumber()) {
                            ary[i - 1] = te.getAsDouble();
                        } else v = false;
                    }
                    if (!v) continue;
                    workerAry[count] = Registers.generateRegion(id, ary);
                    if (workerAry[count] != null) count++;
                }
            }
        }
        if (count == 0) return emptyRegionAry;
        GeometryRegion[] tar = new GeometryRegion[count];
        System.arraycopy(workerAry, 0, tar, 0, count);
        return tar;
    }

    private static JsonElement parse(File file) throws IOException {
        Reader reader = new InputStreamReader(new BufferedInputStream(new FileInputStream(file)), StandardCharsets.UTF_8);
        JsonElement je = new JsonParser().parse(reader);
        reader.close();
        return je;
    }
}
