package com.prestongarno.ktq.compiler

import org.apache.log4j.LogManager
import org.apache.log4j.Logger
import org.gradle.api.Project

/**
 * The build/project context. lateinit only on project field
 */
object QContext {

  /** Config/Extension for capturing input
   */
  fun configuration(proj: Project): QCompilerConfig =
    (proj.extensions.getByName("ktq") as QCompilerConfig?)!!
}