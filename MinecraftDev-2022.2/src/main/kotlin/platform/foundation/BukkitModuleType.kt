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
import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.platform.foundation.generation.FoundationBukkitEventGenerationPanel
import com.demonwav.mcdev.platform.foundation.util.FoundationConstants
import com.demonwav.mcdev.util.CommonColors
import com.demonwav.mcdev.util.SemanticVersion
import com.intellij.psi.PsiClass

object BukkitModuleType : AbstractModuleType<FoundationModule<BukkitModuleType>>("org.bukkit", "bukkit") {

    private const val ID = "BUKKIT_MODULE_TYPE"

    val IGNORED_ANNOTATIONS = listOf(FoundationConstants.HANDLER_ANNOTATION)
    val LISTENER_ANNOTATIONS = listOf(FoundationConstants.HANDLER_ANNOTATION)

    init {
        CommonColors.applyStandardColors(colorMap, FoundationConstants.CHAT_COLOR_CLASS)
    }

    override val platformType = PlatformType.BUKKIT
    override val icon = PlatformAssets.BUKKIT_ICON
    override val id = ID
    override val ignoredAnnotations = IGNORED_ANNOTATIONS
    override val listenerAnnotations = LISTENER_ANNOTATIONS
    override val isEventGenAvailable = true

    override fun generateModule(facet: MinecraftFacet): FoundationModule<BukkitModuleType> = FoundationModule(
        facet,
        this
    )
    override fun getEventGenerationPanel(chosenClass: PsiClass) = FoundationBukkitEventGenerationPanel(chosenClass)

    val API_TAG_VERSION = SemanticVersion.release(1, 13)
}
