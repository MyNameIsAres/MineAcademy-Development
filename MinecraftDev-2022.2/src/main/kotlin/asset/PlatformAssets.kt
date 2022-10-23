/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.asset

@Suppress("unused")
object PlatformAssets : Assets() {

    val FOUNDATION_ICON = loadIcon("/assets/icons/platform/Foundation.png")
    val FOUNDATION_ICON_2X = loadIcon("/assets/icons/platform/Foundation@2x.png")

    val MINECRAFT_ICON = loadIcon("/assets/icons/platform/Minecraft.png")
    val MINECRAFT_ICON_2X = loadIcon("/assets/icons/platform/Minecraft@2x.png")

    val BUKKIT_ICON = loadIcon("/assets/icons/platform/Bukkit.png")
    val BUKKIT_ICON_2X = loadIcon("/assets/icons/platform/Bukkit@2x.png")
    val SPIGOT_ICON = loadIcon("/assets/icons/platform/Spigot.png")
    val SPIGOT_ICON_2X = loadIcon("/assets/icons/platform/Spigot@2x.png")
    val PAPER_ICON = loadIcon("/assets/icons/platform/Paper.png")
    val PAPER_ICON_2X = loadIcon("/assets/icons/platform/Paper@2x.png")
}
