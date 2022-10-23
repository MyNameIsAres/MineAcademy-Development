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

import com.intellij.openapi.project.Project
import com.intellij.project.stateStore
import java.io.File

object ClassGeneratorTemplateManager {

    private val DEFAULT_VALUE = """
        directories:
            command:
                classList:
                   - SimpleCommand
            conversation:
                classList:
                   - SimpleConversation
                   - SimplePrompt
    """.trimIndent()

    private const val FILE_NAME = "class_generator_template.yml"

    private fun projectFile(project: Project): File? =
        project.stateStore.directoryStorePath?.resolve(FILE_NAME)?.toFile()

    fun getProjectTemplateText(project: Project): String? =
        projectFile(project)?.let { if (it.exists()) it.readText() else DEFAULT_VALUE }



    fun writeProjectTemplate(project: Project, text: String) = projectFile(project)?.writeText(text)
}