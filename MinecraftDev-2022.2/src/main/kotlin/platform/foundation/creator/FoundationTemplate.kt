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
import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.creator.buildsystem.BuildSystemType
import com.demonwav.mcdev.platform.BaseTemplate
import com.demonwav.mcdev.platform.foundation.BukkitModuleType
import com.demonwav.mcdev.platform.foundation.FoundationLikeConfiguration
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.FOUNDATION_ANT_BUILD_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.FOUNDATION_MAIN_CLASS_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.FOUNDATION_PLUGIN_YML_TEMPLATE
import com.demonwav.mcdev.util.MinecraftTemplates.Companion.FOUNDATION_POM_TEMPLATE
import com.intellij.openapi.project.Project

object FoundationTemplate : BaseTemplate() {

    fun applyMainClass(
        project: Project,
        packageName: String,
        className: String
    ): String {
        val props = mapOf(
            "PACKAGE" to packageName,
            "CLASS_NAME" to className
        )

        return project.applyTemplate(FOUNDATION_MAIN_CLASS_TEMPLATE, props)
    }

    fun applyPom(project: Project, config: FoundationProjectConfig): String {
        val props = mutableMapOf(
            "javaVersion" to config.customJavaVersion,
            "mainClass" to config.mainClass,
        )
        props.putAll(config.getMavenVersions)

        val pluginRepo = """ 
            <pluginRepositories>
                <pluginRepository>
                    <id>maven-snapshots</id>
                    <url>https://repository.apache.org/content/repositories/snapshots/</url>
                </pluginRepository>
            </pluginRepositories>
        """.trimIndent()

        if (config.hasAuthors()) {
            props["author"] = config.authors[0]
        } else {
            props["author"] = System.getProperty("user.name")
        }

        if (config.customJavaVersion == "17") {
            props["pluginRepo"] = pluginRepo
        }

        return project.applyTemplate(FOUNDATION_POM_TEMPLATE, props)
    }

    fun applyAntBuild(
        project: Project,
        config: FoundationProjectConfig,
    ): String {
        val props = mutableMapOf<String, String>()
        props["name"] = config.pluginName
        props["serverPath"] = config.serverPath

        return project.applyTemplate(FOUNDATION_ANT_BUILD_TEMPLATE, props)
    }

    fun applyPluginYml(
        project: Project,
        config: FoundationProjectConfig,
        buildSystem: BuildSystem
    ): String {
        fun bukkitDeps(props: MutableMap<String, String>, configuration: FoundationLikeConfiguration) {
            if (configuration.hasDependencies()) {
                props["DEPEND"] = configuration.dependencies.toString()
            }
            if (configuration.hasSoftDependencies()) {
                props["SOFT_DEPEND"] = configuration.softDependencies.toString()
            }
        }

        val props = foundationMain(buildSystem.type, config)

        bukkitDeps(props, config)

        if (config.hasAuthors()) {
            props["AUTHOR_LIST"] = config.authors.toString()
        }

        if (config.hasDescription()) {
            props["DESCRIPTION"] = config.description
                ?: throw IllegalStateException("description is null when not blank")
        }

        if (config.hasWebsite()) {
            props["WEBSITE"] = config.website ?: throw IllegalStateException("website is null when not blank")
        }

        // Plugins targeting 1.13 or newer need an explicit api declaration flag
        // This is the major and minor version separated by a dot without the patch version. ex: 1.15 even for 1.15.2
        val mcVersion = config.semanticMinecraftVersion
        if (mcVersion >= BukkitModuleType.API_TAG_VERSION) {
            props["API_VERSION"] = mcVersion.take(2).toString()
        }

        return project.applyTemplate(FOUNDATION_PLUGIN_YML_TEMPLATE, props)
    }

    fun <C> foundationMain(type: BuildSystemType, config: C): MutableMap<String, String>
        where C : ProjectConfig,
              C : FoundationLikeConfiguration {
        val version = when (type) {
            BuildSystemType.GRADLE -> "\${version}"
            BuildSystemType.MAVEN -> "\${project.version}"
        }

        return mutableMapOf(
            "MAIN" to config.mainClass,
            "VERSION" to version,
            "NAME" to config.pluginName
        )
    }
}
