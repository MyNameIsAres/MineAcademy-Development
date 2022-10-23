/*
 * Minecraft Dev for IntelliJ
 *
 * https://minecraftdev.org
 *
 * Copyright (c) 2021 minecraft-dev
 *
 * MIT License
 */

package com.demonwav.mcdev.util

import com.intellij.psi.*

fun PsiAnnotationMemberValue.evaluate(allowReferences: Boolean, allowTranslations: Boolean): String? {
    val visited = mutableSetOf<PsiAnnotationMemberValue?>()

    fun eval(expr: PsiAnnotationMemberValue?, defaultValue: String? = null): String? {
        if (!visited.add(expr)) {
            return defaultValue
        }

        when {
            expr is PsiTypeCastExpression && expr.operand != null ->
                return eval(expr.operand, defaultValue)
            expr is PsiReferenceExpression -> {
                val reference = expr.advancedResolve(false).element
                if (reference is PsiVariable && reference.initializer != null) {
                    return eval(reference.initializer, "\${${expr.text}}")
                }
            }
            expr is PsiLiteral ->
                return expr.value.toString()

        }

        return if (allowReferences && expr != null) {
            "\${${expr.text}}"
        } else {
            defaultValue
        }
    }

    return eval(this)
}
