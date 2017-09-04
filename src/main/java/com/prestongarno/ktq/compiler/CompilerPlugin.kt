package com.prestongarno.ktq.compiler

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.PropertyState
import java.io.File

open class CompilerPlugin : Plugin<Project> {

  override fun apply(project: Project?) {
    Context.project = project!!
    project.extensions?.create("ktq", QCompilerConfig::class.java, project)!!
    project.tasks.create("ktqCompile", QCompilationRunner::class.java)
  }
}

object Context {
  lateinit var project: Project
  val configuration: QCompilerConfig by lazy { project.extensions.getByType(QCompilerConfig::class.java) }
}


fun String.toJavaFileCompat(): String {
  return replace("[^a-z|A-Z|0-9]*".toRegex(), "")
      .let {
        val firstLetter = it.indexOfFirst {
          it.toString().matches("[a-zA-Z]".toRegex())
        }
        if (firstLetter > -1)
          it.substring(firstLetter)
        else throw IllegalArgumentException("Can not generate schema for '$this': Please use a " +
            "java-compatible name for your schema or specify a name in your buildscript")
      }
}

fun PropertyState<File>.child(relative: String): File =
    get()?.let { it.child(relative) }
        ?: throw IllegalStateException("No such file to set child $relative")

fun File.child(relative: String): File = File("$absolutePath/$relative")

fun String.asFile(): File = File(this)
