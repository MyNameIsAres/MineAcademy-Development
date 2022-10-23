/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package platform.foundation.data;

import com.charleskorn.kaml.MalformedYamlException
import com.charleskorn.kaml.MissingRequiredPropertyException
import com.charleskorn.kaml.UnknownPropertyException
import com.charleskorn.kaml.Yaml
import com.demonwav.mcdev.platform.foundation.data.ClassCreator
import com.demonwav.mcdev.platform.foundation.data.ClassGeneratorTemplateManager
import com.demonwav.mcdev.platform.foundation.exceptions.FileTemplateNotFoundException
import com.intellij.codeInsight.template.impl.TemplateEditorUtil
import com.intellij.ide.DataManager
import com.intellij.ide.fileTemplates.FileTemplateManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.ex.util.LexerEditorHighlighter
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.MessageType
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.awt.RelativePoint
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.util.stream.Stream
import javax.swing.JComponent
import javax.swing.JPanel
import org.jetbrains.annotations.Nls
import org.jetbrains.yaml.YAMLSyntaxHighlighter

class ClassCreatorConfigurable(private val project: Project) : Configurable {
    private lateinit var panel: JPanel
    private lateinit var editorPanel: JPanel
    private lateinit var templateEditor: Editor

    @Nls
    override fun getDisplayName() = "Class Generator Settings"

    override fun getHelpTopic(): String? = null

    override fun createComponent(): JComponent {
        return panel
    }

    private fun getActiveTemplateText() = ClassGeneratorTemplateManager.getProjectTemplateText(project)


    private fun init() {
        setupEditor()
    }


    private fun setupEditor() {
        templateEditor = TemplateEditorUtil.createEditor(false, getActiveTemplateText())
        val editorColorsScheme = EditorColorsManager.getInstance().globalScheme
        val highlighter = LexerEditorHighlighter(
            YAMLSyntaxHighlighter(),
            editorColorsScheme
        )
        (templateEditor as EditorEx).highlighter = highlighter
        templateEditor.settings.isLineNumbersShown = true

        editorPanel.preferredSize = JBUI.size(250, 100)
        editorPanel.minimumSize = editorPanel.preferredSize
        editorPanel.removeAll()
        editorPanel.add(templateEditor.component, BorderLayout.CENTER)
    }

    override fun isModified(): Boolean {
        return templateEditor.document.text != getActiveTemplateText()
    }

    private fun validate() {

    }


    override fun apply() {
        // TODO Validate
        try {
            val result = Yaml.default.decodeFromString(ClassCreator.serializer(), templateEditor.document.text)
            println("${result.directories.values}")

            result.directories.values.forEach { list ->
                list.classList.forEach { template ->
                    if (FileTemplateManager.getInstance(project).getTemplate(template) == null) {
                        throw FileTemplateNotFoundException("We couldn't find a template named $template")
                    }

                }
            }

        } catch (malformedYamlException: MalformedYamlException) {
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                "A MalformedYamlException occurred!" +
                        "\nCheck if your Yaml is formed correctly." +
                        "\nPlease use https://jsonformatter.org/yaml-validator " +
                        "if you're unsure!",
                MessageType.ERROR,
                null
            )
                .setFadeoutTime(4000)
                .createBalloon()
                .show(RelativePoint.getSouthEastOf(panel), Balloon.Position.above)
            return;
        } catch (missingRequiredPropertyException: MissingRequiredPropertyException) {
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                "A MissingRequiredPropertyException occurred!" +
                        "\nYou forgot to add ${missingRequiredPropertyException.propertyName}",
                MessageType.ERROR,
                null
            )
                .setFadeoutTime(4000)
                .createBalloon()
                .show(RelativePoint.getSouthEastOf(panel), Balloon.Position.above)


            return;

        } catch (unknownPropertyException: UnknownPropertyException) {
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                "An UnknownPropertyException occurred!" +
                        "\nWe don't know what ${unknownPropertyException.propertyName} is exactly.",
                MessageType.ERROR,
                null
            )
                .setFadeoutTime(4000)
                .createBalloon()
                .show(RelativePoint.getSouthEastOf(panel), Balloon.Position.above)
            return;

        } catch (fileTemplateNotFoundException: FileTemplateNotFoundException) {
            JBPopupFactory.getInstance().createHtmlTextBalloonBuilder(
                "An FileTemplateNotFoundException occurred!" +
                        "\n${fileTemplateNotFoundException.message}",
                MessageType.ERROR,
                null
            )
                .setFadeoutTime(4000)
                .createBalloon()
                .show(RelativePoint.getSouthEastOf(panel), Balloon.Position.above)
            return;
        }


        val project = CommonDataKeys.PROJECT.getData(DataManager.getInstance().getDataContext(panel))
        ClassGeneratorTemplateManager.writeProjectTemplate(project!!, templateEditor.document.text)
    }

    override fun reset() {
        init()
    }
}
