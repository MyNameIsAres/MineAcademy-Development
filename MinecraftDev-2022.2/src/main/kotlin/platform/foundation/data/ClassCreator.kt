/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.foundation.data

import kotlinx.serialization.Serializable

@Serializable
data class ClassCreator(
    val directories: Map<String, Directories>
)

@Serializable
data class Directories(val classList: List<String>)


