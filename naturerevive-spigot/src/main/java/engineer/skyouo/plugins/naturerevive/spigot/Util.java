package engineer.skyouo.plugins.naturerevive.spigot;

import engineer.skyouo.plugins.naturerevive.common.INMSWrapper;
import engineer.skyouo.plugins.naturerevive.common.VersionUtil;

public class Util {
    private static String nmsWrapperPrefix = "engineer.skyouo.plugins.naturerevive.spigot.nms.";

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
                        3 > versions[2] && versions[2] > 0 ?
                        getClassAndInit(nmsWrapperPrefix + "NMSHandler1_19_1") :
                        getClassAndInit(nmsWrapperPrefix + "NMSHandler1_19")
                );
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
