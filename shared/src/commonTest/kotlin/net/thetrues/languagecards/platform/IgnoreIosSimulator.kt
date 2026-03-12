package net.thetrues.languagecards.platform

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

/**
 * When applied to a test class or function, skips execution on iOS simulator.
 * Used as a workaround for DeckRepositoryTest failures on Kotlin/Native until root cause is fixed.
 */
@Target(CLASS, FUNCTION)
expect annotation class IgnoreIosSimulator()
