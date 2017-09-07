package com.prestongarno.ktq.compiler

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

open class CompilerPlugin : Plugin<Project> {

  override fun apply(project: Project?) {
    QContext.project = project!!
    project.extensions?.create("ktq", QCompilerConfig::class.java, project)
    project.tasks.create("compileGraphql", QCompilationRunner::class.java)
  }
}


fun String.toJavaFileCompat(): String {
  return replace("[^a-z|A-Z|0-9]*".toRegex(), "")
      .let {
        val firstLetter = it.indexOfFirst {
          it.toString().matches("[a-zA-Z]".toRegex())
        }
        if (firstLetter > -1)
          it.substring(firstLetter).let {
            "${it[0].toUpperCase()}${it.substring(1)}"
          }.let {
            if (!it.endsWith(".kt")) it + ".kt" else it
          }
        else throw IllegalArgumentException("Can not generate schema for '$this': Please use a " +
            "java-compatible name for your schema or specify a name in your buildscript")
      }
}

fun File.child(relative: String): File = File("${this.path}/$relative")

fun String.asFile(): File = File(this)

