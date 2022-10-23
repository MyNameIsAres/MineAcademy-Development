/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.creator

import com.demonwav.mcdev.creator.BaseProjectCreator
import com.demonwav.mcdev.creator.BasicJavaClassStep
import com.demonwav.mcdev.creator.CreateDirectoriesStep
import com.demonwav.mcdev.creator.CreatorStep
import com.demonwav.mcdev.creator.buildsystem.BuildDependency
import com.demonwav.mcdev.creator.buildsystem.BuildRepository
import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.creator.buildsystem.gradle.BasicGradleFinalizerStep
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleBuildSystem
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleFiles
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleGitignoreStep
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleSetupStep
import com.demonwav.mcdev.creator.buildsystem.gradle.GradleWrapperStep
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenFinalizerStep
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenStep
import com.demonwav.mcdev.creator.buildsystem.maven.MavenBuildSystem
import com.demonwav.mcdev.creator.buildsystem.maven.MavenGitignoreStep
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.util.invokeLater
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil
import com.intellij.execution.configurations.RunConfiguration
import com.intellij.execution.configurations.SimpleConfigurationType
import com.intellij.execution.jar.JarApplicationConfiguration
import com.intellij.icons.AllIcons
import com.intellij.lang.ant.config.AntConfiguration
import com.intellij.lang.ant.config.impl.AntBeforeRunTask
import com.intellij.lang.ant.config.impl.AntConfigurationImpl
import com.intellij.openapi.module.Module
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NotNullLazyValue
import java.nio.file.Path

sealed class BukkitProjectCreator<T : BuildSystem>(
    protected val rootDirectory: Path,
    protected val rootModule: Module,
    protected val buildSystem: T,
    protected val config: BukkitProjectConfig
) : BaseProjectCreator(rootModule, buildSystem) {

    protected fun setupMainClassStep(): BasicJavaClassStep {
        return createJavaClassStep(config.mainClass) { packageName, className ->
            BukkitTemplate.applyMainClass(project, packageName, className)
        }
    }

    protected fun setupDependencyStep(): BukkitDependenciesStep {
        val mcVersion = config.minecraftVersion
        return BukkitDependenciesStep(buildSystem, config.type, mcVersion, config.useNms, config.serverPath)
    }

    protected fun setupAntBuildStep(): AntBuildStep {
        return AntBuildStep(project, rootDirectory, config)
    }

    protected fun setupYmlStep(): PluginYmlStep {
        return PluginYmlStep(project, buildSystem, config)
    }
}

class BukkitMavenCreator(
    rootDirectory: Path,
    rootModule: Module,
    buildSystem: MavenBuildSystem,
    config: BukkitProjectConfig
) : BukkitProjectCreator<MavenBuildSystem>(rootDirectory, rootModule, buildSystem, config) {

    override fun getSteps(): Iterable<CreatorStep> {
        val pomText = BukkitTemplate.applyPom(project, config)
        return listOf(
            setupDependencyStep(),
            BasicMavenStep(project, rootDirectory, buildSystem, config, pomText),
            setupMainClassStep(),
            setupYmlStep(),
            setupAntBuildStep(),
            MavenGitignoreStep(project, rootDirectory),
            BasicMavenFinalizerStep(rootModule, rootDirectory)
        )
    }
}

class BukkitGradleCreator(
    rootDirectory: Path,
    rootModule: Module,
    buildSystem: GradleBuildSystem,
    config: BukkitProjectConfig
) : BukkitProjectCreator<GradleBuildSystem>(rootDirectory, rootModule, buildSystem, config) {

    override fun getSteps(): Iterable<CreatorStep> {
        val buildText = BukkitTemplate.applyBuildGradle(project, buildSystem, config)
        val propText = BukkitTemplate.applyGradleProp(project)
        val settingsText = BukkitTemplate.applySettingsGradle(project, buildSystem.artifactId)
        val files = GradleFiles(buildText, propText, settingsText)

        return listOf(
            setupDependencyStep(),
            CreateDirectoriesStep(buildSystem, rootDirectory),
            GradleSetupStep(project, rootDirectory, buildSystem, files),
            setupMainClassStep(),
            setupYmlStep(),
            GradleWrapperStep(project, rootDirectory, buildSystem),
            GradleGitignoreStep(project, rootDirectory),
            BasicGradleFinalizerStep(rootModule, rootDirectory, buildSystem)
        )
    }
}

open class BukkitDependenciesStep(
    protected val buildSystem: BuildSystem,
    protected val type: PlatformType,
    protected val mcVersion: String,
    protected val enableNms: Boolean,
    protected val serverPath: String
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        when (type) {

            PlatformType.SPIGOT -> {
                spigotRepo(buildSystem.repositories)
                buildSystem.dependencies.add(
                    BuildDependency(
                        "org.spigotmc",
                        "spigot-api",
                        "$mcVersion-R0.1-SNAPSHOT",
                    )
                )
                addSonatype(buildSystem.repositories)
            }
            else -> {}
        }

        if (enableNms) {
            buildSystem.dependencies.add(
                BuildDependency(
                    "paper-server",
                    "Paper-$mcVersion",
                    "1",
                    "system",
                    serverPath + if (mcVersion.replace(".", "").toInt() >= 1180) "/paperclip.jar" else "/cache/patched.jar"
                )
            )
        }

    }

    protected fun addSonatype(buildRepositories: MutableList<BuildRepository>) {
        buildRepositories.add(BuildRepository("sonatype", "https://oss.sonatype.org/content/groups/public/"))
    }

    private fun spigotRepo(list: MutableList<BuildRepository>) {
        list.add(
            BuildRepository(
                "spigotmc-repo",
                "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
            )
        )
    }
}

class AntJarConfigurationType : SimpleConfigurationType("foJarConfig", "Foundation Jar Configuration", "Jar Application configuration for Foundation",
    NotNullLazyValue.createValue { AllIcons.FileTypes.Archive }), ConfigurationType {

    fun createConfigurationProperties(config: BukkitProjectConfig, project: Project): RunConfiguration {
        val configuration = JarApplicationConfiguration(project, this, config.pluginName)
        configuration.jarPath = config.serverPath + if (config.minecraftVersion.replace(".", "").toInt() >= 1180) "/paperclip.jar" else "/cache/patched.jar" // TODO Change depending on server version
        configuration.workingDirectory = config.serverPath
        configuration.programParameters = "nogui"

        return configuration
    }

    //TODO Not used at all. Explore options to remove this entirely.
    override fun createTemplateConfiguration(project: Project): RunConfiguration {
        return JarApplicationConfiguration(project, this, "Jar Application")
    }

    override fun getHelpTopic(): String? {
        return "reference.dialogs.rundebug.JarApplication"
    }

    companion object {
        val instance: AntJarConfigurationType
            get() = ConfigurationTypeUtil.findConfigurationType(AntJarConfigurationType::class.java)
    }
}

class AntBuildStep(
    private val project: Project,
    private val rootDirectory: Path,
    private val config: BukkitProjectConfig,
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        if (!config.useAnt) {
            return
        }

        val text = BukkitTemplate.applyAntBuild(project, config)
        val file = CreatorStep.writeTextToFile(project, rootDirectory, "build.xml", text)

        invokeLater(project.disposed) {
            val runnerSettings = AntJarConfigurationType().createConfigurationProperties(config, project)

            val runManager = RunManager.getInstance(project)
            val runConfiguration = runManager.createConfiguration(runnerSettings, runnerSettings.factory!!)

            val antConfiguration = AntConfiguration.getInstance(project) as AntConfigurationImpl
            antConfiguration.addBuildFile(file)

            val antBeforeRunTask = AntBeforeRunTask(project)
            antBeforeRunTask.antFileUrl = "file://${project.basePath}/build.xml"
            antBeforeRunTask.targetName = "Build"

            val beforeRunTasks = mutableListOf<BeforeRunTask<*>>()
            beforeRunTasks.add(antBeforeRunTask)

            runConfiguration.configuration.beforeRunTasks = beforeRunTasks

            runManager.addConfiguration(runConfiguration)
            if (runManager.selectedConfiguration == null) {
                runManager.selectedConfiguration = runConfiguration
            }
        }
    }
}


class PluginYmlStep(
    private val project: Project,
    private val buildSystem: BuildSystem,
    private val config: BukkitProjectConfig
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        val text = BukkitTemplate.applyPluginYml(project, config, buildSystem)
        CreatorStep.writeTextToFile(project, buildSystem.dirsOrError.resourceDirectory, "plugin.yml", text)
    }
}
