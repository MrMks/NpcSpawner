package moe.gensoukyo.npcspawner;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * @author SQwatermark
 */
public class CommandNpcSpawner extends CommandBase {

    private final SpawnerConfig config;
    private final SpawnerStatus.WritePort status;
    public CommandNpcSpawner(SpawnerConfig config, SpawnerStatus.WritePort status) {
        this.config = config;
        this.status = status;
    }

    @NotNull
    @Override
    public String getName() {
        return "npcspawner";
    }

    @NotNull
    @Override
    public String getUsage(@NotNull ICommandSender sender) {
        return "重载NPC配置文件：/npcspawner reload\n" +
                "暂停/恢复刷怪: /npcspawner pause [true|false|1|0]\n" +
                "开/关调试状态: /npcspawner debug [true|false|1|0]\n" +
                "临时世界黑名单: /npcspawner blacklist [on|off|add|rm|clr|ls] [world]";
    }

    private String playerStr(String msg) {
        return "[npcspawner]" + msg;
    }

    private String consoleStr(String msg, ICommandSender sender) {
        return "[@" + sender.getName() + "]" + msg;
    }

    @Override
    public void execute(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args) {
        if (args.length > 0) {
            boolean isServer = sender instanceof MinecraftServer;
            Logger logger = ModMain.logger;
            if ("reload".equalsIgnoreCase(args[0])) {
                sender.sendMessage(new TextComponentString(playerStr("刷新配置中...")));
                config.reload();
                String senderName = sender.getName();
                if (!isServer) ModMain.logger.info("[@" + senderName + "]配置刷新");
                sender.sendMessage(new TextComponentString(playerStr("配置刷新完成")));
            } else if ("pause".equalsIgnoreCase(args[0])) {
                boolean changed = false;
                if (args.length > 1) {
                    String s = args[1];
                    if (s.equalsIgnoreCase("true") || s.equals("1")) changed = status.enablePause();
                    else changed = status.disablePause();
                }
                String msg = "现在的刷怪器状态: " + (status.isPause() ? "§c暂停" : "§a运行");
                sender.sendMessage(new TextComponentString(playerStr(msg)));
                if (changed && !isServer) logger.info(consoleStr(msg, sender));
            } else if ("debug".equalsIgnoreCase(args[0])) {
                boolean changed = false;
                if (args.length > 1) {
                    String s = args[1];
                    if (s.equalsIgnoreCase("true") || s.equals("1")) changed = status.enableDebug();
                    else changed = status.disableDebug();
                }
                String msg = "现在的调试状态: " + (status.isDebug() ? "§c开" : "§a关");
                sender.sendMessage(new TextComponentString(playerStr(msg)));
                if (changed && !isServer) logger.info(consoleStr(msg, sender));
            } else if ("blacklist".equalsIgnoreCase(args[0])) {
                if (args.length > 1) {
                    if ("on".equalsIgnoreCase(args[1])) {
                        String msg = "刷怪世界黑名单: §con";
                        sender.sendMessage(new TextComponentString(playerStr(msg)));
                        if (status.enableBlacklist() && !isServer) logger.info(consoleStr(msg,sender));
                    } else if ("off".equalsIgnoreCase(args[1])) {
                        String msg = "刷怪世界黑名单: §aoff";
                        sender.sendMessage(new TextComponentString(playerStr(msg)));
                        if (status.disableBlacklist() && !isServer) logger.info(consoleStr(msg, sender));
                    } else if ("clr".equalsIgnoreCase(args[1])) {
                        String msg = "刷怪世界黑名单: 名单已重置";
                        sender.sendMessage(new TextComponentString(playerStr(msg)));
                        if (status.clearBlacklist() && !isServer) logger.info(consoleStr(msg, sender));
                    } else if ("ls".equalsIgnoreCase(args[1])) {
                        String[] list = status.getBlacklists();
                        if (list.length == 0) {
                            sender.sendMessage(new TextComponentString("黑名单为空"));
                        } else {
                            sender.sendMessage(new TextComponentString(Arrays.toString(list)));
                        }
                    } else if ("add".equalsIgnoreCase(args[1])) {
                        if (args.length > 2) {
                            if (args[2] != null) {
                                String msg = "刷怪世界黑名单已§a添加: " + args[2];
                                sender.sendMessage(new TextComponentString(playerStr(msg)));
                                if (status.addBlacklist(args[2]) && !isServer) logger.info(consoleStr(msg, sender));
                            }
                        }
                    } else if ("rm".equalsIgnoreCase(args[1])) {
                        if (args.length > 2) {
                            if (args[2] != null) {
                                String msg = "刷怪世界黑名单已§c移除: " + args[2];
                                sender.sendMessage(new TextComponentString(playerStr(msg)));
                                if (status.removeBlacklist(args[2]) && !isServer) logger.info(consoleStr(msg, sender));
                            }
                        }
                    }
                }
                else {
                    sender.sendMessage(new TextComponentString("刷怪世界黑名单: " + (status.isBlacklist() ? "§con" : "§aoff")));
                }
            }
        }
    }

    @NotNull
    @Override
    public List<String> getTabCompletions(@NotNull MinecraftServer server, @NotNull ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "reload", "pause", "debug", "blacklist");
        } else if (args.length == 2) {
            if ("pause".equals(args[0]) || "debug".equalsIgnoreCase(args[0])) return getListOfStringsMatchingLastWord(args, "0", "1", "false", "true");
            else if ("blacklist".equalsIgnoreCase(args[0])) return getListOfStringsMatchingLastWord(args, "on", "off", "add", "rm", "clr", "ls");
        }
        return super.getTabCompletions(server, sender, args, targetPos);
    }
}
