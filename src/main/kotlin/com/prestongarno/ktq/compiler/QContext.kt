package com.prestongarno.ktq.compiler

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.gradle.api.Project

/**
 * The build/project context. lateinit only on project field
 */
object QContext {

  lateinit var project: Project

  /** Config/Extension for capturing input
   */
  val configuration: QCompilerConfig by lazy {
    project.extensions
        .getByType(QCompilerConfig::class.java)
  }

  val isDryRun = ("" + System.getProperty("com.prestongarno.ktq.compiler.writeFiles")).isNotBlank()

  inline fun <reified T> logger(): Lazy<Logger> = lazy { LogManager.getLogger(T::class.java) }
}