/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform


import com.demonwav.mcdev.platform.bukkit.BukkitModuleType
import com.demonwav.mcdev.platform.bukkit.SpigotModuleType
import com.demonwav.mcdev.platform.bukkit.framework.BUKKIT_LIBRARY_KIND
import com.demonwav.mcdev.platform.bukkit.framework.SPIGOT_LIBRARY_KIND
import com.demonwav.mcdev.platform.foundation.FoundationModuleType
import com.demonwav.mcdev.platform.foundation.framework.FOUNDATION_LIBRARY_KIND
import com.intellij.openapi.roots.libraries.LibraryKind

enum class PlatformType(
    val type: AbstractModuleType<*>,
    val normalName: String,
    val versionJson: String? = null,
    private val parent: PlatformType? = null
) {
    FOUNDATION(FoundationModuleType, "Foundation", "spigot.json"),
    BUKKIT(BukkitModuleType, "Bukkit", "bukkit.json"),
    SPIGOT(SpigotModuleType, "Spigot", "spigot.json", BUKKIT);


    private val children = mutableListOf<PlatformType>()

    init {
        parent?.addChild(this)
    }

    private fun addChild(child: PlatformType) {
        children += child
        parent?.addChild(child)
    }

    companion object {
        fun removeParents(types: MutableSet<PlatformType>) =
            types.filter { type -> type.children.isEmpty() || types.none { type.children.contains(it) } }.toHashSet()

        fun fromLibraryKind(kind: LibraryKind) = when (kind) {
            FOUNDATION_LIBRARY_KIND -> FOUNDATION
            BUKKIT_LIBRARY_KIND -> BUKKIT
            SPIGOT_LIBRARY_KIND -> SPIGOT
            else -> null
        }
    }
}
