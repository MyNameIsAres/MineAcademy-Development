/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.versions

import com.demonwav.mcdev.MinecraftSettings
import com.google.gson.Gson

class MavenJarPluginVersion : MavenPlatformVersion() {

    override fun getMavenPlatformVersion(): String {
        return Gson().fromJson(
            getReleaseVersionOrDefault("apache", "maven-jar-plugin", "releases/latest"),
            MavenVersionName::class.java
        )?.name ?: MinecraftSettings.instance.mavenJarVersion
    }

    override fun updateMavenPlatformVersion(version: String): String {
        if (MinecraftSettings.instance.mavenJarVersion !== version) {
            MinecraftSettings.instance.mavenJarVersion = version;
        }
        return MinecraftSettings.instance.mavenJarVersion
    }

    override fun getEntryMap(): String {
        return "mavenJarVersion"
    }
}