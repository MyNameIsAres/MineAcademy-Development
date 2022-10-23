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
// import com.demonwav.mcdev.platform.foundation.generation.FoundationEventGenerationPanel
import com.demonwav.mcdev.platform.foundation.util.FoundationConstants
import com.demonwav.mcdev.util.CommonColors

object PaperModuleType : AbstractModuleType<FoundationModule<PaperModuleType>>("com.destroystokyo.paper", "paper-api") {

    private const val ID = "PAPER_MODULE_TYPE"

    init {
        CommonColors.applyStandardColors(colorMap, FoundationConstants.CHAT_COLOR_CLASS)
    }

    override val platformType = PlatformType.SPIGOT
    override val icon = PlatformAssets.PAPER_ICON
    override val id = ID
    override val ignoredAnnotations = BukkitModuleType.IGNORED_ANNOTATIONS
    override val listenerAnnotations = BukkitModuleType.LISTENER_ANNOTATIONS
    override val isEventGenAvailable = true

    override fun generateModule(facet: MinecraftFacet): FoundationModule<PaperModuleType> = FoundationModule(
        facet,
        this
    )
//    override fun getEventGenerationPanel(chosenClass: PsiClass) = FoundationEventGenerationPanel(chosenClass)
}
