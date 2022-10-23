/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.generation

import com.demonwav.mcdev.insight.generation.GenerationData
import com.demonwav.mcdev.insight.generation.ui.EventGenerationPanel
import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.psi.PsiClass
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JCheckBox
import javax.swing.JComboBox
import javax.swing.JPanel

class BukkitEventGenerationPanel(chosenClass: PsiClass) : EventGenerationPanel(chosenClass) {

    private lateinit var ignoreCanceledCheckBox: JCheckBox
    private lateinit var parentPanel: JPanel
    private lateinit var eventPriorityComboBox: JComboBox<String>

    override val panel: JPanel?
        get() {
            ignoreCanceledCheckBox.isSelected = true

            // Not static because the form builder is not reliable
            eventPriorityComboBox.addItem("MONITOR")
            eventPriorityComboBox.addItem("HIGHEST")
            eventPriorityComboBox.addItem("HIGH")
            eventPriorityComboBox.addItem("NORMAL")
            eventPriorityComboBox.addItem("LOW")
            eventPriorityComboBox.addItem("LOWEST")

            eventPriorityComboBox.selectedIndex = 3
//


            return parentPanel
        }

    override fun gatherData(): GenerationData? {
        return BukkitGenerationData(ignoreCanceledCheckBox.isSelected, eventPriorityComboBox.selectedItem.toString())
    }
}
