/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.foundation.creator

import com.demonwav.mcdev.creator.ProjectConfig
import com.demonwav.mcdev.creator.ProjectCreator
import com.demonwav.mcdev.creator.buildsystem.BuildSystemType
import com.demonwav.mcdev.creator.buildsystem.maven.MavenBuildSystem
import com.demonwav.mcdev.creator.buildsystem.maven.MavenCreator
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.foundation.*
import com.demonwav.mcdev.platform.versions.FoundationVersion
import com.demonwav.mcdev.platform.versions.MavenPlatformVersionsRegistry
import com.demonwav.mcdev.util.JavaVersions
import com.demonwav.mcdev.util.MinecraftVersions
import com.demonwav.mcdev.util.SemanticVersion
import com.intellij.openapi.module.Module
import com.intellij.util.lang.JavaVersion
import java.nio.file.Path

class FoundationProjectConfig(override var type: PlatformType) :
    ProjectConfig(), FoundationLikeConfiguration, MavenCreator {

    override lateinit var mainClass: String
    var useNms = false
    var useAnt = false;
    var useManualSpigotVersion = false
    var minecraftVersion: String = ""
    var comboBoxJavaVersion = ""
    var serverPath: String = ""
    val semanticMinecraftVersion: SemanticVersion
        get() = if (minecraftVersion.isBlank()) SemanticVersion.release() else SemanticVersion.parse(minecraftVersion)
    val customJavaVersion: String
        get() = JavaVersions.requiredJavaVersion(comboBoxJavaVersion)

    val getMavenVersions by lazy {
        val mavenPlatformVersionsRegistry = MavenPlatformVersionsRegistry
        mavenPlatformVersionsRegistry.addVersion(FoundationVersion())
        mavenPlatformVersionsRegistry.loadMavenPlatformVersions()
    }

    override val dependencies = mutableListOf<String>()
    override fun hasDependencies() = listContainsAtLeastOne(dependencies)
    override fun setDependencies(string: String) {
        dependencies.clear()
        dependencies.addAll(commaSplit(string))
    }

    override val softDependencies = mutableListOf<String>()
    override fun hasSoftDependencies() = listContainsAtLeastOne(softDependencies)
    override fun setSoftDependencies(string: String) {
        softDependencies.clear()
        softDependencies.addAll(commaSplit(string))
    }

    override val preferredBuildSystem = BuildSystemType.MAVEN

    override val javaVersion: JavaVersion
        get() = MinecraftVersions.requiredJavaVersion(semanticMinecraftVersion)

    override fun buildMavenCreator(
        rootDirectory: Path,
        module: Module,
        buildSystem: MavenBuildSystem
    ): ProjectCreator {
        return BukkitMavenCreator(rootDirectory, module, buildSystem, this)
    }
}
