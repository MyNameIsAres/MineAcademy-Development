/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.util

import com.demonwav.mcdev.asset.PlatformAssets
import com.intellij.ide.fileTemplates.FileTemplateDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory

class MinecraftTemplates : FileTemplateGroupDescriptorFactory {

    override fun getFileTemplatesDescriptor(): FileTemplateGroupDescriptor {
        val group = FileTemplateGroupDescriptor("Minecraft", PlatformAssets.MINECRAFT_ICON)

        FileTemplateGroupDescriptor("Foundation", PlatformAssets.FOUNDATION_ICON).let { foundationGroup ->
            group.addTemplate(foundationGroup)
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_MAIN_CLASS_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_PLUGIN_YML_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_BUILD_GRADLE_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_SUBMODULE_BUILD_GRADLE_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_GRADLE_PROPERTIES_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_SETTINGS_GRADLE_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_POM_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_SUBMODULE_POM_TEMPLATE))
            foundationGroup.addTemplate(FileTemplateDescriptor(FOUNDATION_ANT_BUILD_TEMPLATE))
        }

        FileTemplateGroupDescriptor("Bukkit", PlatformAssets.BUKKIT_ICON).let { bukkitGroup ->
            group.addTemplate(bukkitGroup)
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_MAIN_CLASS_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_PLUGIN_YML_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_BUILD_GRADLE_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_SUBMODULE_BUILD_GRADLE_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_GRADLE_PROPERTIES_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_SETTINGS_GRADLE_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_POM_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_SUBMODULE_POM_TEMPLATE))
            bukkitGroup.addTemplate(FileTemplateDescriptor(BUKKIT_ANT_BUILD_TEMPLATE))
        }


        FileTemplateGroupDescriptor("Multi-Module", PlatformAssets.MINECRAFT_ICON).let { multiGroup ->
            group.addTemplate(multiGroup)
            multiGroup.addTemplate(FileTemplateDescriptor(MULTI_MODULE_BUILD_GRADLE_TEMPLATE))
            multiGroup.addTemplate(FileTemplateDescriptor(MULTI_MODULE_GRADLE_PROPERTIES_TEMPLATE))
            multiGroup.addTemplate(FileTemplateDescriptor(MULTI_MODULE_SETTINGS_GRADLE_TEMPLATE))
            multiGroup.addTemplate(FileTemplateDescriptor(MULTI_MODULE_POM_TEMPLATE))
            multiGroup.addTemplate(FileTemplateDescriptor(MULTI_MODULE_COMMON_POM_TEMPLATE))
        }

        FileTemplateGroupDescriptor("Common", PlatformAssets.MINECRAFT_ICON).let { commonGroup ->
            group.addTemplate(commonGroup)
            commonGroup.addTemplate(FileTemplateDescriptor(GRADLE_GITIGNORE_TEMPLATE))
            commonGroup.addTemplate(FileTemplateDescriptor(MAVEN_GITIGNORE_TEMPLATE))
        }



        FileTemplateGroupDescriptor("Licenses", null).let { licenseGroup ->
            group.addTemplate(licenseGroup)
            enumValues<License>().forEach { license ->
                licenseGroup.addTemplate(FileTemplateDescriptor(license.id))
            }
        }

        return group
    }

    companion object {
        const val FOUNDATION_MAIN_CLASS_TEMPLATE = "Foundation Main Class.java"
        const val FOUNDATION_PLUGIN_YML_TEMPLATE = "Foundation plugin.yml"
        const val FOUNDATION_BUILD_GRADLE_TEMPLATE = "Foundation build.gradle"
        const val FOUNDATION_SUBMODULE_BUILD_GRADLE_TEMPLATE = "Foundation Submodule build.gradle"
        const val FOUNDATION_GRADLE_PROPERTIES_TEMPLATE = "Foundation gradle.properties"
        const val FOUNDATION_SETTINGS_GRADLE_TEMPLATE = "Foundation settings.gradle"
        const val FOUNDATION_POM_TEMPLATE = "Foundation pom.xml"
        const val FOUNDATION_SUBMODULE_POM_TEMPLATE = "Foundation Submodule pom.xml"
        const val FOUNDATION_ANT_BUILD_TEMPLATE = "Foundation new_ant.build"

        const val BUKKIT_MAIN_CLASS_TEMPLATE = "Bukkit Main Class.java"
        const val BUKKIT_PLUGIN_YML_TEMPLATE = "Bukkit plugin.yml"
        const val BUKKIT_BUILD_GRADLE_TEMPLATE = "Bukkit build.gradle"
        const val BUKKIT_SUBMODULE_BUILD_GRADLE_TEMPLATE = "Bukkit Submodule build.gradle"
        const val BUKKIT_GRADLE_PROPERTIES_TEMPLATE = "Bukkit gradle.properties"
        const val BUKKIT_SETTINGS_GRADLE_TEMPLATE = "Bukkit settings.gradle"
        const val BUKKIT_POM_TEMPLATE = "Bukkit pom.xml"
        const val BUKKIT_SUBMODULE_POM_TEMPLATE = "Bukkit Submodule pom.xml"
        const val BUKKIT_ANT_BUILD_TEMPLATE = "Bukkit ant.build"


        const val MULTI_MODULE_BUILD_GRADLE_TEMPLATE = "Multi-Module Base build.gradle"
        const val MULTI_MODULE_GRADLE_PROPERTIES_TEMPLATE = "Multi-Module Base gradle.properties"
        const val MULTI_MODULE_SETTINGS_GRADLE_TEMPLATE = "Multi-Module Base settings.gradle"
        const val MULTI_MODULE_POM_TEMPLATE = "Multi-Module Base pom.xml"
        const val MULTI_MODULE_COMMON_POM_TEMPLATE = "Multi-Module Common pom.xml"

        const val GRADLE_GITIGNORE_TEMPLATE = "Gradle.gitignore"
        const val MAVEN_GITIGNORE_TEMPLATE = "Maven.gitignore"


    }

    private fun template(fileName: String, displayName: String? = null) = CustomDescriptor(fileName, displayName)

    private class CustomDescriptor(fileName: String, val visibleName: String?) : FileTemplateDescriptor(fileName) {
        override fun getDisplayName(): String = visibleName ?: fileName
    }
}
