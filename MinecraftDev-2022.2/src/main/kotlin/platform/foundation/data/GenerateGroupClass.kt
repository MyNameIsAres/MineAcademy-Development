/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.foundation.data

import com.intellij.icons.AllIcons
import com.intellij.ide.IdeBundle
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent
import platform.foundation.data.AresTestMenu

class GenerateGroupClass(project: Project?) : DialogWrapper(project) {
    override fun createCenterPanel(): JComponent? {
        return AresTestMenu().panel
    }



    init {
        title = "Hello World"
        isOKActionEnabled = true
        setValidationDelay(0)
        init()
    }

}
