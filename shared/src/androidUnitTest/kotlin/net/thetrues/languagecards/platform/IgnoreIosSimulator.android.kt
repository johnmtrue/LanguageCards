package net.thetrues.languagecards.platform

import kotlin.annotation.AnnotationTarget.CLASS
import kotlin.annotation.AnnotationTarget.FUNCTION

@Target(CLASS, FUNCTION)
actual annotation class IgnoreIosSimulator()
