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

class FoundationVersion  : MavenPlatformVersion() {

    override fun getMavenPlatformVersion(): String {
        return Gson().fromJson(
            getReleaseVersionOrDefault("kangarko", "foundation", "releases/latest"),
            MavenVersionName::class.java
        )?.name ?: MinecraftSettings.instance.foundationVersion
    }

    override fun updateMavenPlatformVersion(version: String): String {
        if (MinecraftSettings.instance.foundationVersion !== version) {
            MinecraftSettings.instance.foundationVersion = version;
        }
        return MinecraftSettings.instance.foundationVersion
    }

    override fun getEntryMap(): String {
        return "foundationVersion"
    }
}