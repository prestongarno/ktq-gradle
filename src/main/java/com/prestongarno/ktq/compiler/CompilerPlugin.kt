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
    project.file("destinationDir") ?:
        File("${project.buildDir.absolutePath}/generated/ktq/")
  }

  var packageName: String = "com.prestongarno.ktq.generated"
  var kotlinName: String = ""

  /**
   * TODO : Need to verify more things like write access, etc.
   */
  override fun doLast(action: Action<in Task>?): Task {
    if (schema?.exists() != false) {
      QCompiler.initialize()
          .packageName(packageName)
          .compile(schema!!)
          .writeToFile("${destinationDir.absolutePath}/${
          if (kotlinName.isNotEmpty())
            kotlinName
          else schema!!.nameWithoutExtension
              .toJavaFileCompat()
          }")
    }
    return super.doLast(action)
  }
}

private fun String.toJavaFileCompat(): String {
  return replace("[^a-z|A-Z|0-9]*".toRegex(), "")
      .let {
        val firstLetter = it.indexOfFirst { "$it".matches("[a-zA-Z]".toRegex()) }
        if (firstLetter > 0)
          it.substring(firstLetter)
        else ""
      }
}
