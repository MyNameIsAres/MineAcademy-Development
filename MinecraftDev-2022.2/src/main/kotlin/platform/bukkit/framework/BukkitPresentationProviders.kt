/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.bukkit.framework

import com.demonwav.mcdev.asset.PlatformAssets
import com.demonwav.mcdev.facet.MavenLibraryPresentationProvider
import com.intellij.framework.library.LibraryVersionProperties

class BukkitPresentationProvider : MavenLibraryPresentationProvider(BUKKIT_LIBRARY_KIND, "org.bukkit", "bukkit")

class SpigotPresentationProvider : MavenLibraryPresentationProvider(SPIGOT_LIBRARY_KIND, "org.spigotmc", "spigot-api") {
    override fun getIcon(properties: LibraryVersionProperties?) = PlatformAssets.SPIGOT_ICON
}

