/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.util

import com.intellij.util.lang.JavaVersion
import javax.swing.JComboBox

object JavaVersions {

    fun requiredJavaVersion(comboBoxJavaVersion: String) = when (comboBoxJavaVersion) {
        "8" -> JavaVersion.parse("8").toFeatureString()
        else -> {"17"}
    }

    fun addJavaVersionBoxItems(javaVersionBox: JComboBox<String>) {
        javaVersionBox.addItem("8")
        javaVersionBox.addItem("17")
    }
}