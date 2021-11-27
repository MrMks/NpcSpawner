package moe.gensoukyo.npcspawner.mob;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.data.INPCStats;
import noppes.npcs.entity.EntityCustomNpc;

public class CustomNpcsProvider implements MobProvider {

    @Override
    public String getRegisterKey() {
        return "cnpc";
    }

    @Override
    public boolean testDependency() {
        try {
            Class.forName("noppes.npcs.api.NpcAPI");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    @Override
    public boolean spawn(World world, Vec3d vec3d, SpawnParamSet paramSet) {
        if (NpcAPI.IsAvailable() && world instanceof WorldServer) {
            NpcAPI api = NpcAPI.Instance();
            IEntity<?> entity = api.getClones().spawn(
                    vec3d.x,
                    vec3d.y,
                    vec3d.z,
                    paramSet.getInt(0),
                    paramSet.getString(1),
                    api.getIWorld((WorldServer) world)
            );
            if (entity instanceof ICustomNpc<?>) {
                INPCStats stats = ((ICustomNpc<?>)entity).getStats();
                if (stats.getRespawnType() < 3) stats.setRespawnType(4);
                return true;
            }
            if (paramSet.size() > 2) {
                entity.setName(paramSet.getString(2));
            }
        }
        return false;
    }

    @Override
    public int countIn(World world, Vec3d p1, Vec3d p2) {
        return world.getEntitiesWithinAABB(EntityCustomNpc.class, new AxisAlignedBB(p1, p2)).size();
    }
}
