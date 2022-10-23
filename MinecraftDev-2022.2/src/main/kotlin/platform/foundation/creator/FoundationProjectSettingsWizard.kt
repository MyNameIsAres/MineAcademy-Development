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


import com.demonwav.mcdev.asset.PlatformAssets
import com.demonwav.mcdev.creator.MinecraftModuleWizardStep
import com.demonwav.mcdev.creator.MinecraftProjectCreator
import com.demonwav.mcdev.creator.ValidatedField
import com.demonwav.mcdev.creator.ValidatedFieldType.*
import com.demonwav.mcdev.creator.exception.EmptyFieldSetupException
import com.demonwav.mcdev.creator.exception.SetupException
import com.demonwav.mcdev.creator.getVersionSelector
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.util.JavaVersions
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import javax.swing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.swing.Swing
import kotlinx.coroutines.withContext

class FoundationProjectSettingsWizard(private val creator: MinecraftProjectCreator,

) : MinecraftModuleWizardStep() {

    @ValidatedField(NON_BLANK)
    private lateinit var pluginNameField: JTextField

    @ValidatedField(NON_BLANK, CLASS_NAME)
    private lateinit var mainClassField: JTextField
    private lateinit var panel: JPanel
    private lateinit var configurationPanel: JPanel
    private lateinit var optionalPanel: JPanel
    private lateinit var descriptionField: JTextField

    @ValidatedField(LIST)
    private lateinit var authorsField: JTextField
    private lateinit var websiteField: JTextField

    @ValidatedField(LIST)
    private lateinit var dependField: JTextField
    private lateinit var softDependField: JTextField
    private lateinit var title: JLabel
    private lateinit var minecraftVersionBox: JComboBox<String>
    private lateinit var javaVersionBox: JComboBox<String>
    private lateinit var nmsCheckBox: JCheckBox
    private lateinit var antCheckBox: JCheckBox
    private lateinit var advancedMode: JCheckBox
    private lateinit var errorLabel: JLabel
    private lateinit var serverPathButton: TextFieldWithBrowseButton

    private var config: FoundationProjectConfig? = null

    private var versionsLoaded: Boolean = false

    init {
        if (advancedMode.isSelected) {
            configurationPanel.isVisible = true
            optionalPanel.isVisible = true

        } else {
            configurationPanel.isVisible = false
            optionalPanel.isVisible = false

        }


        advancedMode.addActionListener {
            if (advancedMode.isSelected) {
                configurationPanel.isVisible = true
                optionalPanel.isVisible = true
            } else {
                configurationPanel.isVisible = false
                optionalPanel.isVisible = false
            }
        }
    }

    override fun getComponent(): JComponent {
        return panel
    }

    override fun isStepVisible(): Boolean {
        return creator.config is FoundationProjectConfig
    }

    override fun updateStep() {
        config = creator.config as? FoundationProjectConfig
        if (config == null) {
            return
        }
        val conf = config ?: return

        basicUpdateStep(creator, pluginNameField, mainClassField)

        when (conf.type) {
            PlatformType.FOUNDATION -> {
                title.icon = PlatformAssets.FOUNDATION_ICON_2X
                title.text = "<html><font size=\"5\">Foundation Settings</font></html>"
            }
            else -> {
            }
        }

        JavaVersions.addJavaVersionBoxItems(javaVersionBox)

        if (versionsLoaded) {
            return
        }

        versionsLoaded = true
        CoroutineScope(Dispatchers.Swing).launch {
            try {
                withContext(Dispatchers.IO) { getVersionSelector(conf.type) }.set(minecraftVersionBox)
            } catch (e: Exception) {
                errorLabel.isVisible = true
            }
        }

        serverPathButton.addBrowseFolderListener(
            "",
            null,
            null,
            FileChooserDescriptorFactory.createSingleFileDescriptor()
        )
    }

    override fun validate(): Boolean {
        try {
            if (antCheckBox.isSelected && serverPathButton.text.isEmpty() || nmsCheckBox.isSelected && serverPathButton.text.isEmpty()) {
                throw EmptyFieldSetupException(serverPathButton)
            }

        } catch(e: SetupException) {
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(e.error, MessageType.ERROR, null)
                .setFadeoutTime(2000)
                .createBalloon()
                .show(RelativePoint.getSouthWestOf(e.j), Balloon.Position.below)
            return false
        }

        return super.validate() && minecraftVersionBox.selectedItem != null
    }

    override fun updateDataModel() {
        val conf = this.config ?: return


        conf.pluginName = this.pluginNameField.text
        conf.mainClass = this.mainClassField.text
        conf.description = this.descriptionField.text
        conf.website = this.websiteField.text
        conf.useNms = this.nmsCheckBox.isSelected
        conf.useAnt = this.antCheckBox.isSelected
        conf.serverPath = this.serverPathButton.text.replace("\\", "/")
        conf.minecraftVersion = this.minecraftVersionBox.selectedItem as String
        conf.comboBoxJavaVersion = this.javaVersionBox.selectedItem as String
        conf.useManualSpigotVersion = setManualSpigotVersion(conf)

        conf.setAuthors(this.authorsField.text)
        conf.setDependencies(this.dependField.text)
        conf.setSoftDependencies(this.softDependField.text)
    }

    private fun setManualSpigotVersion(config: FoundationProjectConfig): Boolean {
       kotlin.run {
           if (minecraftVersionBox.selectedItem as String == getVersionSelector(config.type).versions[0]) {
               return false;
           }
           return true;
       }
    }
}
