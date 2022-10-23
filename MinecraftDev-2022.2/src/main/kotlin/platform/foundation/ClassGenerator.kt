/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package platform.foundation;

import com.intellij.psi.search.TodoAttributesUtil
import com.intellij.psi.search.TodoPattern
import com.intellij.ui.AnActionButtonRunnable
import com.intellij.ui.ToolbarDecorator
import javax.swing.JPanel
import javax.swing.JTable
import javax.swing.JTextField

class ClassGenerator {

    private lateinit var aresPanel: JPanel
    private lateinit var groupClass: JTextField
    private lateinit var packageName: JTextField
    private lateinit var aresTable: JTable

    init {
        aresPanel.add(ToolbarDecorator.createDecorator(aresTable).disableAddAction()
            .setAddAction(AnActionButtonRunnable {

            })
            .setRemoveAction(AnActionButtonRunnable {

            })

            .createPanel())



    }





}
