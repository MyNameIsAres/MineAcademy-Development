/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.creator

import com.demonwav.mcdev.platform.PlatformType
import com.demonwav.mcdev.util.ProxyHttpConnectionFactory
import com.demonwav.mcdev.util.fromJson
import com.google.gson.Gson
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.diagnostic.Logger
import java.io.IOException
import javax.swing.JComboBox

private const val cloudflareBaseUrl = "https://minecraftdev.org/versions/" // loads all of the variables
private const val githubBaseUrl = "https://raw.githubusercontent.com/minecraft-dev/minecraftdev.org/master/versions/" + // loads all of the minecraft versions
    ""

val PLATFORM_VERSION_LOGGER = Logger.getInstance("MDev.PlatformVersion")

fun getVersionSelector(type: PlatformType): PlatformVersion {
    val versionJson = type.versionJson ?: throw UnsupportedOperationException("Incorrect platform type: $type")
    return getVersionJson(versionJson)
}

inline fun <reified T : Any> getVersionJson(path: String): T {
    val text = getText(path)
    try {
        return Gson().fromJson(text)
    } catch (e: Exception) {
        val attachment = Attachment("JSON Document", text)
        attachment.isIncluded = true
        PLATFORM_VERSION_LOGGER.error("Failed to parse JSON document from '$path'", e, attachment)
        throw e
    }
}

fun getText(path: String): String {
    return try {
        // attempt cloudflare
         doCall(cloudflareBaseUrl + path)

    } catch (e: IOException) {
        PLATFORM_VERSION_LOGGER.warn("Failed to reach cloudflare URL ${cloudflareBaseUrl + path}", e)
        // if that fails, attempt github
        try {
            println(doCall(githubBaseUrl + path))

            doCall(githubBaseUrl + path)
        } catch (e: IOException) {
            PLATFORM_VERSION_LOGGER.warn("Failed to reach fallback GitHub URL ${githubBaseUrl + path}", e)
            throw e
        }
    }
}

private fun doCall(urlText: String): String {
    val connection = ProxyHttpConnectionFactory.openHttpConnection(urlText)

    connection.setRequestProperty(
        "User-Agent",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
            "Chrome/72.0.3626.121 Safari/537.36"
    )

    return connection.inputStream.use { stream -> stream.reader().use { it.readText() } }
}

data class PlatformVersion(var versions: List<String>, var selectedIndex: Int) {
    fun set(combo: JComboBox<String>) {
        combo.removeAllItems()
        for (version in this.versions) {
            combo.addItem(version)
        }
        combo.selectedIndex = this.selectedIndex
    }
}
