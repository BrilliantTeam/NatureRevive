package engineer.skyouo.plugins.naturerevive.spigot;

import engineer.skyouo.plugins.naturerevive.common.INMSWrapper;
import engineer.skyouo.plugins.naturerevive.common.VersionUtil;

public class Util {
    private final static String nmsWrapperPrefix = "engineer.skyouo.plugins.naturerevive.spigot.nms.";

    public static INMSWrapper getNMSWrapper() {
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
                return (INMSWrapper) getClassAndInit(nmsWrapperPrefix + "NMSHandler1_20_1");
        }

        return null;
    }

    public static boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static Object getClassAndInit(String name) {
        try {
             return Class.forName(name).getConstructor().newInstance();
        } catch (Exception e) {
            return null;
        }
    }
}
