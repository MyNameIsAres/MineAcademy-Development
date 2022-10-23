/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev

import com.demonwav.mcdev.asset.MCDevBundle
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.editor.markup.EffectType
import java.time.LocalDate


@State(name = "MinecraftSettings", storages = [Storage("minecraft_foundation1_dev.xml")])
class MinecraftSettings : PersistentStateComponent<MinecraftSettings.State> {

    data class State(
        var isShowProjectPlatformIcons: Boolean = true,
        var isShowEventListenerGutterIcons: Boolean = true,
        var isShowChatColorGutterIcons: Boolean = true,
        var isShowChatColorUnderlines: Boolean = false,
        var underlineType: UnderlineType = UnderlineType.DOTTED,
        var nextApiCallDate: LocalDate = LocalDate.parse("2022-10-06"),
        var foundationVersion: String = "6.1.4",
        var mavenJarVersion: String = "3.2.2",
        var mavenShadeVersion: String = "3.0.0",
        var mavenCompilerVersion: String ="3.10.1"
    )

    private var state = State()

    override fun getState(): State {
        return state
    }

    override fun loadState(state: State) {
        this.state = state
    }

    // State mappings
    var isShowProjectPlatformIcons: Boolean
        get() = state.isShowProjectPlatformIcons
        set(showProjectPlatformIcons) {
            state.isShowProjectPlatformIcons = showProjectPlatformIcons
        }

    var isShowEventListenerGutterIcons: Boolean
        get() = state.isShowEventListenerGutterIcons
        set(showEventListenerGutterIcons) {
            state.isShowEventListenerGutterIcons = showEventListenerGutterIcons
        }

    var isShowChatColorGutterIcons: Boolean
        get() = state.isShowChatColorGutterIcons
        set(showChatColorGutterIcons) {
            state.isShowChatColorGutterIcons = showChatColorGutterIcons
        }

    var isShowChatColorUnderlines: Boolean
        get() = state.isShowChatColorUnderlines
        set(showChatColorUnderlines) {
            state.isShowChatColorUnderlines = showChatColorUnderlines
        }

    var underlineType: UnderlineType
        get() = state.underlineType
        set(underlineType) {
            state.underlineType = underlineType
        }

    val underlineTypeIndex: Int
        get() {
            val type = underlineType
            return UnderlineType.values().indices.firstOrNull { type == UnderlineType.values()[it] } ?: 0
        }

    var foundationVersion: String
        get() = state.foundationVersion
        set(foundationVersion) {
            state.foundationVersion = foundationVersion
        }

    var mavenJarVersion: String
        get() = state.mavenJarVersion
        set(mavenJarVersion) {
            state.mavenJarVersion = mavenJarVersion
        }

    var mavenShadeVersion: String
        get() = state.mavenShadeVersion
        set(mavenShadeVersion) {
            state.mavenShadeVersion = mavenShadeVersion
        }

    var mavenCompilerVersion: String
        get() = state.mavenCompilerVersion
        set(mavenCompilerVersion) {
            state.mavenCompilerVersion = mavenCompilerVersion
        }

    var nextApiCallDate: LocalDate
        get() = state.nextApiCallDate
        set(nextApiCallDate) {
            state.nextApiCallDate = nextApiCallDate
        }

    enum class UnderlineType(private val regular: String, val effectType: EffectType) {

        NORMAL("Normal", EffectType.LINE_UNDERSCORE),
        BOLD("Bold", EffectType.BOLD_LINE_UNDERSCORE),
        DOTTED("Dotted", EffectType.BOLD_DOTTED_LINE),
        BOXED("Boxed", EffectType.BOXED),
        ROUNDED_BOXED("Rounded Boxed", EffectType.ROUNDED_BOX),
        WAVED("Waved", EffectType.WAVE_UNDERSCORE);

        override fun toString(): String {
            return regular
        }
    }

    companion object {
        val instance: MinecraftSettings
            get() = ApplicationManager.getApplication().getService(MinecraftSettings::class.java)
    }
}
