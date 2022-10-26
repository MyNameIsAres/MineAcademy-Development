/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.creator

import com.demonwav.mcdev.asset.PlatformAssets
import com.demonwav.mcdev.creator.buildsystem.BuildSystemType
import com.demonwav.mcdev.platform.MinecraftModuleType
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.bukkit.creator.BukkitProjectConfig
import com.demonwav.mcdev.platform.bukkit.creator.BukkitProjectSettingsWizard
import com.demonwav.mcdev.platform.foundation.creator.FoundationProjectConfig
import com.demonwav.mcdev.platform.foundation.creator.FoundationProjectSettingsWizard
import com.intellij.ide.IdeBundle
import com.intellij.ide.JavaUiBundle
import com.intellij.ide.projectWizard.ProjectSettingsStep
import com.intellij.ide.starters.JavaStartersBundle
import com.intellij.ide.starters.shared.*
import com.intellij.ide.util.installNameGenerators
import com.intellij.ide.util.projectWizard.*
import com.intellij.ide.wizard.withVisualPadding
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.module.StdModuleTypes
import com.intellij.openapi.observable.properties.GraphProperty
import com.intellij.openapi.observable.properties.PropertyGraph
import com.intellij.openapi.observable.util.bindBooleanStorage
import com.intellij.openapi.observable.util.joinCanonicalPath
import com.intellij.openapi.observable.util.transform
import com.intellij.openapi.observable.util.trim
import com.intellij.openapi.project.DumbAwareRunnable
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.projectRoots.Sdk
import com.intellij.openapi.roots.ModifiableRootModel
import com.intellij.openapi.roots.ui.configuration.JdkComboBox
import com.intellij.openapi.roots.ui.configuration.ModulesProvider
import com.intellij.openapi.roots.ui.configuration.sdkComboBox
import com.intellij.openapi.startup.StartupManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.getCanonicalPath
import com.intellij.openapi.ui.getPresentablePath
import com.intellij.openapi.util.io.FileUtil
import com.intellij.openapi.util.text.StringUtil
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.UIBundle
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JComponent
import javax.swing.JTextField
import org.jetbrains.annotations.Nls

class MinecraftModuleBuilder : JavaModuleBuilder() {

    private val creator = MinecraftProjectCreator()

    override fun getPresentableName() = MinecraftModuleType.NAME
    override fun getNodeIcon() = PlatformAssets.FOUNDATION_ICON
    override fun getGroupName() = MinecraftModuleType.NAME
    override fun getWeight() = BUILD_SYSTEM_WEIGHT - 1
    override fun getBuilderId() = "MINECRAFT_MODULE"

    override fun isAvailable() = true // TODO: use the new project wizard system

    override fun setupRootModel(modifiableRootModel: ModifiableRootModel) {
        val project = modifiableRootModel.project
        val (root, vFile) = createAndGetRoot()
        modifiableRootModel.addContentEntry(vFile)


        if (moduleJdk != null) {
            modifiableRootModel.sdk = moduleJdk
        } else {
            modifiableRootModel.inheritSdk()
        }

        val r = DumbAwareRunnable {
            creator.create(root, modifiableRootModel.module)
        }

        if (project.isDisposed) {
            return
        }

        if (
            ApplicationManager.getApplication().isUnitTestMode ||
            ApplicationManager.getApplication().isHeadlessEnvironment
        ) {
            r.run()
            return
        }

        if (!project.isInitialized) {
            StartupManager.getInstance(project).registerPostStartupActivity(r)
            return
        }

        DumbService.getInstance(project).runWhenSmart(r)
    }

    private fun createAndGetRoot(): Pair<Path, VirtualFile> {
        val temp = contentEntryPath ?: throw IllegalStateException("Failed to get content entry path")

        val pathName = FileUtil.toSystemIndependentName(temp)

        val path = Paths.get(pathName)
        Files.createDirectories(path)
        val vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(pathName)
            ?: throw IllegalStateException("Failed to refresh and file file: $path")

        return path to vFile
    }

    override fun getModuleType(): ModuleType<*> = MinecraftModuleType.instance
    override fun getParentGroup() = MinecraftModuleType.NAME

    override fun getIgnoredSteps(): List<Class<out ModuleWizardStep>> {
        return listOf(ProjectSettingsStep::class.java)
    }

    override fun createWizardSteps(
        wizardContext: WizardContext,
        modulesProvider: ModulesProvider
    ): Array<ModuleWizardStep> {
        return arrayOf(
            FoundationProjectSettingsWizard(creator),
            BukkitProjectSettingsWizard(creator),
        )
    }



    override fun modifyProjectTypeStep(settingsStep: SettingsStep): ModuleWizardStep? {
        return null;
    }
    override fun getCustomOptionsStep(context: WizardContext?, parentDisposable: Disposable?) =
    AresNewWizardStep(context!!, CommonStarterContext(), parentDisposable!!,this, creator)
}

class AresNewWizardStep(
    wizardContext: WizardContext,
    starterContext: CommonStarterContext,
    parentDisposable: Disposable,
    moduleBuilder: ModuleBuilder,
    private val creator: MinecraftProjectCreator
) : ModuleWizardStep() {
    private val validatedTextComponents: MutableList<JTextField> = mutableListOf()

    private val wizardContext: WizardContext = wizardContext
    private val starterContext: CommonStarterContext = CommonStarterContext()
    private val parentDisposable: Disposable = parentDisposable
    private val moduleBuilder: ModuleBuilder = moduleBuilder

    private val propertyGraph: PropertyGraph = PropertyGraph()
    private val entityNameProperty: GraphProperty<String> = propertyGraph.lazyProperty(::suggestName)
    private val locationProperty: GraphProperty<String> = propertyGraph.lazyProperty(::suggestLocationByName)
    private val canonicalPathProperty = locationProperty.joinCanonicalPath(entityNameProperty)

    private lateinit var groupRow: Row
    private lateinit var artifactRow: Row
    private lateinit var versionRow: Row


    private var entityName: String by entityNameProperty.trim()
    private var location: String by locationProperty


    private val groupIdProperty: GraphProperty<String> = propertyGraph.lazyProperty { starterContext.group }
    private val artifactIdProperty: GraphProperty<String> = propertyGraph.lazyProperty { entityName }
    private val versionProperty: GraphProperty<String> = propertyGraph.lazyProperty { "1.0-SNAPSHOT" }
    private val sdkProperty: GraphProperty<Sdk?> = propertyGraph.lazyProperty { null }
    private lateinit var sdkComboBox: JdkComboBox

    private val buildSystemProperty: GraphProperty<BuildSystemType> = propertyGraph.property(BuildSystemType.MAVEN)
    private val platformProperty : GraphProperty<PlatformType> = propertyGraph.property(PlatformType.FOUNDATION)

    private fun buildConfig(): ProjectConfig {
        return when {
            platformProperty.get() == PlatformType.FOUNDATION -> FoundationProjectConfig(PlatformType.FOUNDATION)
            platformProperty.get() == PlatformType.SPIGOT -> BukkitProjectConfig(PlatformType.SPIGOT)
            else -> {FoundationProjectConfig(PlatformType.FOUNDATION)}
        }
    }
    private fun suggestName(): String {
        return suggestName(starterContext.artifact)
    }

    private fun suggestLocationByName(): String {
        return wizardContext.projectFileDirectory
    }

    private fun suggestName(prefix: String): String {
        val projectFileDirectory = File(wizardContext.projectFileDirectory)
        return FileUtil.createSequentFileName(projectFileDirectory, prefix, "")
    }
    override fun getComponent(): JComponent {
      return createComponent()
    }

    override fun updateDataModel() {
        wizardContext.projectName = entityName
        wizardContext.setProjectFileDirectory(FileUtil.join(location, entityName))
        creator.buildSystem = BuildSystemType.MAVEN.create(groupIdProperty.get(), artifactIdProperty.get(), versionProperty.get())
        creator.config = buildConfig()

        val sdk = sdkProperty.get()
        moduleBuilder.moduleJdk = sdk

        if (wizardContext.project == null) {
            wizardContext.projectJdk = sdk
        }
    }
    private fun createComponent(): DialogPanel {
        return panel {
            addProjectLocationUi()

            addProjectTypeUI()

            addGroupArtifactUi()

            addSdkUi()

        }.withVisualPadding(topField = true)
    }

    @Suppress("SameParameterValue")
    private fun <T : JComponent> Cell<T>.withSpecialValidation(vararg errorValidationUnits: TextValidationFunction): Cell<T> =
        withValidation(this, errorValidationUnits.asList(), null, validatedTextComponents, parentDisposable)

    private fun <T : JComponent> Cell<T>.withSpecialValidation(
        errorValidationUnits: List<TextValidationFunction>,
        warningValidationUnit: TextValidationFunction?
    ): Cell<T> {
        return withValidation(this, errorValidationUnits, warningValidationUnit, validatedTextComponents, parentDisposable)
    }

    private fun Panel.addSdkUi() {
        row(JavaUiBundle.message("label.project.wizard.new.project.jdk")) {
            sdkComboBox = sdkComboBox(wizardContext, sdkProperty, StdModuleTypes.JAVA.id, moduleBuilder::isSuitableSdkType)
                .columns(COLUMNS_MEDIUM)
                .component
        }.bottomGap(BottomGap.SMALL)
    }

    private fun Panel.addProjectLocationUi() {
        row(UIBundle.message("label.project.wizard.new.project.name")) {
            textField()
                .bindText(entityNameProperty)
                .withSpecialValidation(
                    listOf(ValidationFunctions.CHECK_NOT_EMPTY, ValidationFunctions.CHECK_SIMPLE_NAME_FORMAT),
                    ValidationFunctions.createLocationWarningValidator(locationProperty)
                )
                .columns(COLUMNS_MEDIUM)
                .gap(RightGap.SMALL)
                .focused()

            installNameGenerators(moduleBuilder.builderId, entityNameProperty)
        }.bottomGap(BottomGap.SMALL)

        val locationRow = row(UIBundle.message("label.project.wizard.new.project.location")) {
            val commentLabel = projectLocationField(locationProperty, wizardContext)
                .horizontalAlign(HorizontalAlign.FILL)
                .withSpecialValidation(
                    ValidationFunctions.CHECK_NOT_EMPTY,
                    ValidationFunctions.CHECK_LOCATION_FOR_ERROR
                )
                .comment(getLocationComment(), 100)
                .comment!!

            entityNameProperty.afterChange { commentLabel.text = getLocationComment() }
            locationProperty.afterChange { commentLabel.text = getLocationComment() }
        }
    }


    private fun Panel.addGroupArtifactUi() {
        row(JavaStartersBundle.message("title.project.group.label")) {
            groupRow = this

            textField()
                .bindText(groupIdProperty)
                .columns(COLUMNS_MEDIUM)
                .withSpecialValidation(
                    ValidationFunctions.CHECK_NOT_EMPTY,
                    ValidationFunctions.CHECK_NO_WHITESPACES,
                    ValidationFunctions.CHECK_GROUP_FORMAT,
                    ValidationFunctions.CHECK_NO_RESERVED_WORDS,
                   )
        }.bottomGap(BottomGap.SMALL)

        row(JavaStartersBundle.message("title.project.artifact.label")) {
            artifactRow = this

            textField()
                .bindText(artifactIdProperty)
                .columns(COLUMNS_MEDIUM)
                .withSpecialValidation(
                    ValidationFunctions.CHECK_NOT_EMPTY,
                    ValidationFunctions.CHECK_NO_WHITESPACES,
                    ValidationFunctions.CHECK_ARTIFACT_SIMPLE_FORMAT,
                    ValidationFunctions.CHECK_NO_RESERVED_WORDS,
                  )
        }.bottomGap(BottomGap.SMALL)

        row("Version:") {
            versionRow = this

            textField()
                .bindText(versionProperty)
                .columns(COLUMNS_MEDIUM)
                .withSpecialValidation(
                    ValidationFunctions.CHECK_NOT_EMPTY,
                    ValidationFunctions.CHECK_NO_WHITESPACES,
                    ValidationFunctions.CHECK_ARTIFACT_SIMPLE_FORMAT,
                    ValidationFunctions.CHECK_NO_RESERVED_WORDS,
                )
        }.bottomGap(BottomGap.SMALL)
    }
    //! Temporarily disabled
    private fun Panel.addBuildSystemUi() {
        val renderer: (BuildSystemType) -> String = {
            when (it) {
                BuildSystemType.MAVEN -> "Maven"
                BuildSystemType.GRADLE -> "Gradle"
            }
        }
        if (platformProperty.get() == PlatformType.SPIGOT){
            row("Build system:") {
                segmentedButton(mutableListOf(BuildSystemType.MAVEN, BuildSystemType.GRADLE),renderer).bind(buildSystemProperty)

            }.bottomGap(BottomGap.SMALL)
        }
    }

    private fun Panel.addProjectTypeUI() {
        val renderer: (PlatformType) -> String = {
            when (it) {
                PlatformType.FOUNDATION -> "Foundation"
                PlatformType.SPIGOT -> "Spigot"
                else -> {""}
            }
        }
        row("Platform:") {
            segmentedButton(mutableListOf(PlatformType.FOUNDATION, PlatformType.SPIGOT), renderer).bind(
                platformProperty
            )
        }
    }

    private fun Row.projectLocationField(locationProperty: GraphProperty<String>,
                                         wizardContext: WizardContext): Cell<TextFieldWithBrowseButton> {
        val fileChooserDescriptor = FileChooserDescriptorFactory.createSingleLocalFileDescriptor().withFileFilter { it.isDirectory }
        val fileChosen = { file: VirtualFile -> getPresentablePath(file.path) }
        val title = IdeBundle.message("title.select.project.file.directory", wizardContext.presentationName)
        val property = locationProperty.transform(::getPresentablePath, ::getCanonicalPath)
        return this.textFieldWithBrowseButton(title, wizardContext.project, fileChooserDescriptor, fileChosen)
            .bindText(property)
    }

    private fun getLocationComment(): @Nls String {
        val shortPath = StringUtil.shortenPathWithEllipsis(getPresentablePath(canonicalPathProperty.get()), 60)
        return UIBundle.message("label.project.wizard.new.project.path.description", wizardContext.isCreatingNewProjectInt, shortPath)
    }
}

