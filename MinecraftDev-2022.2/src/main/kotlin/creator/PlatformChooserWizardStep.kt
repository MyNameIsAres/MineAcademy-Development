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

import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.bukkit.creator.BukkitProjectConfig
import com.demonwav.mcdev.platform.foundation.creator.FoundationProjectConfig
import com.intellij.ide.util.projectWizard.ModuleWizardStep
import com.intellij.ui.components.JBRadioButton
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel

class PlatformChooserWizardStep(private val creator: MinecraftProjectCreator) : ModuleWizardStep() {

    private lateinit var panel: JPanel

    private lateinit var projectButtons: ButtonGroup
    private lateinit var foundationPluginButton: JBRadioButton
    private lateinit var spigotPluginButton: JBRadioButton


//    private lateinit var bukkitPluginButton: JBRadioButton
//    private lateinit var paperPluginButton: JBRadioButton
    override fun getComponent(): JComponent {
//        if (UIUtil.isUnderDarcula()) {
//            spongeIcon.icon = PlatformAssets.SPONGE_ICON_2X_DARK
//        } else {
//            spongeIcon.icon = PlatformAssets.SPONGE_ICON_2X
//        }

        return panel
    }

    override fun updateDataModel() {
        creator.config = buildConfig()
    }

    override fun validate(): Boolean {
        updateDataModel()
        val isValid = projectButtons.selection != null
        if (isValid && creator.config == null) {
            throw IllegalStateException(
                "A project button does not have an associated config! Make sure to add your button to buildConfig()"
            )
        }
        return isValid
    }

    private fun buildConfig(): ProjectConfig? {
        return when {
            foundationPluginButton.isSelected -> FoundationProjectConfig(PlatformType.FOUNDATION)
            spigotPluginButton.isSelected -> BukkitProjectConfig(PlatformType.SPIGOT)
//            bukkitPluginButton.isSelected -> BukkitProjectConfig(PlatformType.BUKKIT)
//
//            paperPluginButton.isSelected -> BukkitProjectConfig(PlatformType.PAPER)
//
            else -> null
        }
    }
}
