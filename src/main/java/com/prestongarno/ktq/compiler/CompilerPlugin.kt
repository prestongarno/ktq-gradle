package com.prestongarno.ktq.compiler

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File

object CompilerPlugin : Plugin<Project> {

  override fun apply(project: Project?) {
    project?.extensions?.create("graphql", QCompilerConfig::class.java)
  }
}

class QCompilerConfig : DefaultTask() {

  val schema: File? by lazy {
    project.file("schema")
  }

  val destinationDir: File by lazy {
    project.file("destinationDir")?:
        File("${project.buildDir.absolutePath}/generated/ktq/")
  }

  override fun doLast(action: Action<in Task>?): Task {
    if (schema != null)
      TODO("Run the compilation on the schema:) ")
    return super.doLast(action)
  }

}

