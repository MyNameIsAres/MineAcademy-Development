/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.insight.generation.ui

import com.intellij.openapi.wm.ex.IdeFocusTraversalPolicy
import com.intellij.uiDesigner.core.GridConstraints
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JSeparator
import javax.swing.JTextField

class EventListenerWizard(panel: JPanel?, className: String, defaultListenerName: String) {
    lateinit var panel: JPanel
    private lateinit var classNameTextField: JTextField
    private lateinit var listenerNameTextField: JTextField
    private lateinit var publicVoidLabel: JLabel
    private lateinit var contentPanel: JPanel
    private lateinit var separator: JSeparator

    init {
        if (panel != null) {
            separator.isVisible = true
            contentPanel.add(panel, innerContentPanelConstraints)
        }

        classNameTextField.text = className
        listenerNameTextField.text = defaultListenerName

        IdeFocusTraversalPolicy.getPreferredFocusedComponent(listenerNameTextField).requestFocus()
        listenerNameTextField.requestFocus()
    }

    val chosenClassName: String
        get() = listenerNameTextField.text

    companion object {
        private val innerContentPanelConstraints = GridConstraints()

        init {
            innerContentPanelConstraints.row = 0
            innerContentPanelConstraints.column = 0
            innerContentPanelConstraints.rowSpan = 1
            innerContentPanelConstraints.colSpan = 1
            innerContentPanelConstraints.anchor = GridConstraints.ANCHOR_CENTER
            innerContentPanelConstraints.fill = GridConstraints.FILL_BOTH
            innerContentPanelConstraints.hSizePolicy = GridConstraints.SIZEPOLICY_FIXED
            innerContentPanelConstraints.vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
        }
    }
}
