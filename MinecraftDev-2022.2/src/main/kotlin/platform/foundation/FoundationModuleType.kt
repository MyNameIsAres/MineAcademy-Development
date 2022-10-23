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

import com.demonwav.mcdev.asset.PlatformAssets
import com.demonwav.mcdev.facet.MinecraftFacet
import com.demonwav.mcdev.platform.AbstractModuleType
import com.demonwav.mcdev.platform.BaseTemplate
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.foundation.creator.FoundationTemplate
import com.demonwav.mcdev.platform.foundation.generation.FoundationBukkitEventGenerationPanel
import com.demonwav.mcdev.platform.foundation.util.FoundationConstants
import com.demonwav.mcdev.util.CommonColors
import com.intellij.psi.PsiClass

object FoundationModuleType : AbstractModuleType<FoundationModule<FoundationModuleType>>(
    "org.mineacademy",
    "Foundation"
) {

    private const val ID = "FOUNDATION_MODULE_TYPE"

    val IGNORED_ANNOTATIONS = listOf(FoundationConstants.HANDLER_ANNOTATION)
    val LISTENER_ANNOTATIONS = listOf(FoundationConstants.HANDLER_ANNOTATION)

    init {
        CommonColors.applyStandardColors(colorMap, FoundationConstants.CHAT_COLOR_CLASS)
    }

    override val platformType = PlatformType.FOUNDATION
    override val icon = PlatformAssets.FOUNDATION_ICON
    override val id = ID
    override val ignoredAnnotations = IGNORED_ANNOTATIONS
    override val listenerAnnotations = LISTENER_ANNOTATIONS
    override val isEventGenAvailable = true

    override fun generateModule(facet: MinecraftFacet): FoundationModule<FoundationModuleType> = FoundationModule(
        facet,
        this
    )
    override fun getEventGenerationPanel(chosenClass: PsiClass) = FoundationBukkitEventGenerationPanel(chosenClass)
}
