/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.foundation.framework

import com.demonwav.mcdev.asset.PlatformAssets
import com.demonwav.mcdev.facet.MavenLibraryPresentationProvider
import com.intellij.framework.library.LibraryVersionProperties

class FoundationPresentationProvider : MavenLibraryPresentationProvider(
    FOUNDATION_LIBRARY_KIND,
    "org.mineacademy",
    "Foundation"
) {
    override fun getIcon(properties: LibraryVersionProperties?) = PlatformAssets.FOUNDATION_ICON
}

