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

import com.demonwav.mcdev.creator.BaseProjectCreator
import com.demonwav.mcdev.creator.BasicJavaClassStep
import com.demonwav.mcdev.creator.CreatorStep
import com.demonwav.mcdev.creator.buildsystem.BuildDependency
import com.demonwav.mcdev.creator.buildsystem.BuildRepository
import com.demonwav.mcdev.creator.buildsystem.BuildSystem
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenFinalizerStep
import com.demonwav.mcdev.creator.buildsystem.maven.BasicMavenStep
import com.demonwav.mcdev.creator.buildsystem.maven.MavenBuildSystem
import com.demonwav.mcdev.creator.buildsystem.maven.MavenGitignoreStep
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.util.invokeLater
import com.intellij.execution.BeforeRunTask
import com.intellij.execution.RunManager
import com.intellij.execution.configurations.ConfigurationType
import com.intellij.execution.configurations.ConfigurationTypeUtil.findConfigurationType
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


sealed class FoundationProjectCreator<T : BuildSystem>(
    protected val rootDirectory: Path,
    protected val rootModule: Module,
    protected val buildSystem: T,
    protected val config: FoundationProjectConfig,
) : BaseProjectCreator(rootModule, buildSystem) {

    protected fun setupMainClassStep(): BasicJavaClassStep {
        return createJavaClassStep(config.mainClass) { packageName, className ->

            FoundationTemplate.applyMainClass(project, packageName, className)
        }
    }

    protected fun setupDependencyStep(): FoundationDependenciesStep {
        return FoundationDependenciesStep(
            buildSystem,
            config.type,
            config.minecraftVersion,
            config.useNms,
            config.useManualSpigotVersion,
            config.serverPath
        )
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
    config: FoundationProjectConfig
) : FoundationProjectCreator<MavenBuildSystem>(rootDirectory, rootModule, buildSystem, config) {

    override fun getSteps(): Iterable<CreatorStep> {
        val pomText = FoundationTemplate.applyPom(project, config)
        return listOf(
            setupDependencyStep(),
            BasicMavenStep(project, rootDirectory, buildSystem, config, pomText),
            setupAntBuildStep(),
            setupMainClassStep(),
            setupYmlStep(),
            MavenGitignoreStep(project, rootDirectory),
            BasicMavenFinalizerStep(rootModule, rootDirectory) ,
        )
    }
}

open class FoundationDependenciesStep(
    protected val buildSystem: BuildSystem,
    protected val type: PlatformType,
    protected val mcVersion: String,
    private val enableNms: Boolean,
    private val useManualSpigotVersion: Boolean,
    private val serverPath: String

) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        run {
            buildSystem.repositories.add(
                BuildRepository(
                    "jitpack.io",
                    "https://jitpack.io"
                )
            )

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

            if (useManualSpigotVersion) {
                buildSystem.repositories.add(
                    BuildRepository(
                        "spigot-mc",
                        "https://hub.spigotmc.org/nexus/content/repositories/snapshots/"
                    )
                )
                buildSystem.repositories.add(
                    BuildRepository(
                        "sonatype",
                        "https://oss.sonatype.org/content/groups/public/"
                    )
                )

                buildSystem.dependencies.add(
                    BuildDependency(
                        "org.spigotmc",
                        "spigot-api",
                        "$mcVersion-R0.1-SNAPSHOT",
                    )
                )
            }

            buildSystem.dependencies.add(
                BuildDependency(
                    "\${foundation.path}",
                    "Foundation",
                    "\${foundation.version}"
                )
            )
        }
    }
}


class AntJarConfigurationType : SimpleConfigurationType("foJarConfig", "Foundation Jar Configuration", "Jar Application configuration for Foundation",
    NotNullLazyValue.createValue { AllIcons.FileTypes.Archive }), ConfigurationType{

    fun createConfigurationProperties(config: FoundationProjectConfig, project: Project): RunConfiguration {
        val configuration = JarApplicationConfiguration(project, this, config.pluginName)
        configuration.jarPath = config.serverPath + if (config.minecraftVersion.replace(".", "").toInt() >= 1180) "/paperclip.jar" else "/cache/patched.jar"
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
            get() = findConfigurationType(AntJarConfigurationType::class.java)
    }
}

class AntBuildStep(
    private val project: Project,
    private val rootDirectory: Path,
    private val config: FoundationProjectConfig,
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        if (!config.useAnt) {
            return
        }

        val text = FoundationTemplate.applyAntBuild(project, config)
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
    private val config: FoundationProjectConfig
) : CreatorStep {
    override fun runStep(indicator: ProgressIndicator) {
        val text = FoundationTemplate.applyPluginYml(project, config, buildSystem)
        CreatorStep.writeTextToFile(project, buildSystem.dirsOrError.resourceDirectory, "plugin.yml", text)
    }
}

