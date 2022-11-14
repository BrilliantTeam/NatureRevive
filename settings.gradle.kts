rootProject.name = "NatureReviveRewrite"

pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}

include("naturerevive-common")

listOf("1_19_1").forEach {
    include(":naturerevive-spigot:nms:nms-$it")
}

include("naturerevive-spigot")
include("naturerevive-spigot:nms:nms-1_19")
findProject(":naturerevive-spigot:nms:nms-1_19")?.name = "nms-1_19"
