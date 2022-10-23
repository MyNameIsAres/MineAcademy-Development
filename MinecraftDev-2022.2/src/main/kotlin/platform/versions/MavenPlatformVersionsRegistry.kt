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

object MavenPlatformVersionsRegistry {

    private val versions = mutableListOf<MavenPlatformVersion>()

    fun addVersion(version: MavenPlatformVersion) {
        versions.add(version)
    }

    init {
        versions.add(MavenJarPluginVersion())
        versions.add(MavenCompilePluginVersion())
        versions.add(MavenShadePluginVersion())
    }

    fun loadMavenPlatformVersions(): Map<String, String> {
        val mavenPlatformVersionsMap = mutableMapOf<String, String>()

        versions.forEach { version ->
            mavenPlatformVersionsMap[version.getEntryMap()] =
                version.updateMavenPlatformVersion(version.getMavenPlatformVersion())
        }

        return mavenPlatformVersionsMap
    }


}