package moe.gensoukyo.npcspawner;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.server.permission.PermissionAPI;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Stream;

public class SpawnLogicHandler {

    private final Random random = new Random();

    private final SpawnerConfig config;
    private final SpawnerStatus.ReadPort status;

    public SpawnLogicHandler(SpawnerConfig config, SpawnerStatus.ReadPort status) {
        this.config = config;
        this.status = status;
    }

    private int intervalLeft = 0;

    @SubscribeEvent
    public void worldTick(TickEvent.WorldTickEvent tickEvent) {
        if (config == null || !config.isCorrectLoaded()) return;
        if (tickEvent.world.isRemote || tickEvent.phase == TickEvent.Phase.END) return;
        WorldServer world = (WorldServer) tickEvent.world;
        if (!status.canSpawn(world)) return;
        if (intervalLeft <= 0) {
            intervalLeft = config.getIntervalMin() + random.nextInt(config.getIntervalBias() + 1);
            spawnMobs(world);
        } else --intervalLeft;
    }

    private void spawnMobs(WorldServer world) {
        if (world.playerEntities.size() == 0) return;

        Stream<EntityPlayer> playersStream = world.playerEntities.stream()
                .filter(entity->!PermissionAPI.hasPermission(entity, "npcspawner.noMobSpawn"))
                .filter(player -> ((EntityPlayerMP)player).interactionManager.survivalOrAdventure() || status.isDebug());
        LinkedList<EntityPlayer> players = playersStream.collect(LinkedList::new, LinkedList::add, LinkedList::addAll);

        int mobSize = players.size() / 4 + (random.nextInt(4) < players.size() % 4 ? 1 : 0);
        if (mobSize == 0) return;

        while (mobSize-- > 0) {
            EntityPlayer player = players.remove(random.nextInt(players.size()));
            Vec3d playerPos = player.getPositionVector();
            Optional<MobRegionSpawn> rgSpawn = config.spawnStream().filter(rg -> rg.isVecIn(world, playerPos) && !rg.isMobsOverDensity(world, playerPos)).findAny();
            if (rgSpawn.isPresent() && config.despawnStream().noneMatch(rg -> rg.isVecIn(world, playerPos))) {
                MobRegionSpawn region = rgSpawn.get();
                int distance = config.getDistanceMin() + random.nextInt(config.getDistanceBias() + 1);
                double[] cos_sin = MathUtils.unsafe_cos_sin(random.nextInt(360));
                Vec3d target = region.getLocProv().findLocationToSpawn(world, playerPos, distance * cos_sin[0], distance * cos_sin[1], region.getLocProvParams());
                if (target != null && playersStream.noneMatch(o -> entityNearLoc(target, o.getPositionVector(), config.getDistanceMin(), config.getSquareDistance()))) {
                    MobPrototype mobPrototype = region.nextMob(random.nextDouble());
                    if (!mobPrototype.getProvider().spawn(world, target, mobPrototype.getParams())) {
                        ModMain.logger.warn("Can't spawn mob {{}}", mobPrototype);
                    }
                }
            }
        }
    }

    private static boolean entityNearLoc(Vec3d o1, Vec3d o2, int dis, int disSqr) {
        if (o1.x - o2.x > dis || o1.y - o2.y > dis || o1.z - o2.z > dis) return false;
        else if (Math.abs(o1.x + o1.y + o1.z - o2.x - o2.y - o2.z) < dis) return true;
        else return o1.squareDistanceTo(o2) < disSqr;
    }
}
