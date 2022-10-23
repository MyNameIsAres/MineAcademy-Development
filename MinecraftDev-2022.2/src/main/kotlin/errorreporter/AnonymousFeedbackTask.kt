/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.errorreporter

import com.demonwav.mcdev.util.ProxyHttpConnectionFactory
import com.intellij.openapi.diagnostic.Attachment
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project

class AnonymousFeedbackTask(
    project: Project?,
    title: String,
    canBeCancelled: Boolean,
    private val params: LinkedHashMap<String, String?>,
    private val attachments: List<Attachment>,
    private val callback: (String, Int, Boolean) -> Unit,
    private val errorCallback: (Exception) -> Unit
) : Task.Backgroundable(project, title, canBeCancelled) {

    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true

        try {
            val factory = ProxyHttpConnectionFactory
            val (url, token, isDuplicate) = AnonymousFeedback.sendFeedback(factory, params, attachments)

            callback(url, token, isDuplicate)
        } catch (e: Exception) {
            errorCallback(e)
        }
    }
}
