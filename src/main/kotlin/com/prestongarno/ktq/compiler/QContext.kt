package com.prestongarno.ktq.compiler

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.gradle.api.Project

object QContext {
  private const val commandShouldWrite = "com.prestongarno.ktq.compiler.writeFiles"

  inline fun <reified T> logger() : Lazy<Logger> = lazy { LogManager.getLogger(T::class.java) }

  lateinit var project: Project
  val configuration: QCompilerConfig by lazy { project.extensions.getByType(QCompilerConfig::class.java) }
  val isDryRun = ("" + System.getProperty(commandShouldWrite)).isBlank()
}