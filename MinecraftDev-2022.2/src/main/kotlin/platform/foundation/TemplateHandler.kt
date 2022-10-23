/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.foundation

import com.charleskorn.kaml.Yaml
import com.demonwav.mcdev.platform.foundation.data.ClassCreator
import com.demonwav.mcdev.platform.foundation.data.ClassGeneratorTemplateManager
import com.intellij.ide.IdeBundle
import com.intellij.ide.actions.EditFileTemplatesAction
import com.intellij.ide.fileTemplates.CreateFromTemplateActionReplacer
import com.intellij.ide.fileTemplates.FileTemplate
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.ide.fileTemplates.FileTemplateUtil
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateAction
import com.intellij.ide.fileTemplates.actions.CreateFromTemplateActionBase
import com.intellij.ide.fileTemplates.ui.SelectTemplateDialog
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileTypes.StdFileTypes
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsActions
import com.intellij.psi.PsiDirectory
import com.intellij.util.PlatformIcons
import com.intellij.util.containers.toArray
import java.io.File
import java.util.*


class TemplateHandler : ActionGroup(), DumbAware {

    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun getChildren(e: AnActionEvent?): Array<AnAction> {
        println("Is this callled and why")
        if (e == null) return EMPTY_ARRAY
        val project = e.project
        if (project == null || project.isDisposed) return EMPTY_ARRAY
        val manager = FileTemplateManager.getInstance(project)
        var templates = manager.allTemplates
        val showAll = templates.size <= FileTemplateManager.RECENT_TEMPLATES_SIZE
        if (!showAll) {
            val recentNames = manager.recentNames
            templates = arrayOfNulls(recentNames.size)
            var i = 0
            for (name in recentNames) {
                templates[i] = manager.getTemplate(name!!)
                i++
            }
        }
        Arrays.sort(templates) { template1: FileTemplate?, template2: FileTemplate? ->
            // java first
            if (template1!!.isTemplateOfType(StdFileTypes.JAVA) && !template2!!.isTemplateOfType(StdFileTypes.JAVA)) {
                return@sort -1
            }
            if (template2!!.isTemplateOfType(StdFileTypes.JAVA) && !template1.isTemplateOfType(StdFileTypes.JAVA)) {
                return@sort 1
            }

            // group by type
            val i = template1.extension.compareTo(template2.extension)
            if (i != 0) {
                return@sort i
            }
            template1.name.compareTo(template2.name)
        }
        val result: MutableList<AnAction> = ArrayList()

        val toReturnError = FileTemplateManager.getInstance(project).getTemplate("sldakfjas")
        println("This will return error $toReturnError")


        val yamlResult = Yaml.default.decodeFromString(
            ClassCreator.serializer(),
            ClassGeneratorTemplateManager.getProjectTemplateText(project).toString()
        )
        println("This is my yamlResult: $yamlResult")

        val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
        if (file != null && file.isDirectory) {
            val dirName = yamlResult.directories[file.name]
            dirName?.classList?.forEach { classItem ->
                val template = FileTemplateManager.getInstance(project).getTemplate(classItem)

                var someAction = replaceAction(template)

                if (someAction == null) {
                    someAction = CreateFromTemplateAction(template)

                }
                someAction.templatePresentation.icon = PlatformIcons.CLASS_ICON

                result.add(someAction)
            }
        }

        if (!result.isEmpty() || !showAll) {
            if (!showAll) {
                result.add(CreateFromTemplatesAction(IdeBundle.message("action.from.file.template")))
            }
            result.add(Separator.getInstance())
            result.add(EditFileTemplatesAction(IdeBundle.message("action.edit.file.templates")))
        }
        return result.toArray(EMPTY_ARRAY)
    }

    fun readFileAsLinesUsingBufferedReader(fileName: String): List<String> = File(fileName).bufferedReader().readLines()


    fun readFileDirectlyAsText(fileName: String): String = File(fileName).readText(Charsets.UTF_8)


    private class CreateFromTemplatesAction internal constructor(title: @NlsActions.ActionText String?) :
        CreateFromTemplateActionBase(title, null, null) {
        override fun getReplacedAction(template: FileTemplate): AnAction? {
            return replaceAction(template)
        }

        override fun getTemplate(project: Project, dir: PsiDirectory): FileTemplate {
            val dialog = SelectTemplateDialog(project, dir)
            dialog.show()

            println("Selected: ${dialog.selectedTemplate}")

            return dialog.selectedTemplate
        }
    }

    companion object {
        private fun replaceAction(template: FileTemplate?): AnAction? {
            for (actionFactory in CreateFromTemplateActionReplacer.CREATE_FROM_TEMPLATE_REPLACER.extensionList) {
                val action = actionFactory.replaceCreateFromFileTemplateAction(template)
                if (action != null) {
                    return action
                }
            }
            return null
        }

        fun canCreateFromTemplate(e: AnActionEvent?, template: FileTemplate): Boolean {
            if (e == null) return false
            val dataContext = e.dataContext
            val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return false
            val dirs = view.directories

            return if (dirs.size == 0) false else FileTemplateUtil.canCreateFromTemplate(dirs, template)
        }
    }
}
