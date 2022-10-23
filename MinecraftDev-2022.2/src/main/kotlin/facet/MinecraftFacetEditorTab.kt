/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.facet

import com.demonwav.mcdev.platform.PlatformType
import com.intellij.facet.ui.FacetEditorTab
import com.intellij.util.ui.UIUtil
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class MinecraftFacetEditorTab(private val configuration: MinecraftFacetConfiguration) : FacetEditorTab() {

    private lateinit var panel: JPanel

    private lateinit var spigotEnabledCheckBox: JCheckBox
    private lateinit var spigotAutoCheckBox: JCheckBox
    private lateinit var foundationEnabledCheckBox: JCheckBox
    private lateinit var foundationAutoCheckBox: JCheckBox


    private val enableCheckBoxArray: Array<JCheckBox> by lazy {
        arrayOf(
            spigotEnabledCheckBox,
            foundationEnabledCheckBox

        )
    }

    private val autoCheckBoxArray: Array<JCheckBox> by lazy {
        arrayOf(
            spigotAutoCheckBox,
            foundationEnabledCheckBox
        )
    }

    override fun createComponent(): JComponent {
        if (UIUtil.isUnderDarcula()) {

        }

        runOnAll { enabled, auto, platformType, _, _ ->
            auto.addActionListener { checkAuto(auto, enabled, platformType) }
        }


        spigotEnabledCheckBox.addActionListener {
            unique(
                spigotEnabledCheckBox,
            )
        }


        spigotAutoCheckBox.addActionListener {
            all(spigotAutoCheckBox)(
            )
        }


        return panel
    }

    override fun getDisplayName() = "Minecraft Module Settings"

    override fun isModified(): Boolean {
        var modified = false

        runOnAll { enabled, auto, platformType, userTypes, _ ->
            modified += auto.isSelected == platformType in userTypes
            modified += !auto.isSelected && enabled.isSelected != userTypes[platformType]
        }

        return modified
    }

    override fun reset() {
        runOnAll { enabled, auto, platformType, userTypes, autoTypes ->
            auto.isSelected = platformType !in userTypes
            enabled.isSelected = userTypes[platformType] ?: (platformType in autoTypes)

            if (auto.isSelected) {
                enabled.isEnabled = false
            }
        }
    }

    override fun apply() {
        configuration.state.userChosenTypes.clear()
        runOnAll { enabled, auto, platformType, userTypes, _ ->
            if (!auto.isSelected) {
                userTypes[platformType] = enabled.isSelected
            }
        }
    }

    private inline fun runOnAll(
        run: (JCheckBox, JCheckBox, PlatformType, MutableMap<PlatformType, Boolean>, Set<PlatformType>) -> Unit
    ) {
        val state = configuration.state
        for (i in indexes) {
            run(
                enableCheckBoxArray[i],
                autoCheckBoxArray[i],
                platformTypes[i],
                state.userChosenTypes,
                state.autoDetectTypes
            )
        }
    }

    private fun unique(vararg checkBoxes: JCheckBox) {
        if (checkBoxes.size <= 1) {
            return
        }

        if (checkBoxes[0].isSelected) {
            for (i in 1 until checkBoxes.size) {
                checkBoxes[i].isSelected = false
            }
        }
    }

    private fun also(vararg checkBoxes: JCheckBox) {
        if (checkBoxes.size <= 1) {
            return
        }

        if (checkBoxes[0].isSelected) {
            for (i in 1 until checkBoxes.size) {
                checkBoxes[i].isSelected = true
            }
        }
    }

    private fun all(vararg checkBoxes: JCheckBox): Invoker {
        if (checkBoxes.size <= 1) {
            return Invoker()
        }

        for (i in 1 until checkBoxes.size) {
            checkBoxes[i].isSelected = checkBoxes[0].isSelected
        }

        return object : Invoker() {
            override fun invoke(vararg indexes: Int) {
                for (i in indexes) {
                    checkAuto(autoCheckBoxArray[i], enableCheckBoxArray[i], platformTypes[i])
                }
            }
        }
    }

    private fun checkAuto(auto: JCheckBox, enabled: JCheckBox, type: PlatformType) {
        if (auto.isSelected) {
            enabled.isEnabled = false
            enabled.isSelected = type in configuration.state.autoDetectTypes
        } else {
            enabled.isEnabled = true
        }
    }

    private operator fun Boolean.plus(n: Boolean) = this || n

    // This is here so we can use vararg. Can't use parameter modifiers in function type definitions for some reason
    open class Invoker {
        open operator fun invoke(vararg indexes: Int) {}
    }

    companion object {

        private const val SPIGOT = 0
        private const val FOUNDATION = SPIGOT + 1


        private val platformTypes = arrayOf(
            PlatformType.FOUNDATION,

            PlatformType.SPIGOT,

        )

        private val indexes = intArrayOf(
           SPIGOT,
            FOUNDATION
        )
    }
}
