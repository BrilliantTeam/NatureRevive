package engineer.skyouo.plugins.naturerevive.spigot;

import engineer.skyouo.plugins.naturerevive.common.INMSWrapper;
import engineer.skyouo.plugins.naturerevive.common.VersionUtil;
import org.bukkit.Bukkit;

public class Util {
    private final static String nmsWrapperPrefix = "engineer.skyouo.plugins.naturerevive.spigot.nms.";

    public static INMSWrapper getNMSWrapper() {
        INMSWrapper wrapper = getNMSWrapperInternal();

        if (wrapper == null) {
            NatureReviveBukkitLogger.warning("由於 NatureRevive 尚未對該 Paper 版本: " + Bukkit.getBukkitVersion() + " 原生支援，");
            NatureReviveBukkitLogger.warning("將嘗試使用兼容層 NMSHandlerCompat，該模式下，有可能出現錯誤或性能下降。");

            return (INMSWrapper) getClassAndInit(nmsWrapperPrefix + "NMSHandlerCompat");
        } else {
            return wrapper;
        }
    }

    public static INMSWrapper getNMSWrapperInternal() {
        int[] versions = VersionUtil.getVersion();

        switch (versions[1]) {
            case 17:
                return (INMSWrapper) getClassAndInit(nmsWrapperPrefix + "NMSHandler1_17");
            case 18:
                return (INMSWrapper) (
                        versions[2] == 2 ?
                                getClassAndInit(nmsWrapperPrefix + "NMSHandler1_18_2") :
                                getClassAndInit(nmsWrapperPrefix + "NMSHandler1_18")
                );
            case 19:
                return (INMSWrapper) (
                        versions[2] == 0 ?
                        getClassAndInit(nmsWrapperPrefix + "NMSHandler1_19") :
                        versions[2] == 1 || versions[2] == 2 ?
                        getClassAndInit(nmsWrapperPrefix + "NMSHandler1_19_1") :
                        versions[2] == 3 ?
                        getClassAndInit(nmsWrapperPrefix + "NMSHandler1_19_3") :
                        getClassAndInit(nmsWrapperPrefix + "NMSHandler1_19_4")
                );
            case 20:
                return versions[2] == 2 ?
                    (INMSWrapper) getClassAndInit(nmsWrapperPrefix + "NMSHandler1_20_2") :
                    versions[2] < 2 ? (INMSWrapper) getClassAndInit(nmsWrapperPrefix + "NMSHandler1_20_1") :
                    null;
        }

        return null;
    }

    private static Object getClassAndInit(String name) {
        try {
             return Class.forName(name).getConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
