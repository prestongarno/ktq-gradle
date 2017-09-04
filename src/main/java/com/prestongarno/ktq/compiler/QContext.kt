package com.prestongarno.ktq.compiler

import org.gradle.api.Project

object QContext {
  private const val commandShouldWrite = "-Dcom.prestongarno.ktq.compiler.writeFiles"

  lateinit var project: Project
  val configuration: QCompilerConfig by lazy { project.extensions.getByType(QCompilerConfig::class.java) }
  val isDryRun = ("" + System.getProperty(commandShouldWrite)).isBlank()
}