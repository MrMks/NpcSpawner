package moe.gensoukyo.npcspawner;

import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.world.WorldServer;

import java.util.HashSet;
import java.util.Set;

public class SpawnerStatus {
    private boolean pause = false;
    private boolean blackEnable = false;
    private boolean debug = false;
    private final IntSet blackDimList = new IntArraySet();
    private final Set<String> blackNameList = new HashSet<>();

    public WritePort newWritePort() {
        return new WritePort();
    }

    public ReadPort newReadPort() {
        return new ReadPort();
    }

    public class WritePort {
        public boolean isPause() {
            return pause;
        }

        public boolean togglePause() {
            return (pause = !pause);
        }

        public boolean enablePause() {
            return !pause && togglePause();
        }

        public boolean disablePause() {
            return pause && !togglePause();
        }

        public boolean isDebug() {
            return debug;
        }

        public boolean toggleDebug() {
            return (debug = !debug);
        }

        public boolean enableDebug() {
            return !debug && toggleDebug();
        }

        public boolean disableDebug() {
            return debug && !toggleDebug();
        }

        public boolean isBlacklist() {
            return blackEnable;
        }

        public boolean toggleBlacklist() {
            return (blackEnable = !blackEnable);
        }

        public boolean enableBlacklist() {
            return !blackEnable && toggleBlacklist();
        }

        public boolean disableBlacklist() {
            return blackEnable && !toggleBlacklist();
        }

        public boolean addBlacklist(String str) {
            try {
                int i = Integer.parseInt(str);
                return blackDimList.add(i);
            } catch(NumberFormatException e) {
                return blackNameList.add(str);
            }
        }

        public boolean removeBlacklist(String str) {
            try {
                int i = Integer.parseInt(str);
                return blackDimList.remove(i);
            } catch (NumberFormatException e) {
                return blackNameList.remove(str);
            }
        }

        public boolean clearBlacklist() {
            if (blackDimList.isEmpty() && blackNameList.isEmpty()) return false;
            blackDimList.clear();
            blackNameList.clear();
            return true;
        }

        public String[] getBlacklists() {
            String[] ary = new String[blackDimList.size() + blackNameList.size()];
            int i = 0;
            for (int dim : blackDimList) ary[i++] = Integer.toString(dim);
            for (String name : blackNameList) ary[i++] = name;
            return ary;
        }
    }

    public class ReadPort {
        public boolean canSpawn(WorldServer world) {
            return !pause && (!blackEnable || !checkBlacklist(world));
        }

        private boolean checkBlacklist(WorldServer world) {
            int dim = world.provider.getDimension();
            for (int i : blackDimList) if (dim == i) return true;
            String name = world.getWorldInfo().getWorldName();
            for (String nm : blackNameList) if (name.equals(nm)) return true;
            return false;
        }

        public boolean isDebug() {
            return debug;
        }
    }
}
