/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.insight

import com.demonwav.mcdev.asset.GeneralAssets
import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.platform.foundation.FoundationModuleType
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.module.ModuleUtilCore
import com.intellij.psi.PsiElement
import com.intellij.util.FunctionUtil

class PluginLineMarkerProvider : LineMarkerProviderDescriptor() {

    override fun getName() = "Minecraft Plugin line marker"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? {
        if (!element.isValid) {
            return null
        }

        val module = ModuleUtilCore.findModuleForPsiElement(element) ?: return null

        val instance = MinecraftFacet.getInstance(module) ?: return null

        if (!instance.shouldShowPluginIcon(element)) {
            return null
        }

        return LineMarkerInfo(
            element,
            element.textRange,
            GeneralAssets.PLUGIN,
            FunctionUtil.nullConstant(),
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { "minecraft plugin entry point indicator" }
        )
    }
}
