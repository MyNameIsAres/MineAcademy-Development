/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.platform.versions

import com.demonwav.mcdev.MinecraftSettings
import com.demonwav.mcdev.util.ProxyHttpConnectionFactory
import com.google.gson.annotations.SerializedName
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.project.Project
import java.io.IOException
import java.time.LocalDate

abstract class MavenPlatformVersion {

    abstract fun getMavenPlatformVersion(): String

    abstract fun updateMavenPlatformVersion(version: String): String

    abstract fun getEntryMap(): String

    protected fun getReleaseVersionOrDefault(owner: String, repo: String, type: String): String {
        if (!canUpdate()) {
            return ""
        }
        return try {
            doCall("https://api.github.com/repos/$owner/$repo/$type")
        } catch (e: IOException) {
            MyNotifier.notifyError(null, "Oops! We couldn't fetch $repo by $owner.\nUsing the default value instead.")
            return "";
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

    data class MavenVersionName(@SerializedName("name") val name: String)

   private fun canUpdate(): Boolean {
        return LocalDate.now().isAfter(MinecraftSettings.instance.nextApiCallDate)
    }

    internal object MyNotifier {
        fun notifyError(
            project: Project?,
            content: String?
        ) {
            if (content != null) {
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("Maven API Error")
                    .createNotification(content, NotificationType.ERROR)
                    .notify(project)
            }
        }
    }


}