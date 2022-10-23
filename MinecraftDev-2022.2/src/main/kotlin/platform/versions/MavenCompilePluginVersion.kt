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

class MavenCompilePluginVersion  : MavenPlatformVersion() {

    override fun getMavenPlatformVersion(): String {
        return Gson().fromJson(
            getReleaseVersionOrDefault("apache", "maven-compiler-plugin", "releases/latest"),
            MavenVersionName::class.java
        )?.name ?: MinecraftSettings.instance.mavenCompilerVersion
    }

    override fun updateMavenPlatformVersion(version: String): String {
        if (MinecraftSettings.instance.mavenCompilerVersion !== version) {
            MinecraftSettings.instance.mavenCompilerVersion = version;
        }
        return MinecraftSettings.instance.mavenCompilerVersion
    }

    override fun getEntryMap(): String {
        return "mavenCompilerVersion"
    }
}